package com.ngxyzaudax.radiacode.alert

import com.ngxyzaudax.radiacode.data.LiveReading
import com.ngxyzaudax.radiacode.protocol.AlarmSignalMode
import com.ngxyzaudax.radiacode.protocol.DeviceConfig

private const val REPEAT_MS: Long = 3_000

class AlarmAlertCoordinator(
    private val alerter: WatchAlerter,
) {
    private var activeEvents: Set<AlarmEvent> = emptySet()
    private var alertedEvents: Set<AlarmEvent> = emptySet()
    private var lastAlertMs: Long = 0L

    fun onReading(reading: LiveReading, config: DeviceConfig?): Unit {
        if (config == null || !reading.connected) {
            reset()
            return
        }
        val nextActive = activeAlarmEvents(reading, activeEvents)
        val rising = nextActive - activeEvents
        val cleared = activeEvents - nextActive
        activeEvents = nextActive
        alertedEvents = alertedEvents - cleared
        val now = System.currentTimeMillis()
        val repeatDue = config.alarmMode == AlarmSignalMode.Continuous &&
            nextActive.isNotEmpty() &&
            now - lastAlertMs >= REPEAT_MS
        val targets = when {
            rising.isNotEmpty() -> rising
            repeatDue -> nextActive
            else -> emptySet()
        }
        if (targets.isEmpty()) return
        targets.forEach { event -> alertEvent(event, config) }
        lastAlertMs = now
    }

    private fun alertEvent(event: AlarmEvent, config: DeviceConfig): Unit {
        if (config.alarmMode == AlarmSignalMode.Once && alertedEvents.contains(event)) return
        val sound = config.soundOn && isSoundEnabled(config.soundCtrl, event)
        val vibration = config.vibroOn && isVibrationEnabled(config.vibroCtrl, event)
        if (!sound && !vibration) return
        if (vibration) alerter.vibrate(isLevelTwo(event))
        if (sound) alerter.beep(isLevelTwo(event))
        if (config.alarmMode == AlarmSignalMode.Once) {
            alertedEvents = alertedEvents + event
        }
    }

    private fun reset(): Unit {
        activeEvents = emptySet()
        alertedEvents = emptySet()
        lastAlertMs = 0L
    }
}
