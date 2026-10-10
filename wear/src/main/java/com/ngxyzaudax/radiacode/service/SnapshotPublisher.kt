package com.ngxyzaudax.radiacode.service

import com.ngxyzaudax.radiacode.RadiaCodeApp
import com.ngxyzaudax.radiacode.data.LiveReading
import com.ngxyzaudax.radiacode.presentation.theme.doseRateScale
import com.ngxyzaudax.radiacode.presentation.theme.metricFraction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val HISTORY_LIMIT: Int = 24
private const val SEVERITY_HISTORY_LIMIT: Int = 7

internal class SnapshotPublisher(
    private val app: RadiaCodeApp,
    private val scope: CoroutineScope,
) {
    fun publish(reading: LiveReading): Unit {
        scope.launch { app.readingSnapshotStore.save(reading) }
    }

    fun appendSeverityHistory(reading: LiveReading): LiveReading {
        val fraction = metricFraction(reading.doseRate, doseRateScale(reading.alarmLimits))
        val severityHistory = (reading.severityHistory + fraction).takeLast(SEVERITY_HISTORY_LIMIT)
        val doseRateHistory = appendRateHistory(reading.doseRateHistory, reading.doseRate)
        val countRateHistory = appendRateHistory(reading.countRateHistory, reading.countRate)
        return reading.copy(
            severityHistory = severityHistory,
            doseRateHistory = doseRateHistory,
            countRateHistory = countRateHistory,
        )
    }

    private fun appendRateHistory(existing: List<Float>, value: Float): List<Float> {
        if (value <= 0f) return existing
        return (existing + value).takeLast(HISTORY_LIMIT)
    }
}
