package com.ngxyzaudax.radiacode.ble

import java.util.UUID

object BleUuids {
    val service: UUID = UUID.fromString("e63215e5-7003-49d8-96b0-b024798fb901")
    val write: UUID = UUID.fromString("e63215e6-7003-49d8-96b0-b024798fb901")
    val notify: UUID = UUID.fromString("e63215e7-7003-49d8-96b0-b024798fb901")
    const val chunkSize: Int = 18
    const val responseTimeoutMs: Long = 30_000
    const val linkSettleMs: Long = 250
    const val settleDrainMs: Long = 500
    const val commandDrainMs: Long = 400
    const val quietGapMs: Long = 120
}

data class DiscoveredDevice(
    val address: String,
    val label: String,
    val serial: String?,
    val model: String?,
    val rssi: Int?,
)

fun DiscoveredDevice.displayName(): String = label

fun isRadiacodeAdvertisement(name: String?, serviceUuids: List<UUID>): Boolean {
    val nameMatch = name?.lowercase()?.contains("radiacode") == true
    val serviceMatch = serviceUuids.any { it == BleUuids.service }
    return nameMatch || serviceMatch
}

fun labelFromAdvertisement(name: String?): String =
    name?.substringBefore('#')?.takeIf { it.isNotBlank() } ?: name ?: "RadiaCode"

fun serialFromAdvertisement(name: String?): String? {
    val detail = name?.substringAfter('#', "")?.takeIf { it.isNotBlank() } ?: return null
    return detail.substringAfterLast('-').takeIf { it.isNotBlank() }
}

fun modelFromAdvertisement(name: String?): String? {
    val detail = name?.substringAfter('#', "")?.takeIf { it.isNotBlank() }
    if (detail != null) {
        return detail.substringBefore('-').takeIf { it.isNotBlank() }
    }
    return name?.substringBefore('-')?.takeIf { it.startsWith("RC", ignoreCase = true) }
}
