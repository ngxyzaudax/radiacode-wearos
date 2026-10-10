package com.ngxyzaudax.radiacode.protocol

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class AlarmUnitsTest {
    @Test
    fun doseSvRoundTrip() {
        val display = 1.25f
        val raw = encodeDoseAlarm(display, DoseDisplayUnit.MicroSievertPerHour)
        assertEquals(125, raw)
        assertTrue(abs(decodeDoseAlarm(raw, DoseDisplayUnit.MicroSievertPerHour) - display) < 0.01f)
    }

    @Test
    fun doseRRoundTrip() {
        val display = 125f
        val raw = encodeDoseAlarm(display, DoseDisplayUnit.MicroRoentgenPerHour)
        assertEquals(125, raw)
        assertTrue(abs(decodeDoseAlarm(raw, DoseDisplayUnit.MicroRoentgenPerHour) - display) < 0.01f)
    }

    @Test
    fun countCpsRoundTrip() {
        val display = 42f
        val raw = encodeCountAlarm(display, CountDisplayUnit.Cps)
        assertEquals(420, raw)
        assertTrue(abs(decodeCountAlarm(raw, CountDisplayUnit.Cps) - display) < 0.01f)
    }

    @Test
    fun countCpmRoundTrip() {
        val display = 600f
        val raw = encodeCountAlarm(display, CountDisplayUnit.Cpm)
        assertEquals(100, raw)
        assertTrue(abs(decodeCountAlarm(raw, CountDisplayUnit.Cpm) - display) < 0.01f)
    }
}
