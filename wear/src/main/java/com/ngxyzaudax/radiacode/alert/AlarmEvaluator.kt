package com.ngxyzaudax.radiacode.alert

import com.ngxyzaudax.radiacode.data.LiveReading
import com.ngxyzaudax.radiacode.protocol.SignalFlags

private const val RELEASE_FACTOR: Float = 0.95f

fun activeAlarmEvents(reading: LiveReading, previous: Set<AlarmEvent>): Set<AlarmEvent> {
    val limits = reading.alarmLimits
    val active = mutableSetOf<AlarmEvent>()
    evaluateRate(
        reading.countRate,
        limits.l1CountRate,
        limits.l2CountRate,
        AlarmEvent.CountRateL1,
        AlarmEvent.CountRateL2,
        previous,
        active,
    )
    evaluateRate(
        reading.doseRate,
        limits.l1DoseRate,
        limits.l2DoseRate,
        AlarmEvent.DoseRateL1,
        AlarmEvent.DoseRateL2,
        previous,
        active,
    )
    evaluateRate(
        reading.accumulatedDose,
        limits.l1Dose,
        limits.l2Dose,
        AlarmEvent.AccumDoseL1,
        AlarmEvent.AccumDoseL2,
        previous,
        active,
    )
    return active
}

private fun evaluateRate(
    value: Float,
    warn: Float,
    danger: Float,
    warnEvent: AlarmEvent,
    dangerEvent: AlarmEvent,
    previous: Set<AlarmEvent>,
    active: MutableSet<AlarmEvent>,
): Unit {
    if (danger > 0f && crossesThreshold(value, danger, dangerEvent, previous)) {
        active.add(dangerEvent)
    }
    if (warn > 0f && crossesThreshold(value, warn, warnEvent, previous)) {
        active.add(warnEvent)
    }
}

private fun crossesThreshold(
    value: Float,
    threshold: Float,
    event: AlarmEvent,
    previous: Set<AlarmEvent>,
): Boolean {
    val release = threshold * RELEASE_FACTOR
    return if (previous.contains(event)) value >= release else value >= threshold
}

fun isSoundEnabled(flags: SignalFlags, event: AlarmEvent): Boolean =
    eventFlag(event, flags)

fun isVibrationEnabled(flags: SignalFlags, event: AlarmEvent): Boolean =
    eventFlag(event, flags.vibroEvents())

private fun eventFlag(event: AlarmEvent, flags: SignalFlags): Boolean =
    when (event) {
        AlarmEvent.CountRateL1 -> flags.countRateAlarm1
        AlarmEvent.CountRateL2 -> flags.countRateAlarm2
        AlarmEvent.DoseRateL1 -> flags.doseRateAlarm1
        AlarmEvent.DoseRateL2 -> flags.doseRateAlarm2
        AlarmEvent.AccumDoseL1 -> flags.doseAlarm1
        AlarmEvent.AccumDoseL2 -> flags.doseAlarm2
    }

fun isLevelTwo(event: AlarmEvent): Boolean =
    when (event) {
        AlarmEvent.CountRateL2,
        AlarmEvent.DoseRateL2,
        AlarmEvent.AccumDoseL2,
        -> true
        else -> false
    }
