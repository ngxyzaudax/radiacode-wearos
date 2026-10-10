package com.ngxyzaudax.radiacode.export

sealed interface TrackExportState {
    data object WaitingForWifi : TrackExportState
    data class Ready(val url: String) : TrackExportState
    data object WifiUnavailable : TrackExportState
    data class Failed(val message: String) : TrackExportState
}
