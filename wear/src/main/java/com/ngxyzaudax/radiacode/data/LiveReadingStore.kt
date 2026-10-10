package com.ngxyzaudax.radiacode.data

import com.ngxyzaudax.radiacode.protocol.AlarmLimits
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LiveReading(
    val doseRate: Float = 0f,
    val countRate: Float = 0f,
    val doseLabel: String = "µSv/h",
    val countLabel: String = "CPS",
    val accumulatedDose: Float = 0f,
    val accumulatedDurationSec: Long = 0L,
    val connected: Boolean = false,
    val deviceLabel: String = "",
    val tracking: Boolean = false,
    val trackPointCount: Int = 0,
    val batteryPercent: Float? = null,
    val temperatureC: Float? = null,
    val rssiDbm: Int? = null,
    val gpsAccuracyM: Float? = null,
    val gpsFixAtMs: Long? = null,
    val alarmLimits: AlarmLimits = AlarmLimits(),
    val severityHistory: List<Float> = emptyList(),
    val doseRateHistory: List<Float> = emptyList(),
    val countRateHistory: List<Float> = emptyList(),
)

class LiveReadingStore {
    private val _reading = MutableStateFlow(LiveReading())
    val reading: StateFlow<LiveReading> = _reading.asStateFlow()

    fun update(transform: (LiveReading) -> LiveReading): Unit {
        _reading.value = transform(_reading.value)
    }
}
