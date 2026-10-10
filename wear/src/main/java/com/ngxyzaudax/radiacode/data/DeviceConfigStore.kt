package com.ngxyzaudax.radiacode.data

import com.ngxyzaudax.radiacode.protocol.DeviceConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DeviceConfigStore {
    private val _loaded = MutableStateFlow<DeviceConfig?>(null)
    val loaded: StateFlow<DeviceConfig?> = _loaded.asStateFlow()

    private val _pending = MutableStateFlow<DeviceConfig?>(null)
    val pending: StateFlow<DeviceConfig?> = _pending.asStateFlow()

    fun setLoaded(config: DeviceConfig?): Unit {
        _loaded.value = config
        if (_pending.value == null) _pending.value = config
    }

    fun setPending(config: DeviceConfig): Unit {
        _pending.value = config
    }

    fun effective(): DeviceConfig? = _pending.value ?: _loaded.value
}
