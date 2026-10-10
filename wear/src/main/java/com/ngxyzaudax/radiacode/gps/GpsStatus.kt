package com.ngxyzaudax.radiacode.gps

import androidx.compose.ui.graphics.Color
import com.ngxyzaudax.radiacode.data.LiveReading
import com.ngxyzaudax.radiacode.presentation.theme.MetricPalette

const val GPS_STALE_MS: Long = 10_000
const val GPS_WEAK_ACCURACY_M: Float = 20f

sealed interface GpsStatus {
    data object Off : GpsStatus
    data object Ready : GpsStatus
    data object Searching : GpsStatus
    data class Weak(val accuracyM: Float) : GpsStatus
    data class Locked(val accuracyM: Float) : GpsStatus
}

fun gpsStatus(reading: LiveReading, locationEnabled: Boolean, nowMs: Long): GpsStatus {
    if (!locationEnabled) return GpsStatus.Off
    if (!reading.tracking) return GpsStatus.Ready
    return trackingStatus(reading, nowMs)
}

fun gpsLabel(status: GpsStatus): String = when (status) {
    GpsStatus.Off -> "GPS off"
    GpsStatus.Ready -> "GPS ready"
    GpsStatus.Searching -> "Searching GPS"
    is GpsStatus.Weak -> "GPS weak ${accuracyLabel(status.accuracyM)}"
    is GpsStatus.Locked -> "GPS ${accuracyLabel(status.accuracyM)}"
}

fun gpsColor(status: GpsStatus): Color = when (status) {
    GpsStatus.Off -> MetricPalette.danger
    GpsStatus.Ready -> MetricPalette.muted
    GpsStatus.Searching -> MetricPalette.warn
    is GpsStatus.Weak -> MetricPalette.warn
    is GpsStatus.Locked -> MetricPalette.safe
}

fun gpsCategory(status: GpsStatus): String = when (status) {
    GpsStatus.Off -> "off"
    GpsStatus.Ready -> "ready"
    GpsStatus.Searching -> "searching"
    is GpsStatus.Weak -> "weak"
    is GpsStatus.Locked -> "locked"
}

fun trackHint(reading: LiveReading, status: GpsStatus): String = when {
    !reading.connected -> "Device disconnected"
    status is GpsStatus.Off -> "Turn on location"
    status is GpsStatus.Searching -> "Waiting for fix"
    reading.tracking -> "Tap stop to finish"
    else -> "Tap start to record"
}

private fun trackingStatus(reading: LiveReading, nowMs: Long): GpsStatus {
    val fixAt = reading.gpsFixAtMs
    val accuracy = reading.gpsAccuracyM
    if (fixAt == null || accuracy == null || nowMs - fixAt > GPS_STALE_MS) return GpsStatus.Searching
    if (accuracy > GPS_WEAK_ACCURACY_M) return GpsStatus.Weak(accuracy)
    return GpsStatus.Locked(accuracy)
}

private fun accuracyLabel(accuracyM: Float): String = "±${accuracyM.toInt()} m"
