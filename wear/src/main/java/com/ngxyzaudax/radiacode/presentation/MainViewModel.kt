package com.ngxyzaudax.radiacode.presentation

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngxyzaudax.radiacode.RadiaCodeApp
import com.ngxyzaudax.radiacode.ble.DiscoveredDevice
import com.ngxyzaudax.radiacode.data.AppSettings
import com.ngxyzaudax.radiacode.data.LiveReading
import com.ngxyzaudax.radiacode.device.DeviceInfo
import com.ngxyzaudax.radiacode.device.MonitorState
import com.ngxyzaudax.radiacode.device.SpectrumState
import com.ngxyzaudax.radiacode.log.RadiaLog
import com.ngxyzaudax.radiacode.protocol.DeviceConfig
import com.ngxyzaudax.radiacode.service.MonitoringService
import com.ngxyzaudax.radiacode.track.listTrackFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File

private const val TRACK_FLUSH_DELAY_MS: Long = 1_000

class MainViewModel(private val app: RadiaCodeApp) : ViewModel() {
    private val component: String = "MainViewModel"
    private val binder = ServiceBinder(
        onConnected = { observeSession() },
        onDisconnected = { _bound.value = false },
    )
    private var observeJob: Job? = null
    private val _bound = MutableStateFlow(false)
    val bound: StateFlow<Boolean> = _bound.asStateFlow()

