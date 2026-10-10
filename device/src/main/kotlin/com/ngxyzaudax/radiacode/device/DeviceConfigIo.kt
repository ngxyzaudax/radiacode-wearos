package com.ngxyzaudax.radiacode.device

import com.ngxyzaudax.radiacode.protocol.AlarmLimits
import com.ngxyzaudax.radiacode.protocol.AlarmSignalMode
import com.ngxyzaudax.radiacode.protocol.BacklightOffTime
import com.ngxyzaudax.radiacode.protocol.CountDisplayUnit
import com.ngxyzaudax.radiacode.protocol.DEVICE_CTRL_LIGHT
import com.ngxyzaudax.radiacode.protocol.DeviceConfig
import com.ngxyzaudax.radiacode.protocol.DisplayDirection
import com.ngxyzaudax.radiacode.log.RadiaLog
import com.ngxyzaudax.radiacode.protocol.DoseDisplayUnit
import com.ngxyzaudax.radiacode.protocol.SignalFlags
import com.ngxyzaudax.radiacode.protocol.VirtSfr
import com.ngxyzaudax.radiacode.protocol.VirtString
import com.ngxyzaudax.radiacode.protocol.decodeCountAlarm
import com.ngxyzaudax.radiacode.protocol.decodeDoseAccum
import com.ngxyzaudax.radiacode.protocol.decodeDoseAlarm
import com.ngxyzaudax.radiacode.protocol.encodeCountAlarm
import com.ngxyzaudax.radiacode.protocol.encodeDoseAccum
import com.ngxyzaudax.radiacode.protocol.encodeDoseAlarm
import com.ngxyzaudax.radiacode.protocol.sfrSupportsLedsOn
import java.nio.charset.Charset

internal suspend fun loadDeviceConfig(
    vsfr: VsfrIo,
    readVirtString: suspend (VirtString) -> ByteArray,
): DeviceConfig {
    val ids = listOf(
        VirtSfr.CrLev1Cp10s,
        VirtSfr.CrLev2Cp10s,
        VirtSfr.DrLev1UrH,
        VirtSfr.DrLev2UrH,
        VirtSfr.DsLev1Ur,
        VirtSfr.DsLev2Ur,
        VirtSfr.DsUnits,
        VirtSfr.CrUnits,
        VirtSfr.AlarmMode,
        VirtSfr.DispBrt,
        VirtSfr.DispOffTime,
        VirtSfr.DispDir,
        VirtSfr.SoundOn,
        VirtSfr.SoundCtrl,
        VirtSfr.VibroOn,
        VirtSfr.VibroCtrl,
    )
    val values = vsfr.readVsfrBatch(ids)
    val doseUnit = DoseDisplayUnit.fromDeviceFlag(values[6])
    val countUnit = CountDisplayUnit.fromDeviceFlag(values[7])
    val light = loadLightState(vsfr, readVirtString)
    return DeviceConfig(
        alarms = AlarmLimits(
            l1CountRate = decodeCountAlarm(values[0], countUnit),
            l2CountRate = decodeCountAlarm(values[1], countUnit),
            l1DoseRate = decodeDoseAlarm(values[2], doseUnit),
            l2DoseRate = decodeDoseAlarm(values[3], doseUnit),
            l1Dose = decodeDoseAccum(values[4], doseUnit),
            l2Dose = decodeDoseAccum(values[5], doseUnit),
            doseUnit = doseUnit,
            countUnit = countUnit,
        ),
        alarmMode = AlarmSignalMode.fromRaw(values[8]),
        brightness = values[9].coerceIn(0, 9),
        backlightOff = BacklightOffTime.fromRaw(values[10]),
        displayDir = DisplayDirection.fromRaw(values[11]),
        soundOn = values[12] != 0,
        soundCtrl = SignalFlags.fromRaw(values[13]),
        vibroOn = values[14] != 0,
        vibroCtrl = SignalFlags.fromRaw(values[15]).vibroEvents(),
        ledsOn = light.first,
        ledsSupported = light.second,
        ledsUsesDeviceCtrl = light.third,
    )
}

