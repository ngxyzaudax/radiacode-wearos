package com.ngxyzaudax.radiacode.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.content.Context
import com.ngxyzaudax.radiacode.log.RadiaLog
import com.ngxyzaudax.radiacode.protocol.ProtocolException
import com.ngxyzaudax.radiacode.protocol.ResponseAssembler
import com.ngxyzaudax.radiacode.protocol.framedRequestHeader
import com.ngxyzaudax.radiacode.protocol.responseMatchesRequest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

@SuppressLint("MissingPermission")
class BleTransport(private val context: Context) {
    private val component: String = "BleTransport"
    private val mutex = Mutex()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var gatt: BluetoothGatt? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null
    private var notifyCharacteristic: BluetoothGattCharacteristic? = null
    private val notificationQueue = ConcurrentLinkedQueue<ByteArray>()
    private var connectDeferred: CompletableDeferred<Unit>? = null
    private var writeDeferred: CompletableDeferred<Unit>? = null
    private val linkAlive = AtomicBoolean(false)
    private val _connectionState = MutableSharedFlow<ConnectionState>(replay = 1)
    val connectionState = _connectionState.asSharedFlow()
    private val _rssi = MutableStateFlow<Int?>(null)
    val rssi: StateFlow<Int?> = _rssi.asStateFlow()
    private var rssiDeferred: CompletableDeferred<Int>? = null

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            RadiaLog.info(component, "GATT state=$newState status=$status")
            if (newState == BluetoothProfile.STATE_CONNECTED && status == BluetoothGatt.GATT_SUCCESS) {
                linkAlive.set(true)
                gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_BALANCED)
                gatt.discoverServices()
                return
            }
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                RadiaLog.warn(component, "GATT disconnected status=$status")
                linkAlive.set(false)
                connectDeferred?.let { deferred ->
                    if (!deferred.isCompleted) {
                        deferred.completeExceptionally(ProtocolException("Disconnected status=$status"))
                    }
                }
                _connectionState.tryEmit(ConnectionState.Disconnected)
                mainHandler.post { closeGattHandles() }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            RadiaLog.debug(component, "Services discovered status=$status")
            if (status != BluetoothGatt.GATT_SUCCESS) {
                failConnect(ProtocolException("Service discovery failed status=$status"))
                return
            }
            val service = gatt.getService(BleUuids.service) ?: run {
                failConnect(ProtocolException("Service not found"))
                return
            }
            writeCharacteristic = service.getCharacteristic(BleUuids.write)
            notifyCharacteristic = service.getCharacteristic(BleUuids.notify)
            val notifyChar = notifyCharacteristic ?: run {
                failConnect(ProtocolException("Notify char not found"))
                return
            }
            if (writeCharacteristic == null) {
                failConnect(ProtocolException("Write char not found"))
                return
            }
            gatt.setCharacteristicNotification(notifyChar, true)
            val descriptor = notifyChar.getDescriptor(CCCD_UUID) ?: run {
                failConnect(ProtocolException("CCCD missing"))
                return
            }
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            if (!gatt.writeDescriptor(descriptor)) {
                failConnect(ProtocolException("CCCD write rejected"))
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int,
        ) {
            if (descriptor.uuid != CCCD_UUID) return
            if (status != BluetoothGatt.GATT_SUCCESS) {
                failConnect(ProtocolException("CCCD write failed status=$status"))
                return
            }
            RadiaLog.info(component, "Notifications enabled")
            connectDeferred?.complete(Unit)
            _connectionState.tryEmit(ConnectionState.Connected)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
        ) {
            notificationQueue.add(value)
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            characteristic.value?.let { notificationQueue.add(it) }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int,
        ) {
            val deferred = writeDeferred
            writeDeferred = null
            if (status == BluetoothGatt.GATT_SUCCESS) {
                deferred?.complete(Unit)
            } else {
                deferred?.completeExceptionally(ProtocolException("Write status=$status"))
            }
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                _rssi.value = rssi
                rssiDeferred?.complete(rssi)
            } else {
                rssiDeferred?.completeExceptionally(ProtocolException("RSSI read failed $status"))
            }
        }
    }

    private fun failConnect(error: ProtocolException): Unit {
        RadiaLog.error(component, error.message ?: "Connect failed")
        connectDeferred?.completeExceptionally(error)
    }

    suspend fun readRemoteRssi(): Int? = mutex.withLock {
        val gattRef = gatt ?: return null
        rssiDeferred = CompletableDeferred()
        if (!gattRef.readRemoteRssi()) {
            rssiDeferred = null
            return _rssi.value
        }
        return runCatching { withTimeout(3_000) { rssiDeferred!!.await() } }
            .getOrNull()
            .also { rssiDeferred = null }
    }

    suspend fun scan(durationMs: Long): List<DiscoveredDevice> {
        RadiaLog.info(component, "BLE scan started durationMs=$durationMs")
        val adapter = bluetoothAdapter() ?: run {
            RadiaLog.warn(component, "Bluetooth adapter unavailable")
            return emptyList()
        }
        val found = LinkedHashMap<String, DiscoveredDevice>()
        val deferred = CompletableDeferred<Unit>()
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val record = result.scanRecord
                val uuids = record?.serviceUuids?.map { it.uuid } ?: emptyList()
                val name = record?.deviceName ?: result.device.name ?: "RadiaCode"
                if (!isRadiacodeAdvertisement(name, uuids) && uuids.none { it == BleUuids.service }) return
                found[result.device.address] = DiscoveredDevice(
                    address = result.device.address,
                    label = labelFromAdvertisement(name),
                    serial = serialFromAdvertisement(name),
                    model = modelFromAdvertisement(name),
                    rssi = result.rssi,
                )
            }

            override fun onScanFailed(errorCode: Int) {
                RadiaLog.error(component, "Scan failed errorCode=$errorCode")
                deferred.completeExceptionally(ProtocolException("Scan failed $errorCode"))
            }
        }
        val scanner = adapter.bluetoothLeScanner ?: return emptyList()
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(BleUuids.service))
            .build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        scanner.startScan(listOf(filter), settings, callback)
        delay(durationMs)
        scanner.stopScan(callback)
        RadiaLog.info(component, "BLE scan finished count=${found.size}")
        return found.values.sortedBy { it.address }
    }

    suspend fun connect(address: String): Unit = mutex.withLock {
        RadiaLog.info(component, "Connecting address=$address")
        disconnectInternal()
        delay(500)
        val device = bluetoothAdapter()?.getRemoteDevice(address)
            ?: throw ProtocolException("Bluetooth unavailable")
        val deferred = CompletableDeferred<Unit>()
        connectDeferred = deferred
        linkAlive.set(false)
        try {
            gatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
            withTimeout(20_000) { deferred.await() }
            delay(BleUuids.linkSettleMs)
            val drained = drainNotificationsQuiet(
                notificationQueue,
                BleUuids.settleDrainMs,
                peekThenQuiet = false,
                linkAlive = { linkAlive.get() },
            )
            if (drained > 0) {
                RadiaLog.debug(component, "Settle drain count=$drained")
            }
            RadiaLog.info(component, "Connected address=$address")
        } catch (error: Exception) {
            disconnectInternal()
            throw error
        } finally {
            if (connectDeferred === deferred) connectDeferred = null
        }
    }

    suspend fun disconnect(): Unit = mutex.withLock {
        RadiaLog.info(component, "Disconnect requested")
        disconnectInternal()
    }

    suspend fun drain(): Unit = mutex.withLock {
        drainNotificationsQuiet(
            notificationQueue,
            BleUuids.settleDrainMs,
            peekThenQuiet = false,
            linkAlive = { linkAlive.get() },
        )
    }

    suspend fun execute(request: ByteArray): ByteArray = mutex.withLock {
        if (!linkAlive.get()) throw ProtocolException("Not connected")
        val gattRef = gatt ?: throw ProtocolException("Not connected")
        val writeChar = writeCharacteristic ?: throw ProtocolException("Write char missing")
        drainNotificationsQuiet(
            notificationQueue,
            BleUuids.commandDrainMs,
            peekThenQuiet = true,
            linkAlive = { linkAlive.get() },
        )
        request.toList().chunked(BleUuids.chunkSize).forEach { chunk ->
            writeChunk(gattRef, writeChar, chunk.toByteArray())
        }
        val expected = framedRequestHeader(request)
        var assembler = ResponseAssembler()
        val deadline = System.currentTimeMillis() + BleUuids.responseTimeoutMs
        var discarded = 0
        while (System.currentTimeMillis() < deadline) {
            if (!linkAlive.get()) throw ProtocolException("Disconnected")
            val chunk = pollNotification(200) ?: continue
            val payload = assembler.push(chunk) ?: continue
            if (responseMatchesRequest(payload, expected)) return payload
            discarded += 1
            RadiaLog.warn(component, "Discarding unrelated BLE frame discarded=$discarded")
            assembler = ResponseAssembler()
        }
        drainNotificationsQuiet(
            notificationQueue,
            2_500,
            peekThenQuiet = false,
            linkAlive = { linkAlive.get() },
        )
        RadiaLog.error(component, "Response timeout bytes=${request.size} discarded=$discarded")
        throw ProtocolException("Response timeout")
    }

    private suspend fun writeChunk(
        gattRef: BluetoothGatt,
        writeChar: BluetoothGattCharacteristic,
        chunk: ByteArray,
    ): Unit {
        if (!linkAlive.get()) throw ProtocolException("Disconnected")
        val supportsAck = (writeChar.properties and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0
        writeChar.writeType = if (supportsAck) {
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        } else {
            BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        }
        writeChar.value = chunk
        if (supportsAck) {
            val deferred = CompletableDeferred<Unit>()
            writeDeferred = deferred
            if (!gattRef.writeCharacteristic(writeChar)) {
                writeDeferred = null
                throw ProtocolException("Write failed")
            }
            try {
                withTimeout(5_000) { deferred.await() }
            } finally {
                if (writeDeferred === deferred) writeDeferred = null
            }
            return
        }
        if (!gattRef.writeCharacteristic(writeChar)) {
            delay(30)
            if (!linkAlive.get()) throw ProtocolException("Disconnected")
            if (!gattRef.writeCharacteristic(writeChar)) {
                throw ProtocolException("Write failed")
            }
        }
        delay(30)
    }

    private suspend fun pollNotification(timeoutMs: Long): ByteArray? {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (!linkAlive.get()) return null
            notificationQueue.poll()?.let { return it }
            delay(10)
        }
        return null
    }

    private fun disconnectInternal() {
        linkAlive.set(false)
        writeDeferred?.completeExceptionally(ProtocolException("Disconnected"))
        writeDeferred = null
        val current = gatt
        gatt = null
        writeCharacteristic = null
        notifyCharacteristic = null
        notificationQueue.clear()
        runCatching { current?.disconnect() }
        runCatching { current?.close() }
        _connectionState.tryEmit(ConnectionState.Disconnected)
    }

    private fun closeGattHandles() {
        val current = gatt ?: return
        gatt = null
        writeCharacteristic = null
        notifyCharacteristic = null
        runCatching { current.close() }
    }

    private fun bluetoothAdapter(): BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

    companion object {
        private val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }
}

enum class ConnectionState {
    Disconnected,
    Connected,
}
