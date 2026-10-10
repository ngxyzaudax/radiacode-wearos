package com.ngxyzaudax.radiacode.track

import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class TrackSummary(
    val displayName: String,
    val pointCount: Int,
    val durationSec: Long,
)

fun readTrackSummary(file: File): TrackSummary {
    val lines = runCatching { file.readLines() }.getOrDefault(emptyList())
    val headerName = parseTrackHeaderName(lines.firstOrNull()).ifBlank { file.nameWithoutExtension }
    val times = parsePointEpochMillis(lines)
    val durationSec = if (times.size < 2) {
        0L
    } else {
        ((times.last() - times.first()).coerceAtLeast(0L) / 1_000L)
    }
    return TrackSummary(
        displayName = headerName,
        pointCount = times.size,
        durationSec = durationSec,
    )
}

fun parseTrackHeaderName(headerLine: String?): String {
    if (headerLine.isNullOrBlank()) return ""
    val parts = headerLine.split('\t')
    if (parts.isEmpty() || !parts[0].startsWith("Track:")) return ""
    return parts.getOrNull(1)?.trim().orEmpty()
}

fun parsePointEpochMillis(lines: List<String>): List<Long> {
    if (lines.size < 3) return emptyList()
    val formatter = trackTimeFormatter()
    return lines.asSequence()
        .drop(2)
        .mapNotNull { line -> parsePointTimeMs(line, formatter) }
        .toList()
}

private fun parsePointTimeMs(line: String, formatter: SimpleDateFormat): Long? {
    val time = line.split('\t').getOrNull(1)?.trim().orEmpty()
    if (time.isEmpty()) return null
    return runCatching { formatter.parse(time)?.time }.getOrNull()
}

private fun trackTimeFormatter(): SimpleDateFormat =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }
