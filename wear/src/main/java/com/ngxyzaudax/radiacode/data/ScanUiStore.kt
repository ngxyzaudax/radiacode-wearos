package com.ngxyzaudax.radiacode.data

import com.ngxyzaudax.radiacode.presentation.ScanUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScanUiStore {
    private val _state = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val state: StateFlow<ScanUiState> = _state.asStateFlow()

    fun update(value: ScanUiState): Unit {
        _state.value = value
    }
}