internal suspend fun applyDeviceConfig(vsfr: VsfrIo, config: DeviceConfig): Unit {
    val doseUnit = config.alarms.doseUnit
    val countUnit = config.alarms.countUnit
    val pairs = mutableListOf(
        VirtSfr.CrLev1Cp10s to encodeCountAlarm(config.alarms.l1CountRate, countUnit),
        VirtSfr.CrLev2Cp10s to encodeCountAlarm(config.alarms.l2CountRate, countUnit),
        VirtSfr.DrLev1UrH to encodeDoseAlarm(config.alarms.l1DoseRate, doseUnit),
        VirtSfr.DrLev2UrH to encodeDoseAlarm(config.alarms.l2DoseRate, doseUnit),
        VirtSfr.DsLev1Ur to encodeDoseAccum(config.alarms.l1Dose, doseUnit),
        VirtSfr.DsLev2Ur to encodeDoseAccum(config.alarms.l2Dose, doseUnit),
        VirtSfr.DsUnits to doseUnit.toDeviceFlag(),
        VirtSfr.CrUnits to countUnit.toDeviceFlag(),
        VirtSfr.AlarmMode to config.alarmMode.asRaw(),
        VirtSfr.DispBrt to config.brightness.coerceIn(0, 9),
        VirtSfr.DispOffTime to config.backlightOff.asRaw(),
        VirtSfr.DispDir to config.displayDir.asRaw(),
        VirtSfr.SoundOn to if (config.soundOn) 1 else 0,
        VirtSfr.SoundCtrl to config.soundCtrl.asRaw(),
        VirtSfr.VibroOn to if (config.vibroOn) 1 else 0,
        VirtSfr.VibroCtrl to config.vibroCtrl.vibroEvents().asRaw(),
    )
    if (config.ledsSupported && !config.ledsUsesDeviceCtrl) {
        pairs.add(VirtSfr.LedsOn to if (config.ledsOn) 1 else 0)
    }
    vsfr.writeVsfrBatch(pairs)
    if (config.ledsSupported && config.ledsUsesDeviceCtrl) {
        applyDeviceCtrlLight(vsfr, config.ledsOn)
    }
}

/**
 * LED control is optional across firmware revisions, so probing it must never discard the alarm
 * limits and signal flags that were already read successfully.
 */
private suspend fun loadLightState(
    vsfr: VsfrIo,
    readVirtString: suspend (VirtString) -> ByteArray,
): Triple<Boolean, Boolean, Boolean> =
    runCatching { readLightState(vsfr, readVirtString) }
        .getOrElse { error ->
            RadiaLog.warn("DeviceConfig", "Light state unavailable ${error.message}")
            Triple(false, false, false)
        }

private suspend fun readLightState(
    vsfr: VsfrIo,
    readVirtString: suspend (VirtString) -> ByteArray,
): Triple<Boolean, Boolean, Boolean> {
    val sfrFile = readVirtString(VirtString.SfrFile)
    val text = String(sfrFile, Charset.defaultCharset())
    if (sfrSupportsLedsOn(text)) {
        val ledsOn = vsfr.readVsfrOptional(VirtSfr.LedsOn)?.let { it != 0 } ?: false
        return Triple(ledsOn, true, false)
    }
    val deviceCtrl = vsfr.readVsfrOptional(VirtSfr.DeviceCtrl)
        ?: return Triple(false, false, false)
    return Triple(deviceCtrl and DEVICE_CTRL_LIGHT != 0, true, true)
}

private suspend fun applyDeviceCtrlLight(vsfr: VsfrIo, ledsOn: Boolean): Unit {
    val current = vsfr.readVsfrU32(VirtSfr.DeviceCtrl)
    val next = if (ledsOn) current or DEVICE_CTRL_LIGHT else current and DEVICE_CTRL_LIGHT.inv()
    if (next != current) vsfr.writeVsfr(VirtSfr.DeviceCtrl, next)
}
