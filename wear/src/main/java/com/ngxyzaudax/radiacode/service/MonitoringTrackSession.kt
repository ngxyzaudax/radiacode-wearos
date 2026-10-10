package com.ngxyzaudax.radiacode.service

import android.content.Context
import android.location.Location
import com.ngxyzaudax.radiacode.RadiaCodeApp
import com.ngxyzaudax.radiacode.data.LiveReading
import com.ngxyzaudax.radiacode.device.RadiaCodeSession
import com.ngxyzaudax.radiacode.gps.gpsCategory
import com.ngxyzaudax.radiacode.gps.gpsLabel
import com.ngxyzaudax.radiacode.gps.gpsStatus
import com.ngxyzaudax.radiacode.gps.isLocationAvailable
import com.ngxyzaudax.radiacode.log.RadiaLog
import com.ngxyzaudax.radiacode.tile.TileUpdatePusher
import com.ngxyzaudax.radiacode.track.TrackHeader
import com.ngxyzaudax.radiacode.track.TrackPoint
import com.ngxyzaudax.radiacode.track.TrackRecorder
import com.ngxyzaudax.radiacode.track.formatTrackName
import com.ngxyzaudax.radiacode.track.trackDoseRateMicroRoentgen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

private const val SAMPLE_INTERVAL_MS: Long = 1_000

internal class MonitoringTrackSession(
    private val context: Context,
    private val app: RadiaCodeApp,
    private val session: RadiaCodeSession,
    private val recorder: TrackRecorder,
    private val scope: CoroutineScope,
) {
    private val component: String = "MonitoringTrackSession"
    private var locationJob: Job? = null
    private var sampleJob: Job? = null
    private var latestTemperature: Float? = null
    private var lastGpsCategory: String? = null

    @Volatile
    private var latestLocation: Location? = null

    fun setTemperature(value: Float?): Unit {
        latestTemperature = value
    }

    fun tracksDirectory(context: Context): File = File(context.filesDir, "tracks")

    fun start(): Unit {
        scope.launch {
            RadiaLog.info(component, "Track recording start requested")
            val info = session.deviceInfo.first() ?: run {
                RadiaLog.warn(component, "Track start skipped device info unavailable")
                return@launch
            }
            recorder.start(trackHeader(info.serial, info.model))
            latestLocation = null
            app.liveReadingStore.update {
                it.copy(tracking = true, trackPointCount = 0, gpsAccuracyM = null, gpsFixAtMs = null)
            }
            observeLocation()
            observeSamples()
            publishGpsStatus()
            RadiaLog.info(component, "Track recording started intervalMs=$SAMPLE_INTERVAL_MS")
        }
    }

    fun stop(context: Context): Unit {
        scope.launch {
            RadiaLog.info(component, "Track recording stop requested")
            sampleJob?.cancel()
            sampleJob = null
            locationJob?.cancel()
            locationJob = null
            latestLocation = null
            val result = recorder.stop() ?: run {
                RadiaLog.warn(component, "Track stop skipped no active recording")
                return@launch
            }
            val saved = recorder.saveTrack(result.first, result.second, tracksDirectory(context))
            app.liveReadingStore.update { it.copy(tracking = false, gpsAccuracyM = null, gpsFixAtMs = null) }
            publishGpsStatus()
            RadiaLog.info(component, "Track saved path=${saved.absolutePath} points=${result.second.size}")
        }
    }

    private fun trackHeader(serial: String, model: String): TrackHeader =
        TrackHeader(
            name = formatTrackName(System.currentTimeMillis()),
            serialNumber = serial.ifBlank { model },
            comment = model,
        )

    /** Location updates only refresh the cached fix; sampling cadence is owned by [observeSamples]. */
    private fun observeLocation(): Unit {
        locationJob?.cancel()
        locationJob = scope.launch {
            recorder.locationFlow().collect { location ->
                latestLocation = location
                app.liveReadingStore.update {
                    it.copy(gpsAccuracyM = location.accuracy, gpsFixAtMs = fixTime(location))
                }
            }
        }
    }

    private fun observeSamples(): Unit {
        sampleJob?.cancel()
        sampleJob = scope.launch {
            while (isActive) {
                recordSample(latestLocation, app.liveReadingStore.reading.value)
                publishGpsStatus()
                delay(SAMPLE_INTERVAL_MS)
            }
        }
    }

    private fun recordSample(location: Location?, reading: LiveReading): Unit {
        if (location == null || !reading.connected) return
        recorder.addPoint(trackPoint(location, reading))
        app.liveReadingStore.update { it.copy(trackPointCount = recorder.pointCount()) }
    }

    private fun publishGpsStatus(): Unit {
        val reading = app.liveReadingStore.reading.value
        val status = gpsStatus(reading, isLocationAvailable(context), System.currentTimeMillis())
        val category = gpsCategory(status)
        if (category == lastGpsCategory) return
        lastGpsCategory = category
        RadiaLog.info(component, "GPS status category=$category label=${gpsLabel(status)}")
        TileUpdatePusher.pushTrack(context)
    }

    private fun trackPoint(location: Location, reading: LiveReading): TrackPoint =
        TrackPoint(
            epochMillis = System.currentTimeMillis(),
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = location.altitude,
            accuracy = location.accuracy,
            temperatureC = latestTemperature,
            doseRate = trackDoseRateMicroRoentgen(reading.doseRate, session.doseUnit),
            countRate = reading.countRate,
        )
}

private fun fixTime(location: Location): Long =
    location.time.takeIf { time -> time > 0L } ?: System.currentTimeMillis()
