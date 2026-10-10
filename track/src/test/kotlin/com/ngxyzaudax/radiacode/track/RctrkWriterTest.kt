package com.ngxyzaudax.radiacode.track

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RctrkWriterTest {
    @Test
    fun fileTimeMatchesSampleTimestamp() {
        val sampleFileTime = 134317223014850000L
        val epochMillis = sampleFileTime / 10_000L - 11_644_473_600_000L
        assertEquals(sampleFileTime, epochToFileTime(epochMillis))
    }

    @Test
    fun buildRctrkHasExpectedColumns() {
        val header = TrackHeader("2026-08-20 19-58-13", "RC-110", "Eržix")
        val points = listOf(
            TrackPoint(
                epochMillis = 1_756_112_301_000L,
                latitude = 45.7011064,
                longitude = 19.3814973,
                altitude = 132.0,
                accuracy = 3.79f,
                temperatureC = 29.7f,
                doseRate = 7.57f,
                countRate = 11.3f,
            ),
        )
        val content = buildRctrkContent(header, points)
        assertTrue(content.startsWith("Track:\t"))
        assertTrue(content.contains("Timestamp\tTime\tLatitude\tLongitude\tAltitude\tAccuracy\tTemperature\tDoseRate\tCountRate\tComment"))
        assertTrue(content.contains("45.7011064"))
        assertTrue(content.contains("7.57"))
    }

    @Test
    fun doseRateUnitMatchesMicroRoentgenPerHourFromSample() {
        val doseRateRh = 0.00000757f
        val microRoentgen = com.ngxyzaudax.radiacode.protocol.doseDisplayFromRh(
            doseRateRh,
            com.ngxyzaudax.radiacode.protocol.DoseDisplayUnit.MicroRoentgenPerHour,
        )
        assertEquals(7.57f, microRoentgen, 0.01f)
    }
}
