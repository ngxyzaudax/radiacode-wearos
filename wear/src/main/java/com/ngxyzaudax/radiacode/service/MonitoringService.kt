package com.ngxyzaudax.radiacode.service

import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.ngxyzaudax.radiacode.RadiaCodeApp
import com.ngxyzaudax.radiacode.alert.AlarmAlertCoordinator
import com.ngxyzaudax.radiacode.alert.WatchAlerter
import com.ngxyzaudax.radiacode.ble.BleTransport
import com.ngxyzaudax.radiacode.ble.DiscoveredDevice
import com.ngxyzaudax.radiacode.complication.ComplicationUpdatePusher
import com.ngxyzaudax.radiacode.device.RadiaCodeSession
import com.ngxyzaudax.radiacode.log.RadiaLog
import com.ngxyzaudax.radiacode.presentation.DevicePermissions
import com.ngxyzaudax.radiacode.presentation.ScanUiState
import com.ngxyzaudax.radiacode.protocol.AlarmLimits
import com.ngxyzaudax.radiacode.protocol.countUnitLabel
import com.ngxyzaudax.radiacode.protocol.doseUnitLabel
import com.ngxyzaudax.radiacode.tile.TileUpdatePusher
import com.ngxyzaudax.radiacode.track.TrackRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import java.io.File

class MonitoringService : LifecycleService() {
    private val component: String = "MonitoringService"
    private val binder = LocalBinder()
    private val deviceScope: CoroutineScope by lazy { lifecycleScope + Dispatchers.IO }
    private val workScope: CoroutineScope by lazy { lifecycleScope + Dispatchers.Default }
    private lateinit var transport: BleTransport
    private lateinit var session: RadiaCodeSession
    private lateinit var trackRecorder: TrackRecorder
    private lateinit var trackSession: MonitoringTrackSession
    private lateinit var snapshotPublisher: SnapshotPublisher
    private lateinit var alarmCoordinator: AlarmAlertCoordinator
    private var foregroundStarted: Boolean = false
    private var autoReconnectEnabled: Boolean = true
    private var userDisconnected: Boolean = false
    private var autoConnectJob: Job? = null
    private var wasConnected: Boolean = false

    override fun onCreate() {
        super.onCreate()
        RadiaLog.info(component, "Service created")
        val app = application as RadiaCodeApp
        transport = BleTransport(this)
        session = RadiaCodeSession(transport, deviceScope)
        trackRecorder = TrackRecorder(this)
        trackSession = MonitoringTrackSession(this, app, session, trackRecorder, deviceScope)
        snapshotPublisher = SnapshotPublisher(app, deviceScope)
        alarmCoordinator = AlarmAlertCoordinator(WatchAlerter(this))
        MonitoringNotification.createChannel(this)
        observeSettings(app)
        observeMonitor(app)
        observeConnection(app)
        scheduleAutoConnect()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_CONNECT -> deviceScope.launch {
                userDisconnected = false
                autoConnectJob?.cancel()
                autoConnectJob = null
                val address = intent.getStringExtra(EXTRA_ADDRESS) ?: return@launch
                val label = intent.getStringExtra(EXTRA_LABEL) ?: "RadiaCode"
                val model = intent.getStringExtra(EXTRA_MODEL)
                val serial = intent.getStringExtra(EXTRA_SERIAL)
                val app = application as RadiaCodeApp
                app.scanUiStore.update(ScanUiState.Connecting(label))
                runCatching {
                    connect(DiscoveredDevice(address, label, serial, model, null))
                }.onSuccess {
                    RadiaLog.info(component, "Connect succeeded label=$label")
                    app.scanUiStore.update(ScanUiState.Idle)
                }.onFailure { error ->
                    RadiaLog.error(component, "Connect failed", error)
                    app.scanUiStore.update(ScanUiState.Failed(error.message ?: "Connect failed"))
                }
            }
            ACTION_DISCONNECT -> deviceScope.launch {
                userDisconnected = true
                autoConnectJob?.cancel()
                session.disconnect()
                (application as RadiaCodeApp).scanUiStore.update(ScanUiState.Idle)
            }
            ACTION_AUTO_CONNECT -> scheduleAutoConnect()
            ACTION_START_TRACK -> trackSession.start()
            ACTION_STOP_TRACK -> trackSession.stop(this)
            ACTION_LOAD_CONFIG -> deviceScope.launch { loadConfig() }
            ACTION_APPLY_CONFIG -> deviceScope.launch { applyPendingConfig() }
            ACTION_DOSE_RESET -> deviceScope.launch { session.doseReset() }
            ACTION_SPECTRUM_RESET -> deviceScope.launch { session.spectrumReset() }
            ACTION_SYNC_CLOCK -> deviceScope.launch { session.syncClock() }
        }
        promoteToForegroundIfAllowed()
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    inner class LocalBinder : android.os.Binder() {
        val service: MonitoringService get() = this@MonitoringService
    }

