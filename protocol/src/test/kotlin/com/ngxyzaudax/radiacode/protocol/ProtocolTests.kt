package com.ngxyzaudax.radiacode.protocol

import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RateUnitsTest {
    @Test
    fun doseSvConversion() {
        val rh = 0.000125f
        assertTrue(abs(doseDisplayFromRh(rh, DoseDisplayUnit.MicroSievertPerHour) - 1.25f) < 0.001f)
    }

    @Test
    fun countCpmConversion() {
        assertEquals(600f, countDisplayFromCps(10f, CountDisplayUnit.Cpm), 0.001f)
    }
}

class FramingTest {
    @Test
    fun buildRequestHasLengthPrefix() {
        val request = buildRequest(Command.SetExchange, 0x80, byteArrayOf(1, 0xFF.toByte(), 0x12, 0xFF.toByte()))
        assertEquals(12, request.size)
        assertEquals(8, request[0].toInt() and 0xFF)
    }

    @Test
    fun responseAssemblerCompletes() {
        val body = byteArrayOf(0x05, 0x00, 0x00, 0x80.toByte(), 0x01, 0x00, 0x00, 0x00)
        val framed = ByteArray(4 + body.size)
        framed[0] = body.size.toByte()
        body.copyInto(framed, 4)
        val assembler = ResponseAssembler()
        val result = assembler.push(framed)
        assertTrue(result != null)
        assertEquals(8, result!!.size)
    }
}

class DataBufTest {
    @Test
    fun decodeRealTimeRecord() {
        val payload = byteArrayOf(
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x34, 0x42,
            0x00, 0x00, 0x00, 0x00,
            0x0A, 0x00, 0x0A, 0x00, 0x00, 0x00, 0x00,
        )
        val frame = decodeDataBuf(payload)
        assertEquals(1, frame.realTimeRecords.size)
        assertEquals(45f, frame.realTimeRecords[0].countRateCps, 0.1f)
    }
}

class FwVersionTest {
    @Test
    fun decodeBootAndTarget() {
        fun asciiField(value: String): ByteArray =
            byteArrayOf(value.length.toByte()) + value.toByteArray(Charsets.US_ASCII)

        val payload = byteArrayOf(4, 0, 5, 0) +
            asciiField("boot-date") +
            byteArrayOf(8, 0, 4, 0) +
            asciiField("target-date")
        val versions = decodeFwVersion(payload)
        assertEquals(5, versions.boot.major)
        assertEquals(4, versions.boot.minor)
        assertEquals(4, versions.target.major)
        assertEquals(8, versions.target.minor)
        assertTrue(isCompatibleTargetFirmware(versions))
    }
}

