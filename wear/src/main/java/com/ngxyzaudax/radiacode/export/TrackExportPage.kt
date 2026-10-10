package com.ngxyzaudax.radiacode.export

import android.net.Uri
import com.ngxyzaudax.radiacode.format.formatExposureDuration
import com.ngxyzaudax.radiacode.track.readTrackSummary
import java.io.File

fun renderTrackExportPage(tracks: List<File>): String = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>RadiaCode tracks</title>
<style>
body{margin:0;padding:24px;background:#101418;color:#e6e1e5;font-family:sans-serif}
a{color:#9ecaff}
li{margin:16px 0}
</style>
</head>
<body>
<h1>RadiaCode tracks</h1>
${downloadAllLink(tracks)}
<ul>
${tracks.joinToString("\n") { file -> trackListItem(file) }}
</ul>
<p>Leave the export screen open on the watch until the download finishes.</p>
</body>
</html>
""".trimIndent()

private fun downloadAllLink(tracks: List<File>): String =
    if (tracks.isEmpty()) "<p>No tracks saved on the watch.</p>"
    else "<p><a href=\"/tracks.zip\">Download all</a></p>"

private fun trackListItem(file: File): String {
    val summary = readTrackSummary(file)
    val href = "/tracks/${Uri.encode(file.name)}"
    val duration = formatExposureDuration(summary.durationSec)
    return "<li><a href=\"$href\">${escapeHtml(summary.displayName)}</a><br>" +
        "${summary.pointCount} points, $duration, ${formatByteSize(file.length())}</li>"
}

private fun escapeHtml(value: String): String = buildString(value.length) {
    value.forEach { char -> append(htmlReplacement(char)) }
}

private fun htmlReplacement(char: Char): String = when (char) {
    '&' -> "&amp;"
    '<' -> "&lt;"
    '>' -> "&gt;"
    '"' -> "&quot;"
    else -> char.toString()
}

private fun formatByteSize(bytes: Long): String = when {
    bytes < 1024L -> "$bytes B"
    bytes < 1024L * 1024L -> "${bytes / 1024L} KB"
    else -> "${bytes / (1024L * 1024L)} MB"
}