    val settings: StateFlow<AppSettings> = app.settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    val liveReading: StateFlow<LiveReading> = app.liveReadingStore.reading
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LiveReading())

    val deviceConfig: StateFlow<DeviceConfig?> = app.deviceConfigStore.loaded
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val pendingDeviceConfig: StateFlow<DeviceConfig?> = app.deviceConfigStore.pending
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _discovered = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val discovered: StateFlow<List<DiscoveredDevice>> = _discovered.asStateFlow()

    val scanState: StateFlow<ScanUiState> = app.scanUiStore.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScanUiState.Idle)

    private val _monitorState = MutableStateFlow<MonitorState?>(null)
    val monitorState: StateFlow<MonitorState?> = _monitorState.asStateFlow()

    private val _spectrumState = MutableStateFlow<SpectrumState?>(null)
    val spectrumState: StateFlow<SpectrumState?> = _spectrumState.asStateFlow()

    private val _deviceInfo = MutableStateFlow<DeviceInfo?>(null)
    val deviceInfo: StateFlow<DeviceInfo?> = _deviceInfo.asStateFlow()

    private val _tracks = MutableStateFlow<List<File>>(emptyList())
    val tracks: StateFlow<List<File>> = _tracks.asStateFlow()

    init {
        refreshTracks()
    }

    fun ensureService(context: Context): Boolean = binder.ensure(context)

    fun unbind(context: Context): Unit = binder.unbind(context)

    fun scan(context: Context): Unit {
        viewModelScope.launch { runScan(context) }
    }

    fun connect(device: DiscoveredDevice, context: Context): Unit {
        sendServiceAction(context, MonitoringService.ACTION_CONNECT) {
            putExtra(MonitoringService.EXTRA_ADDRESS, device.address)
            putExtra(MonitoringService.EXTRA_LABEL, device.label)
            putExtra(MonitoringService.EXTRA_MODEL, device.model)
            putExtra(MonitoringService.EXTRA_SERIAL, device.serial)
        }
    }

    fun disconnect(context: Context): Unit {
        sendServiceAction(context, MonitoringService.ACTION_DISCONNECT)
    }

    fun autoConnect(context: Context): Unit {
        if (!settings.value.autoReconnect) return
        sendServiceAction(context, MonitoringService.ACTION_AUTO_CONNECT)
    }

    fun startTrack(context: Context): Unit {
        viewModelScope.launch {
            if (prepareService(context)) {
                sendServiceAction(context, MonitoringService.ACTION_START_TRACK)
            }
        }
    }

    fun stopTrack(context: Context): Unit {
        sendServiceAction(context, MonitoringService.ACTION_STOP_TRACK)
        viewModelScope.launch {
            delay(TRACK_FLUSH_DELAY_MS)
            refreshTracks()
        }
    }

    fun refreshTracks(): Unit {
        viewModelScope.launch {
            _tracks.value = withContext(Dispatchers.IO) { readTracks() }
        }
    }

    fun trackFile(fileName: String): File? =
        _tracks.value.firstOrNull { it.name == fileName }
            ?: binder.service?.tracksDirectory()?.let { dir ->
                File(dir, fileName).takeIf { it.isFile }
            }

    fun deleteTrack(file: File): Unit {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                binder.service?.recorder?.deleteTrack(file) ?: file.delete()
            }
            refreshTracks()
        }
    }

    private fun readTracks(): List<File> = listTrackFiles(tracksDirectory())

    private fun tracksDirectory(): File =
        binder.service?.tracksDirectory() ?: File(app.filesDir, "tracks")

    fun updateSettings(transform: (AppSettings) -> AppSettings): Unit {
        viewModelScope.launch { app.settingsStore.update(transform) }
    }

    fun setPendingDeviceConfig(config: DeviceConfig): Unit {
        app.deviceConfigStore.setPending(config)
    }

    fun applyDeviceConfig(context: Context): Unit {
        sendServiceAction(context, MonitoringService.ACTION_APPLY_CONFIG)
    }

    fun loadDeviceConfig(context: Context): Unit {
        sendServiceAction(context, MonitoringService.ACTION_LOAD_CONFIG)
    }

    fun doseReset(context: Context): Unit {
        sendServiceAction(context, MonitoringService.ACTION_DOSE_RESET)
    }

    fun spectrumReset(context: Context): Unit {
        sendServiceAction(context, MonitoringService.ACTION_SPECTRUM_RESET)
    }

    fun syncClock(context: Context): Unit {
        sendServiceAction(context, MonitoringService.ACTION_SYNC_CLOCK)
    }

    private fun sendServiceAction(context: Context, action: String, block: (Intent.() -> Unit)? = null): Unit {
        viewModelScope.launch {
            if (!prepareService(context)) return@launch
            val intent = Intent(context, MonitoringService::class.java).setAction(action)
            block?.invoke(intent)
            context.startForegroundService(intent)
        }
    }

    private suspend fun runScan(context: Context): Unit {
        if (app.scanUiStore.state.value is ScanUiState.Scanning) return
        if (!DevicePermissions.hasAll(context)) {
            app.scanUiStore.update(ScanUiState.NeedsPermissions)
            return
        }
        app.scanUiStore.update(ScanUiState.Scanning)
        try {
            if (!prepareService(context)) {
                app.scanUiStore.update(ScanUiState.NeedsPermissions)
                return
            }
            val results = binder.service?.scanDevices() ?: emptyList()
            _discovered.value = results
            app.scanUiStore.update(ScanUiState.Finished(results.size))
        } catch (error: Exception) {
            RadiaLog.error(component, "Scan failed", error)
            app.scanUiStore.update(ScanUiState.Failed(error.message ?: "Scan failed"))
        }
    }

    private suspend fun prepareService(context: Context): Boolean {
        if (!DevicePermissions.hasAll(context)) return false
        if (binder.service != null) return true
        if (!binder.ensure(context)) return false
        withTimeout(8_000) { binder.awaitBinding() }
        delay(150)
        _bound.value = binder.service != null
        return binder.service != null
    }

    private fun observeSession(): Unit {
        observeJob?.cancel()
        val session = binder.service?.radiaCodeSession ?: return
        _bound.value = true
        refreshTracks()
        observeJob = viewModelScope.launch {
            launch { session.discovered.collect { _discovered.value = it } }
            launch { session.monitorState.collect { _monitorState.value = it } }
            launch { session.spectrumState.collect { _spectrumState.value = it } }
            launch { session.deviceInfo.collect { _deviceInfo.value = it } }
            launch {
                session.deviceConfig.collect { config ->
                    if (config != null) app.deviceConfigStore.setLoaded(config)
                }
            }
        }
    }
}
