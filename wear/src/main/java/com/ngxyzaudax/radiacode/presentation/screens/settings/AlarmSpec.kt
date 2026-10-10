package com.ngxyzaudax.radiacode.presentation.screens.settings

import com.ngxyzaudax.radiacode.protocol.AlarmLimits
import com.ngxyzaudax.radiacode.protocol.DeviceConfig
import com.ngxyzaudax.radiacode.protocol.SignalFlags
import com.ngxyzaudax.radiacode.protocol.doseAccumUnitLabel
import com.ngxyzaudax.radiacode.protocol.doseUnitLabel

data class AlarmSpec(
    val key: AlarmKey,
    val levelLabel: String,
    val valueGetter: (AlarmLimits) -> Float,
    val valueSetter: (AlarmLimits, Float) -> AlarmLimits,
    val soundGetter: (SignalFlags) -> Boolean,
    val soundSetter: (SignalFlags, Boolean) -> SignalFlags,
    val vibroGetter: (SignalFlags) -> Boolean,
    val vibroSetter: (SignalFlags, Boolean) -> SignalFlags,
    val unitLabel: (AlarmLimits) -> String,
)

fun alarmSpec(key: AlarmKey): AlarmSpec =
    alarmSpecs().first { it.key == key }

fun alarmSpecs(): List<AlarmSpec> = listOf(
    doseRateSpec(AlarmKey.DoseRateWarn, AlarmLevel.Warn),
    doseRateSpec(AlarmKey.DoseRateDanger, AlarmLevel.Danger),
    countRateSpec(AlarmKey.CountRateWarn, AlarmLevel.Warn),
    countRateSpec(AlarmKey.CountRateDanger, AlarmLevel.Danger),
    accumDoseSpec(AlarmKey.AccumDoseWarn, AlarmLevel.Warn),
    accumDoseSpec(AlarmKey.AccumDoseDanger, AlarmLevel.Danger),
)

fun applyAlarmEdit(
    config: DeviceConfig,
    key: AlarmKey,
    value: Float,
    sound: Boolean,
    vibro: Boolean,
): DeviceConfig {
    val spec = alarmSpec(key)
    val scale = alarmStepScale(key, config.alarms)
    val pair = pairedAlarmValues(key, scale.snap(value), config.alarms, scale)
    return config.copy(
        alarms = withLevelValues(key.metric, config.alarms, pair),
        soundCtrl = spec.soundSetter(config.soundCtrl, sound),
        vibroCtrl = spec.vibroSetter(config.vibroCtrl, vibro),
    )
}

private fun doseRateSpec(key: AlarmKey, level: AlarmLevel): AlarmSpec =
    AlarmSpec(
        key = key,
        levelLabel = levelLabel(level),
        valueGetter = if (level == AlarmLevel.Warn) ({ it.l1DoseRate }) else ({ it.l2DoseRate }),
        valueSetter = if (level == AlarmLevel.Warn) {
            { limits, value -> limits.copy(l1DoseRate = value) }
        } else {
            { limits, value -> limits.copy(l2DoseRate = value) }
        },
        soundGetter = if (level == AlarmLevel.Warn) ({ it.doseRateAlarm1 }) else ({ it.doseRateAlarm2 }),
        soundSetter = if (level == AlarmLevel.Warn) {
            { flags, enabled -> flags.copy(doseRateAlarm1 = enabled) }
        } else {
            { flags, enabled -> flags.copy(doseRateAlarm2 = enabled) }
        },
        vibroGetter = if (level == AlarmLevel.Warn) ({ it.doseRateAlarm1 }) else ({ it.doseRateAlarm2 }),
        vibroSetter = if (level == AlarmLevel.Warn) {
            { flags, enabled -> flags.copy(doseRateAlarm1 = enabled) }
        } else {
            { flags, enabled -> flags.copy(doseRateAlarm2 = enabled) }
        },
        unitLabel = { doseUnitLabel(it.doseUnit) },
    )

private fun countRateSpec(key: AlarmKey, level: AlarmLevel): AlarmSpec =
    AlarmSpec(
        key = key,
        levelLabel = levelLabel(level),
        valueGetter = if (level == AlarmLevel.Warn) ({ it.l1CountRate }) else ({ it.l2CountRate }),
        valueSetter = if (level == AlarmLevel.Warn) {
            { limits, value -> limits.copy(l1CountRate = value) }
        } else {
            { limits, value -> limits.copy(l2CountRate = value) }
        },
        soundGetter = if (level == AlarmLevel.Warn) ({ it.countRateAlarm1 }) else ({ it.countRateAlarm2 }),
        soundSetter = if (level == AlarmLevel.Warn) {
            { flags, enabled -> flags.copy(countRateAlarm1 = enabled) }
        } else {
            { flags, enabled -> flags.copy(countRateAlarm2 = enabled) }
        },
        vibroGetter = if (level == AlarmLevel.Warn) ({ it.countRateAlarm1 }) else ({ it.countRateAlarm2 }),
        vibroSetter = if (level == AlarmLevel.Warn) {
            { flags, enabled -> flags.copy(countRateAlarm1 = enabled) }
        } else {
            { flags, enabled -> flags.copy(countRateAlarm2 = enabled) }
        },
        unitLabel = { it.countUnit.name },
    )

private fun accumDoseSpec(key: AlarmKey, level: AlarmLevel): AlarmSpec =
    AlarmSpec(
        key = key,
        levelLabel = levelLabel(level),
        valueGetter = if (level == AlarmLevel.Warn) ({ it.l1Dose }) else ({ it.l2Dose }),
        valueSetter = if (level == AlarmLevel.Warn) {
            { limits, value -> limits.copy(l1Dose = value) }
        } else {
            { limits, value -> limits.copy(l2Dose = value) }
        },
        soundGetter = if (level == AlarmLevel.Warn) ({ it.doseAlarm1 }) else ({ it.doseAlarm2 }),
        soundSetter = if (level == AlarmLevel.Warn) {
            { flags, enabled -> flags.copy(doseAlarm1 = enabled) }
        } else {
            { flags, enabled -> flags.copy(doseAlarm2 = enabled) }
        },
        vibroGetter = if (level == AlarmLevel.Warn) ({ it.doseAlarm1 }) else ({ it.doseAlarm2 }),
        vibroSetter = if (level == AlarmLevel.Warn) {
            { flags, enabled -> flags.copy(doseAlarm1 = enabled) }
        } else {
            { flags, enabled -> flags.copy(doseAlarm2 = enabled) }
        },
        unitLabel = { doseAccumUnitLabel(it.doseUnit) },
    )

private fun levelLabel(level: AlarmLevel): String =
    when (level) {
        AlarmLevel.Warn -> "WARN"
        AlarmLevel.Danger -> "DANGER"
    }

fun metricSectionLabel(metric: AlarmMetric): String =
    when (metric) {
        AlarmMetric.DoseRate -> "DOSE RATE"
        AlarmMetric.CountRate -> "COUNT RATE"
        AlarmMetric.AccumDose -> "ACCUM DOSE"
    }

fun signalMarkers(sound: Boolean, vibro: Boolean): String =
    buildString {
        append(if (sound) "S" else "-")
        append(" ")
        append(if (vibro) "V" else "-")
    }
