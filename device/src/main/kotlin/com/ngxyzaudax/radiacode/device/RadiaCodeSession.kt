package com.ngxyzaudax.radiacode.device

import com.ngxyzaudax.radiacode.ble.BleTransport
import com.ngxyzaudax.radiacode.ble.ConnectionState
import com.ngxyzaudax.radiacode.ble.DiscoveredDevice
import com.ngxyzaudax.radiacode.log.RadiaLog
import com.ngxyzaudax.radiacode.protocol.Command
import com.ngxyzaudax.radiacode.protocol.CountDisplayUnit
import com.ngxyzaudax.radiacode.protocol.DeviceConfig
import com.ngxyzaudax.radiacode.protocol.DoseDisplayUnit
import com.ngxyzaudax.radiacode.protocol.ProtocolException
import com.ngxyzaudax.radiacode.protocol.SequenceCounter
import com.ngxyzaudax.radiacode.protocol.VirtSfr
import com.ngxyzaudax.radiacode.protocol.VirtString
import com.ngxyzaudax.radiacode.protocol.buildReadVirtString
import com.ngxyzaudax.radiacode.protocol.buildRequest
import com.ngxyzaudax.radiacode.protocol.decodeDataBuf
import com.ngxyzaudax.radiacode.protocol.decodeSpectrum
import com.ngxyzaudax.radiacode.protocol.parseVirtStringPayload
import com.ngxyzaudax.radiacode.protocol.realTimeRecords
import com.ngxyzaudax.radiacode.protocol.requestHeader
import com.ngxyzaudax.radiacode.protocol.stripEchoedHeader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RadiaCodeSession(
    private val transport: BleTransport,
    private val scope: CoroutineScope,
) {
    private val component: String = "RadiaCodeSession"
    private val sequence = SequenceCounter()
    private lateinit var vsfrIo: VsfrIo
    private lateinit var deviceActions: DeviceActions
    private var specFormatVersion: Int = 0
    private var baseTimeEpochMs: Long = 0L
    private var pollJob: Job? = null
    private var rssiJob: Job? = null
    private var connectedAddress: String? = null
    private var connectedLabel: String? = null
    private var carryStatus: DeviceStatus = DeviceStatus(null, null, null, false, null)
    private var carryAccumulated: AccumulatedDose? = null
    private var sawRareData: Boolean = false
    private val connectMutex = Mutex()

    private val _deviceInfo = MutableStateFlow<DeviceInfo?>(null)
    val deviceInfo: StateFlow<DeviceInfo?> = _deviceInfo.asStateFlow()

    private val _monitorState = MutableStateFlow(
        MonitorState(emptyList(), null, DeviceStatus(null, null, null, false, null)),
    )
    val monitorState: StateFlow<MonitorState> = _monitorState.asStateFlow()

    private val _spectrumState = MutableStateFlow(SpectrumState(null, null, 0))
    val spectrumState: StateFlow<SpectrumState> = _spectrumState.asStateFlow()

    private val _discovered = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val discovered: StateFlow<List<DiscoveredDevice>> = _discovered.asStateFlow()

    private val _deviceConfig = MutableStateFlow<DeviceConfig?>(null)
    val deviceConfig: StateFlow<DeviceConfig?> = _deviceConfig.asStateFlow()

    private val _rssi = MutableStateFlow<Int?>(null)
    val rssi: StateFlow<Int?> = _rssi.asStateFlow()

    var doseUnit: DoseDisplayUnit = DoseDisplayUnit.MicroSievertPerHour
    var countUnit: CountDisplayUnit = CountDisplayUnit.Cps
    var monitorWindowMs: Long = 60_000
    var pollIntervalMs: Long = 1_000

    init {
        bindIo()
        observeLinkState()
    }

    private fun bindIo(): Unit {
        vsfrIo = VsfrIo(::executeCommand, sequence)
        deviceActions = DeviceActions(vsfrIo, ::executeCommandRaw)
    }

    private fun observeLinkState(): Unit {
        scope.launch {
            transport.connectionState.collect { state ->
                if (state == ConnectionState.Disconnected) {
                    markDisconnected("link lost")
                }
            }
        }
    }

    suspend fun scan(durationMs: Long = 5_000): List<DiscoveredDevice> {
        RadiaLog.info(component, "Scan requested durationMs=$durationMs")
        val devices = transport.scan(durationMs)
        _discovered.value = devices
        RadiaLog.info(component, "Scan result count=${devices.size}")
        return devices
    }

    suspend fun connect(device: DiscoveredDevice): Unit = connectMutex.withLock {
        RadiaLog.info(component, "Connect requested label=${device.label} address=${device.address}")
        var lastError: Exception? = null
        repeat(3) { attempt ->
            if (attempt > 0) {
                RadiaLog.warn(component, "Connect retry attempt=${attempt + 1}")
                runCatching { transport.disconnect() }
                delay(1_500)
            }
            val outcome = runCatching {
                transport.connect(device.address)
                connectedAddress = device.address
                connectedLabel = device.label
                val init = initializeRadiaCodeSession(
                    executeRaw = ::executeRaw,
                    readVirtString = ::readVirtString,
                    vsfr = vsfrIo,
                    drain = { transport.drain() },
                    deviceLabel = device.label,
                )
                specFormatVersion = init.specFormatVersion
                baseTimeEpochMs = init.baseTimeEpochMs
                _deviceInfo.value = DeviceInfo(
                    serial = init.serial,
                    firmwareMajor = init.versions.target.major,
                    firmwareMinor = init.versions.target.minor,
                    model = connectedLabel ?: "RC-110",
                )
                _spectrumState.value = SpectrumState(null, null, specFormatVersion)
                carryStatus = DeviceStatus(null, null, null, false, null)
                carryAccumulated = null
                sawRareData = false
                runCatching { loadDeviceConfig() }
                    .onFailure { error -> RadiaLog.warn(component, "Config load failed ${error.message}") }
                markConnected()
            }
            if (outcome.isSuccess) {
                startPolling()
                startRssiPolling()
                RadiaLog.info(component, "Session ready label=${device.label}")
                return
            }
            lastError = outcome.exceptionOrNull() as? Exception
            RadiaLog.error(component, "Connect attempt failed ${lastError?.message}")
            connectedAddress = null
            connectedLabel = null
        }
        throw lastError ?: ProtocolException("Connect failed")
    }

    suspend fun disconnect(): Unit {
        RadiaLog.info(component, "Disconnect requested")
        pollJob?.cancel()
        pollJob = null
        rssiJob?.cancel()
        rssiJob = null
        transport.disconnect()
        connectedAddress = null
        connectedLabel = null
        _deviceInfo.value = null
        _deviceConfig.value = null
        _rssi.value = null
        carryStatus = DeviceStatus(null, null, null, false, null)
        carryAccumulated = null
        sawRareData = false
        _monitorState.value = MonitorState(emptyList(), null, carryStatus)
        _spectrumState.value = SpectrumState(null, null, 0)
        RadiaLog.info(component, "Session cleared")
    }

    suspend fun loadDeviceConfig(): DeviceConfig {
        val config = loadDeviceConfig(vsfrIo, ::readVirtString)
        _deviceConfig.value = config
        doseUnit = config.alarms.doseUnit
        countUnit = config.alarms.countUnit
        return config
    }

    suspend fun applyDeviceConfig(config: DeviceConfig): Unit {
        applyDeviceConfig(vsfrIo, config)
        _deviceConfig.value = config
        doseUnit = config.alarms.doseUnit
        countUnit = config.alarms.countUnit
    }

    suspend fun doseReset(): Unit = deviceActions.doseReset()

    suspend fun spectrumReset(): Unit = deviceActions.spectrumReset()

    suspend fun syncClock(): Unit = deviceActions.syncClock {
        executeRaw(Command.SetTime, com.ngxyzaudax.radiacode.protocol.buildSetTimePayload())
    }

    suspend fun readLatestRates(): Pair<Float, Float>? {
        val payload = readVirtString(VirtString.DataBuf)
        val frame = decodeDataBuf(payload)
        val latest = realTimeRecords(frame).lastOrNull() ?: return null
        return com.ngxyzaudax.radiacode.protocol.doseDisplayFromRh(latest.doseRateRh, doseUnit) to
            com.ngxyzaudax.radiacode.protocol.countDisplayFromCps(latest.countRateCps, countUnit)
    }

    private fun markConnected(): Unit {
        val current = _monitorState.value
        _monitorState.value = current.copy(
            status = current.status.copy(connected = true, label = connectedLabel),
        )
    }

    private fun markDisconnected(reason: String): Unit {
        val current = _monitorState.value
        if (!current.status.connected) return
        RadiaLog.warn(component, "Marking disconnected reason=$reason")
        pollJob?.cancel()
        pollJob = null
        rssiJob?.cancel()
        rssiJob = null
        _monitorState.value = current.copy(
            status = current.status.copy(connected = false),
        )
    }

    private fun startPolling() {
        pollJob?.cancel()
        var spectrumTick = 0
        pollJob = scope.launch {
            while (isActive) {
                runCatching { pollOnce(refreshSpectrum = spectrumTick % 5 == 0) }
                    .onFailure { error ->
                        RadiaLog.warn(component, "Poll failed ${error.message}")
                    }
                spectrumTick += 1
                delay(pollIntervalMs)
            }
        }
        RadiaLog.debug(component, "Polling started intervalMs=$pollIntervalMs")
    }

    private fun startRssiPolling() {
        rssiJob?.cancel()
        rssiJob = scope.launch {
            while (isActive) {
                runCatching {
                    transport.readRemoteRssi()?.let { value ->
                        _rssi.value = value
                        val current = _monitorState.value
                        if (current.status.connected) {
                            _monitorState.value = current.copy(
                                status = current.status.copy(rssiDbm = value),
                            )
                        }
                    }
                }
                delay(10_000)
            }
        }
    }

    private suspend fun pollOnce(refreshSpectrum: Boolean) {
        val payload = readVirtString(VirtString.DataBuf)
        val frame = decodeDataBuf(payload)
        val now = System.currentTimeMillis()
        val newSamples = realTimeRecords(frame).map { record ->
            val timestampMs = wallClockMs(record.header.tsOffset, now)
            toMonitorSample(record, doseUnit, countUnit, timestampMs)
        }
        val existing = _monitorState.value.samples
        val merged = (existing + newSamples).filter { now - it.timestampMs <= monitorWindowMs }
        val rare = frame.latestRare
        if (rare != null && !sawRareData) {
            sawRareData = true
            RadiaLog.debug(
                component,
                "First RareData battery=${rare.batteryPercent} temp=${rare.temperatureC}",
            )
        }
        val freshBattery = rare?.batteryPercent?.takeIf(::validBattery)
        var freshTemp = rare?.temperatureC?.takeIf(::validTemperature)
        if (freshTemp == null) {
            freshTemp = runCatching { vsfrIo.readVsfrF32(VirtSfr.TempDegC) }
                .getOrNull()
                ?.takeIf(::validTemperature)
        }
        val freshStatus = DeviceStatus(
            batteryPercent = freshBattery,
            temperatureC = freshTemp,
            rssiDbm = _rssi.value,
            connected = true,
            label = connectedLabel,
        )
        carryStatus = mergeDeviceStatus(carryStatus, freshStatus)
        val freshAccumulated = rare?.let { toAccumulatedDose(it, doseUnit) }
        carryAccumulated = mergeAccumulatedDose(carryAccumulated, freshAccumulated)
        _monitorState.value = MonitorState(merged, carryAccumulated, carryStatus)
        if (!refreshSpectrum) return
        val livePayload = readVirtString(VirtString.Spectrum)
        val live = decodeSpectrum(livePayload, specFormatVersion)
        val accumPayload = readVirtString(VirtString.SpecAccum)
        val accum = decodeSpectrum(accumPayload, specFormatVersion)
        _spectrumState.value = SpectrumState(live, accum, specFormatVersion)
    }

    private fun wallClockMs(tsOffset: Int, fallbackNowMs: Long): Long {
        if (baseTimeEpochMs <= 0L) return fallbackNowMs
        val tickMs = tsOffset.toLong().coerceAtLeast(0L) * 10L
        return baseTimeEpochMs + tickMs
    }

    private suspend fun readVirtString(virtString: VirtString): ByteArray {
        val seq = sequence.advance()
        val request = buildReadVirtString(virtString, seq)
        val response = transport.execute(request)
        val body = stripEchoedHeader(response, requestHeader(Command.RdVirtString, seq))
        return parseVirtStringPayload(body)
    }

    private suspend fun executeCommand(command: Command, seq: Int, args: ByteArray): ByteArray {
        val request = buildRequest(command, seq, args)
        val response = transport.execute(request)
        return stripEchoedHeader(response, requestHeader(command, seq))
    }

    private suspend fun executeCommandRaw(command: Command, args: ByteArray): ByteArray {
        val seq = sequence.advance()
        return executeCommand(command, seq, args)
    }

    private suspend fun executeRaw(command: Command, args: ByteArray): ByteArray =
        executeCommandRaw(command, args)
}
