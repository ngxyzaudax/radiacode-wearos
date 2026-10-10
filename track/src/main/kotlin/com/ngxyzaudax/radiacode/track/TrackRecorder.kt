package com.ngxyzaudax.radiacode.track

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File
import java.util.concurrent.Executors

const val LOCATION_INTERVAL_MS: Long = 1_000
private const val LOCATION_MIN_INTERVAL_MS: Long = 500
private const val LOCATION_THREAD_NAME: String = "radiacode-location"

class TrackRecorder(private val context: Context) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, LOCATION_THREAD_NAME).apply { isDaemon = true }
    }
    private val points = mutableListOf<TrackPoint>()
    private var startedAtMs: Long = 0
    private var header: TrackHeader? = null

    val isRecording: Boolean get() = header != null

    fun start(header: TrackHeader): Unit {
        this.header = header
        startedAtMs = System.currentTimeMillis()
        points.clear()
    }

    fun stop(): Pair<TrackHeader, List<TrackPoint>>? {
        val currentHeader = header ?: return null
        val result = currentHeader to points.toList()
        header = null
        points.clear()
        return result
    }

    fun addPoint(point: TrackPoint): Unit {
        if (header != null) points.add(point)
    }

    fun pointCount(): Int = points.size

    @SuppressLint("MissingPermission")
    fun locationFlow(intervalMs: Long = LOCATION_INTERVAL_MS): Flow<Location> = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it) }
            }
        }
        fusedClient.requestLocationUpdates(highAccuracyRequest(intervalMs), locationExecutor, callback)
        awaitClose { fusedClient.removeLocationUpdates(callback) }
    }

    private fun highAccuracyRequest(intervalMs: Long): LocationRequest =
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(LOCATION_MIN_INTERVAL_MS)
            .setMaxUpdateDelayMillis(0)
            .setGranularity(Granularity.GRANULARITY_FINE)
            .setWaitForAccurateLocation(false)
            .setMinUpdateDistanceMeters(0f)
            .build()

    fun saveTrack(header: TrackHeader, points: List<TrackPoint>, directory: File): File {
        directory.mkdirs()
        val fileName = "${formatTrackName(if (startedAtMs == 0L) System.currentTimeMillis() else startedAtMs)}.rctrk"
        val file = File(directory, fileName)
        file.writeText(buildRctrkContent(header, points))
        return file
    }

    fun listTracks(directory: File): List<File> = listTrackFiles(directory)

    fun deleteTrack(file: File): Boolean =
        file.exists() && file.extension.equals("rctrk", ignoreCase = true) && file.delete()
}

fun listTrackFiles(directory: File): List<File> =
    directory.listFiles { file -> file.isFile && file.extension.equals("rctrk", ignoreCase = true) }
        ?.sortedByDescending { it.lastModified() }
        ?: emptyList()
