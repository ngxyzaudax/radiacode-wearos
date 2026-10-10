package com.ngxyzaudax.radiacode.complication

import android.content.Context
import com.ngxyzaudax.radiacode.RadiaCodeApp
import com.ngxyzaudax.radiacode.data.LiveReading
import com.ngxyzaudax.radiacode.data.ReadingSnapshot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

internal object ComplicationReading {
    fun load(context: Context): LiveReading {
        val app = context.applicationContext as RadiaCodeApp
        val live = app.liveReadingStore.reading.value
        if (hasLiveData(live)) return live
        return runBlocking {
            app.readingSnapshotStore.snapshot.first().toLiveReading()
        }
    }

    private fun hasLiveData(reading: LiveReading): Boolean =
        reading.connected ||
            reading.countRate > 0f ||
            reading.doseRate > 0f ||
            reading.batteryPercent != null ||
            reading.temperatureC != null
}

private fun ReadingSnapshot.toLiveReading(): LiveReading = LiveReading(
    doseRate = doseRate,
    countRate = countRate,
    doseLabel = doseLabel,
    countLabel = countLabel,
    accumulatedDose = accumulatedDose,
    accumulatedDurationSec = accumulatedDurationSec,
    connected = connected,
    deviceLabel = deviceLabel,
    tracking = tracking,
    trackPointCount = trackPointCount,
    batteryPercent = batteryPercent,
    temperatureC = temperatureC,
    rssiDbm = rssiDbm,
    alarmLimits = alarmLimits,
    severityHistory = severityHistory,
    doseRateHistory = doseRateHistory,
    countRateHistory = countRateHistory,
)
