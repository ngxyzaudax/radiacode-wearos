package com.ngxyzaudax.radiacode.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.HorizontalPageIndicator
import androidx.wear.compose.material3.ScreenScaffold
import com.ngxyzaudax.radiacode.ble.DiscoveredDevice
import com.ngxyzaudax.radiacode.data.LiveReading
import com.ngxyzaudax.radiacode.device.DeviceInfo
import com.ngxyzaudax.radiacode.device.MonitorState
import com.ngxyzaudax.radiacode.device.SpectrumState
import com.ngxyzaudax.radiacode.presentation.ScanUiState
import java.io.File

@Composable
fun DashboardScreen(
    reading: LiveReading,
    monitorState: MonitorState?,
    spectrumState: SpectrumState?,
    deviceInfo: DeviceInfo?,
    discovered: List<DiscoveredDevice>,
    scanState: ScanUiState,
    tracks: List<File>,
    onOpenSettings: () -> Unit,
    onScan: () -> Unit,
    onConnect: (DiscoveredDevice) -> Unit,
    onDisconnect: () -> Unit,
    onStartTrack: () -> Unit,
    onStopTrack: () -> Unit,
    onOpenTrack: (File) -> Unit,
    onExportTracks: (() -> Unit)?,
    onResetDose: () -> Unit,
    onResetSpectrum: () -> Unit,
): Unit {
    if (!reading.connected) {
        ScreenScaffold {
            Box(modifier = Modifier.fillMaxSize()) {
                DeviceScreen(
                    devices = discovered,
                    scanState = scanState,
                    connected = false,
                    deviceInfo = deviceInfo,
                    reading = reading,
                    onScan = onScan,
                    onConnect = onConnect,
                    onDisconnect = onDisconnect,
                    onOpenSettings = onOpenSettings,
                    onExportTracks = onExportTracks,
                )
            }
        }
        return
    }
    val pagerState = rememberPagerState(pageCount = { 5 })
    ScreenScaffold {
        Box(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                when (page) {
                    0 -> MonitorScreen(
                        monitorState = monitorState,
                        reading = reading,
                        onOpenSettings = onOpenSettings,
                    )
                    1 -> AccumDoseScreen(
                        monitorState = monitorState,
                        reading = reading,
                        onResetDose = onResetDose,
                    )
                    2 -> SpectrumScreen(
                        state = spectrumState,
                        onResetSpectrum = onResetSpectrum,
                    )
                    3 -> TrackScreen(
                        reading = reading,
                        tracks = tracks,
                        onStart = onStartTrack,
                        onStop = onStopTrack,
                        onOpenTrack = onOpenTrack,
                        onExport = onExportTracks,
                    )
                    4 -> DeviceScreen(
                        devices = discovered,
                        scanState = scanState,
                        connected = reading.connected,
                        deviceInfo = deviceInfo,
                        reading = reading,
                        onScan = onScan,
                        onConnect = onConnect,
                        onDisconnect = onDisconnect,
                        onOpenSettings = onOpenSettings,
                    )
                }
            }
            HorizontalPageIndicator(
                pagerState = pagerState,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
            )
        }
    }
}
