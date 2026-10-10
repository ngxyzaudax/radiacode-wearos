package com.ngxyzaudax.radiacode.track

data class TrackPoint(
    val epochMillis: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val accuracy: Float,
    val temperatureC: Float?,
    val doseRate: Float,
    val countRate: Float,
    val comment: String = " ",
)

data class TrackHeader(
    val name: String,
    val serialNumber: String,
    val comment: String,
    val flags: String = "EC",
)

fun epochToFileTime(epochMillis: Long): Long =
    (epochMillis + 11_644_473_600_000L) * 10_000L

fun formatTrackTime(epochMillis: Long): String {
    val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
    formatter.timeZone = java.util.TimeZone.getDefault()
    return formatter.format(java.util.Date(epochMillis))
}

fun formatTrackName(epochMillis: Long): String {
    val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH-mm-ss", java.util.Locale.US)
    formatter.timeZone = java.util.TimeZone.getDefault()
    return formatter.format(java.util.Date(epochMillis))
}

fun buildRctrkContent(header: TrackHeader, points: List<TrackPoint>): String {
    val headerLine = listOf("Track:", header.name, header.serialNumber, header.comment, header.flags)
        .joinToString("\t")
    val columnsLine = "Timestamp\tTime\tLatitude\tLongitude\tAltitude\tAccuracy\tTemperature\tDoseRate\tCountRate\tComment"
    val rows = points.map { point ->
        listOf(
            epochToFileTime(point.epochMillis).toString(),
            formatTrackTime(point.epochMillis),
            point.latitude.toString(),
            point.longitude.toString(),
            point.altitude.toInt().toString(),
            String.format(java.util.Locale.US, "%.2f", point.accuracy),
            point.temperatureC?.let { String.format(java.util.Locale.US, "%.1f", it) } ?: "",
            String.format(java.util.Locale.US, "%.2f", point.doseRate),
            String.format(java.util.Locale.US, "%.1f", point.countRate),
            point.comment,
        ).joinToString("\t")
    }
    return (listOf(headerLine, columnsLine) + rows).joinToString("\n")
}
