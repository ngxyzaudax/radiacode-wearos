package com.ngxyzaudax.radiacode.protocol

import java.util.Calendar
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class DeviceTimeTest {
    @Test
    fun setTimePayloadUsesCalendarFields() {
        val calendar = Calendar.getInstance()
        calendar.set(2026, Calendar.AUGUST, 30, 13, 45, 7)
        calendar.set(Calendar.MILLISECOND, 0)
        val payload = buildSetTimePayload(calendar.timeInMillis)
        assertArrayEquals(
            byteArrayOf(30, 8, 26, 0, 7, 45, 13, 0),
            payload,
        )
    }
}
