package com.ngxyzaudax.radiacode.protocol

private const val CTRL_BUTTONS: Int = 1 shl 0
private const val CTRL_CLICKS: Int = 1 shl 1
private const val CTRL_DR_ALARM1: Int = 1 shl 2
private const val CTRL_DR_ALARM2: Int = 1 shl 3
private const val CTRL_DR_OOS: Int = 1 shl 4
private const val CTRL_DOSE_ALARM1: Int = 1 shl 5
private const val CTRL_DOSE_ALARM2: Int = 1 shl 6
private const val CTRL_DOSE_OOS: Int = 1 shl 7
private const val CTRL_CONNECTION: Int = 1 shl 8
private const val CTRL_POWER: Int = 1 shl 9
private const val CTRL_CR_ALARM1: Int = 1 shl 10
private const val CTRL_CR_ALARM2: Int = 1 shl 11
private const val CTRL_CR_OOS: Int = 1 shl 12
private const val CTRL_KNOWN_MASK: Int = 0x1FFF

const val DEVICE_CTRL_LIGHT: Int = 1 shl 3

data class AlarmLimits(
    val l1CountRate: Float = 0f,
    val l2CountRate: Float = 0f,
    val l1DoseRate: Float = 0f,
    val l2DoseRate: Float = 0f,
    val l1Dose: Float = 0f,
    val l2Dose: Float = 0f,
    val doseUnit: DoseDisplayUnit = DoseDisplayUnit.MicroSievertPerHour,
    val countUnit: CountDisplayUnit = CountDisplayUnit.Cps,
)

data class SignalFlags(
    val buttons: Boolean = false,
    val clicks: Boolean = false,
    val doseRateAlarm1: Boolean = false,
    val doseRateAlarm2: Boolean = false,
    val doseRateOutOfScale: Boolean = false,
    val doseAlarm1: Boolean = false,
    val doseAlarm2: Boolean = false,
    val doseOutOfScale: Boolean = false,
    val connection: Boolean = false,
    val power: Boolean = false,
    val countRateAlarm1: Boolean = false,
    val countRateAlarm2: Boolean = false,
    val countRateOutOfScale: Boolean = false,
    val reserved: Int = 0,
) {
    fun asRaw(): Int {
        var raw = reserved
        if (buttons) raw = raw or CTRL_BUTTONS
        if (clicks) raw = raw or CTRL_CLICKS
        if (doseRateAlarm1) raw = raw or CTRL_DR_ALARM1
        if (doseRateAlarm2) raw = raw or CTRL_DR_ALARM2
        if (doseRateOutOfScale) raw = raw or CTRL_DR_OOS
        if (doseAlarm1) raw = raw or CTRL_DOSE_ALARM1
        if (doseAlarm2) raw = raw or CTRL_DOSE_ALARM2
        if (doseOutOfScale) raw = raw or CTRL_DOSE_OOS
        if (connection) raw = raw or CTRL_CONNECTION
        if (power) raw = raw or CTRL_POWER
        if (countRateAlarm1) raw = raw or CTRL_CR_ALARM1
        if (countRateAlarm2) raw = raw or CTRL_CR_ALARM2
        if (countRateOutOfScale) raw = raw or CTRL_CR_OOS
        return raw
    }

    fun vibroEvents(): SignalFlags =
        copy(clicks = false, connection = false, power = false)

    companion object {
        fun fromRaw(raw: Int): SignalFlags = SignalFlags(
            buttons = raw and CTRL_BUTTONS != 0,
            clicks = raw and CTRL_CLICKS != 0,
            doseRateAlarm1 = raw and CTRL_DR_ALARM1 != 0,
            doseRateAlarm2 = raw and CTRL_DR_ALARM2 != 0,
            doseRateOutOfScale = raw and CTRL_DR_OOS != 0,
            doseAlarm1 = raw and CTRL_DOSE_ALARM1 != 0,
            doseAlarm2 = raw and CTRL_DOSE_ALARM2 != 0,
            doseOutOfScale = raw and CTRL_DOSE_OOS != 0,
            connection = raw and CTRL_CONNECTION != 0,
            power = raw and CTRL_POWER != 0,
            countRateAlarm1 = raw and CTRL_CR_ALARM1 != 0,
            countRateAlarm2 = raw and CTRL_CR_ALARM2 != 0,
            countRateOutOfScale = raw and CTRL_CR_OOS != 0,
            reserved = raw and CTRL_KNOWN_MASK.inv(),
        )
    }
}

enum class AlarmSignalMode {
    Once,
    Continuous,
    ;

    fun asRaw(): Int = if (this == Continuous) 0 else 1

    companion object {
        fun fromRaw(raw: Int): AlarmSignalMode =
            if (raw == 0) Continuous else Once
    }
}

enum class DisplayDirection {
    Auto,
    Right,
    Left,
    ;

    fun asRaw(): Int = ordinal

    companion object {
        fun fromRaw(raw: Int): DisplayDirection = when (raw) {
            1 -> Right
            2 -> Left
            else -> Auto
        }
    }
}

enum class BacklightOffTime {
    Sec5,
    Sec10,
    Sec15,
    Sec30,
    Min2,
    Min5,
    ;

    fun asRaw(): Int = ordinal

    companion object {
        fun fromRaw(raw: Int): BacklightOffTime = when (raw) {
            1 -> Sec10
            2 -> Sec15
            3 -> Sec30
            4 -> Min2
            5 -> Min5
            else -> Sec5
        }
    }
}

data class DeviceConfig(
    val alarms: AlarmLimits,
    val alarmMode: AlarmSignalMode = AlarmSignalMode.Continuous,
    val brightness: Int = 5,
    val backlightOff: BacklightOffTime = BacklightOffTime.Sec5,
    val displayDir: DisplayDirection = DisplayDirection.Auto,
    val soundOn: Boolean = true,
    val vibroOn: Boolean = true,
    val ledsOn: Boolean = true,
    val ledsSupported: Boolean = true,
    val ledsUsesDeviceCtrl: Boolean = false,
    val soundCtrl: SignalFlags = SignalFlags(),
    val vibroCtrl: SignalFlags = SignalFlags(),
)
