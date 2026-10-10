package com.ngxyzaudax.radiacode.track

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class RctrkReaderTest {
    @Test
    fun readTrackSummaryParsesDurationAndPoints(): Unit {
        val header = TrackHeader("2026-08-30 14-00-00", "RC-110", "walk")
        val points = listOf(
            trackPoint(1_756_550_400_000L),
            trackPoint(1_756_550_460_000L),
            trackPoint(1_756_550_520_000L),
        )
        val file = File.createTempFile("track", ".rctrk")
        file.writeText(buildRctrkContent(header, points))
        val summary = readTrackSummary(file)
        assertEquals("2026-08-30 14-00-00", summary.displayName)
        assertEquals(3, summary.pointCount)
        assertEquals(120L, summary.durationSec)
        file.delete()
    }

    private fun trackPoint(epochMillis: Long): TrackPoint =
        TrackPoint(
            epochMillis = epochMillis,
            latitude = 45.0,
            longitude = 19.0,
            altitude = 100.0,
            accuracy = 4f,
            temperatureC = 22f,
            doseRate = 0.1f,
            countRate = 10f,
        )
}