    suspend fun scanDevices(): List<DiscoveredDevice> {
        autoConnectJob?.cancel()
        return session.scan()
    }

    suspend fun connect(device: DiscoveredDevice): Unit {
        userDisconnected = false
        session.connect(device)
        (application as RadiaCodeApp).knownDeviceStore.remember(device)
    }

    suspend fun disconnect(): Unit = session.disconnect()

    val radiaCodeSession: RadiaCodeSession get() = session

    val recorder: TrackRecorder get() = trackRecorder

    fun tracksDirectory(): File = trackSession.tracksDirectory(this)

    private fun observeSettings(app: RadiaCodeApp): Unit {
        lifecycleScope.launch {
            app.settingsStore.settings.collectLatest { settings ->
                session.doseUnit = settings.doseUnit
                session.countUnit = settings.countUnit
                session.pollIntervalMs = settings.pollIntervalMs
                session.monitorWindowMs = settings.monitorWindowMs
                autoReconnectEnabled = settings.autoReconnect
                if (!autoReconnectEnabled) autoConnectJob?.cancel()
            }
        }
    }

    private fun observeConnection(app: RadiaCodeApp): Unit {
        lifecycleScope.launch {
            session.monitorState.collectLatest { state ->
                val connected = state.status.connected
                if (wasConnected && !connected && autoReconnectEnabled && !userDisconnected) {
                    scheduleAutoConnect(delayMs = 3_000)
                }
                if (connected) {
                    app.scanUiStore.update(ScanUiState.Idle)
                }
                wasConnected = connected
            }
        }
    }

    private fun observeMonitor(app: RadiaCodeApp): Unit {
        workScope.launch {
            session.monitorState.collectLatest { state ->
                trackSession.setTemperature(state.status.temperatureC)
                val config = app.deviceConfigStore.effective()
                val limits = config?.alarms ?: AlarmLimits(
                    doseUnit = session.doseUnit,
                    countUnit = session.countUnit,
                )
                val updated = snapshotPublisher.appendSeverityHistory(
                    app.liveReadingStore.reading.value.copy(
                        doseRate = state.samples.lastOrNull()?.doseRate ?: 0f,
                        countRate = state.samples.lastOrNull()?.countRate ?: 0f,
                        doseLabel = doseUnitLabel(session.doseUnit),
                        countLabel = countUnitLabel(session.countUnit),
                        accumulatedDose = state.accumulated?.dose ?: 0f,
                        accumulatedDurationSec = state.accumulated?.durationSecs ?: 0L,
                        connected = state.status.connected,
                        deviceLabel = state.status.label ?: "",
                        trackPointCount = trackRecorder.pointCount(),
                        batteryPercent = state.status.batteryPercent,
                        temperatureC = state.status.temperatureC,
                        rssiDbm = state.status.rssiDbm,
                        alarmLimits = limits,
                    ),
                )
                app.liveReadingStore.update { updated }
                snapshotPublisher.publish(updated)
                alarmCoordinator.onReading(updated, config)
                if (foregroundStarted) {
                    MonitoringNotification.update(this@MonitoringService, updated)
                }
                pushComplicationUpdates()
                TileUpdatePusher.pushAll(this@MonitoringService)
            }
        }
        workScope.launch {
            session.deviceConfig.collectLatest { config ->
                if (config != null) app.deviceConfigStore.setLoaded(config)
            }
        }
    }

