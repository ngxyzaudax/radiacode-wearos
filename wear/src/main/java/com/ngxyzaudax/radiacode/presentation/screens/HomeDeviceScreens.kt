package com.ngxyzaudax.radiacode.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.ngxyzaudax.radiacode.ble.DiscoveredDevice
import com.ngxyzaudax.radiacode.ble.displayName
import com.ngxyzaudax.radiacode.data.LiveReading
import com.ngxyzaudax.radiacode.device.DeviceInfo
import com.ngxyzaudax.radiacode.presentation.ScanUiState
import com.ngxyzaudax.radiacode.presentation.components.BodyMetric
import com.ngxyzaudax.radiacode.presentation.components.ScreenActionButton
import com.ngxyzaudax.radiacode.presentation.components.ScreenHeader

@Composable
fun DeviceScreen(
    devices: List<DiscoveredDevice>,
    scanState: ScanUiState,
    connected: Boolean,
    deviceInfo: DeviceInfo?,
    reading: LiveReading,
    onScan: () -> Unit,
    onConnect: (DiscoveredDevice) -> Unit,
    onDisconnect: () -> Unit,
    onOpenSettings: () -> Unit,
    onExportTracks: (() -> Unit)? = null,
): Unit {
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val scanLabel = scanLabelFor(scanState)
    val statusText = statusTextFor(scanState, devices)
    ScreenScaffold(scrollState = listState) { padding ->
        TransformingLazyColumn(state = listState, contentPadding = padding) {
            item { ScreenHeader(this, transformationSpec, "Device") }
            if (connected) {
                item {
                    DeviceStats(
                        reading = reading,
                        deviceInfo = deviceInfo,
                        modifier = Modifier.transformedHeight(this, transformationSpec),
                    )
                }
            }
            item {
                ScreenActionButton(
                    this,
                    transformationSpec,
                    scanLabel,
                    if (scanState is ScanUiState.Scanning || scanState is ScanUiState.Connecting) ({}) else onScan,
                    emphasized = scanState !is ScanUiState.Scanning && scanState !is ScanUiState.Connecting,
                )
            }
            if (statusText != null) {
                item { BodyMetric(this, transformationSpec, statusText) }
            }
            if (onExportTracks != null) {
                item { ScreenActionButton(this, transformationSpec, "Export tracks", onExportTracks) }
            }
            item { ScreenActionButton(this, transformationSpec, "Settings", onOpenSettings) }
            if (connected) {
                item { ScreenActionButton(this, transformationSpec, "Disconnect", onDisconnect) }
            }
            if (!connected) {
                items(devices) { device ->
                    ScreenActionButton(
                        this,
                        transformationSpec,
                        device.displayName(),
                        { onConnect(device) },
                    )
                }
            }
        }
    }
}

private fun scanLabelFor(scanState: ScanUiState): String = when (scanState) {
    ScanUiState.Idle -> "Scan devices"
    ScanUiState.Scanning -> "Scanning..."
    is ScanUiState.Connecting -> "Connecting..."
    ScanUiState.NeedsPermissions -> "Permissions required"
    is ScanUiState.Finished -> if (scanState.count == 0) "Scan again" else "Scan again (${scanState.count} found)"
    is ScanUiState.Failed -> "Scan failed"
}

private fun statusTextFor(scanState: ScanUiState, devices: List<DiscoveredDevice>): String? = when (scanState) {
    ScanUiState.Idle -> if (devices.isEmpty()) "Tap scan to find devices" else null
    ScanUiState.Scanning -> "Searching nearby..."
    is ScanUiState.Connecting -> "Connecting to ${scanState.deviceName}..."
    ScanUiState.NeedsPermissions -> "Grant Bluetooth and location"
    is ScanUiState.Finished -> if (scanState.count == 0) "No RadiaCode found nearby" else null
    is ScanUiState.Failed -> scanState.message
}

