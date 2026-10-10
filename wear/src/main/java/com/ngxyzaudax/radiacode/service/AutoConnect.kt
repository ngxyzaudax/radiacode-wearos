package com.ngxyzaudax.radiacode.service

import com.ngxyzaudax.radiacode.ble.DiscoveredDevice
import com.ngxyzaudax.radiacode.data.KnownDevice
import com.ngxyzaudax.radiacode.device.RadiaCodeSession
import com.ngxyzaudax.radiacode.log.RadiaLog

internal object AutoConnect {
    private const val component: String = "AutoConnect"

    suspend fun tryConnect(session: RadiaCodeSession, devices: List<KnownDevice>): KnownDevice? {
        for (device in devices.sortedByDescending { it.lastConnectedEpochMs }) {
            RadiaLog.info(component, "Attempting known device name=${device.displayName()} address=${device.address}")
            val target = DiscoveredDevice(
                address = device.address,
                label = device.label,
                serial = null,
                model = device.model,
                rssi = null,
            )
            val result = runCatching { session.connect(target) }
            if (result.isSuccess) {
                RadiaLog.info(component, "Connected to known device name=${device.displayName()}")
                return device
            }
            RadiaLog.warn(component, "Known device connect failed name=${device.displayName()} ${result.exceptionOrNull()?.message}")
        }
        return null
    }
}
