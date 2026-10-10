package com.ngxyzaudax.radiacode.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ngxyzaudax.radiacode.ble.DiscoveredDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.knownDevicesStore by preferencesDataStore("known_devices")

data class KnownDevice(
    val address: String,
    val label: String,
    val model: String?,
    val lastConnectedEpochMs: Long,
) {
    fun displayName(): String = label
}

class KnownDeviceStore(private val context: Context) {
    val devices: Flow<List<KnownDevice>> =
        context.knownDevicesStore.data.map { prefs -> decode(prefs[Keys.devices] ?: "") }

    suspend fun list(): List<KnownDevice> = devices.first()

    suspend fun remember(device: DiscoveredDevice): Unit {
        val entry = KnownDevice(
            address = device.address,
            label = device.label,
            model = device.model,
            lastConnectedEpochMs = System.currentTimeMillis(),
        )
        context.knownDevicesStore.edit { prefs ->
            val next = (decode(prefs[Keys.devices] ?: "") + entry)
                .distinctBy { it.address }
                .sortedByDescending { it.lastConnectedEpochMs }
                .take(8)
            prefs[Keys.devices] = encode(next)
        }
    }

    private object Keys {
        val devices = stringPreferencesKey("devices")
    }
}

private fun encode(devices: List<KnownDevice>): String =
    devices.joinToString("\n") { "${it.address}\t${it.label}\t${it.model ?: ""}\t${it.lastConnectedEpochMs}" }

private fun decode(raw: String): List<KnownDevice> =
    raw.lineSequence()
        .mapNotNull { line ->
            val parts = line.split('\t')
            if (parts.size < 4) return@mapNotNull null
            KnownDevice(
                address = parts[0],
                label = parts[1],
                model = parts[2].ifBlank { null },
                lastConnectedEpochMs = parts[3].toLongOrNull() ?: 0L,
            )
        }
        .toList()
