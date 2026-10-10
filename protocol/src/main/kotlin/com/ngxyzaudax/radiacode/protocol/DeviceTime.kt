package com.ngxyzaudax.radiacode.protocol

import java.util.Calendar

fun buildSetTimePayload(epochMs: Long = System.currentTimeMillis()): ByteArray {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = epochMs
    return byteArrayOf(
        calendar.get(Calendar.DAY_OF_MONTH).toByte(),
        (calendar.get(Calendar.MONTH) + 1).toByte(),
        (calendar.get(Calendar.YEAR) - 2000).toByte(),
        0,
        calendar.get(Calendar.SECOND).toByte(),
        calendar.get(Calendar.MINUTE).toByte(),
        calendar.get(Calendar.HOUR_OF_DAY).toByte(),
        0,
    )
}