    private fun scheduleAutoConnect(delayMs: Long = 0): Unit {
        if (!DevicePermissions.canStartBleForeground(this)) return
        if (!autoReconnectEnabled) return
        if (userDisconnected) return
        if (session.monitorState.value.status.connected) return
        if (autoConnectJob?.isActive == true) {
            RadiaLog.debug(component, "AutoConnect already running")
            return
        }
        autoConnectJob = deviceScope.launch {
            val app = application as RadiaCodeApp
            val known = app.knownDeviceStore.list()
            if (known.isEmpty()) {
                app.scanUiStore.update(ScanUiState.Idle)
                return@launch
            }
            if (delayMs > 0) delay(delayMs)
            while (isActive && autoReconnectEnabled && !userDisconnected) {
                if (session.monitorState.value.status.connected) break
                val devices = app.knownDeviceStore.list()
                if (devices.isEmpty()) break
                app.scanUiStore.update(ScanUiState.Connecting(devices.first().displayName()))
                val connected = AutoConnect.tryConnect(session, devices)
                if (connected != null) {
                    app.knownDeviceStore.remember(
                        DiscoveredDevice(
                            connected.address,
                            connected.label,
                            null,
                            connected.model,
                            null,
                        ),
                    )
                    app.scanUiStore.update(ScanUiState.Idle)
                    break
                }
                app.scanUiStore.update(ScanUiState.Failed("Reconnect failed"))
                delay(15_000)
            }
        }
    }

    private suspend fun loadConfig(): Unit {
        val config = session.loadDeviceConfig()
        (application as RadiaCodeApp).deviceConfigStore.setLoaded(config)
    }

    private suspend fun applyPendingConfig(): Unit {
        val app = application as RadiaCodeApp
        val config = app.deviceConfigStore.pending.value ?: return
        session.applyDeviceConfig(config)
        app.deviceConfigStore.setLoaded(config)
    }

    private fun promoteToForegroundIfAllowed(): Unit {
        if (foregroundStarted) return
        if (!DevicePermissions.canStartBleForeground(this)) return
        val reading = (application as RadiaCodeApp).liveReadingStore.reading.value
        startForeground(MonitoringNotification.NOTIFICATION_ID, MonitoringNotification.build(this, reading))
        foregroundStarted = true
    }

    private fun pushComplicationUpdates(): Unit {
        ComplicationUpdatePusher.pushAll(this)
    }

    companion object {
        const val ACTION_CONNECT = "com.ngxyzaudax.radiacode.CONNECT"
        const val ACTION_DISCONNECT = "com.ngxyzaudax.radiacode.DISCONNECT"
        const val ACTION_AUTO_CONNECT = "com.ngxyzaudax.radiacode.AUTO_CONNECT"
        const val ACTION_START_TRACK = "com.ngxyzaudax.radiacode.START_TRACK"
        const val ACTION_STOP_TRACK = "com.ngxyzaudax.radiacode.STOP_TRACK"
        const val ACTION_LOAD_CONFIG = "com.ngxyzaudax.radiacode.LOAD_CONFIG"
        const val ACTION_APPLY_CONFIG = "com.ngxyzaudax.radiacode.APPLY_CONFIG"
        const val ACTION_DOSE_RESET = "com.ngxyzaudax.radiacode.DOSE_RESET"
        const val ACTION_SPECTRUM_RESET = "com.ngxyzaudax.radiacode.SPECTRUM_RESET"
        const val ACTION_SYNC_CLOCK = "com.ngxyzaudax.radiacode.SYNC_CLOCK"
        const val EXTRA_ADDRESS = "address"
        const val EXTRA_LABEL = "label"
        const val EXTRA_MODEL = "model"
        const val EXTRA_SERIAL = "serial"
    }
}
