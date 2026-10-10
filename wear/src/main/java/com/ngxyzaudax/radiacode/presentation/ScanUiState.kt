package com.ngxyzaudax.radiacode.presentation

sealed interface ScanUiState {
    data object Idle : ScanUiState
    data object Scanning : ScanUiState
    data object NeedsPermissions : ScanUiState
    data class Finished(val count: Int) : ScanUiState
    data class Failed(val message: String) : ScanUiState
    data class Connecting(val deviceName: String) : ScanUiState
}
