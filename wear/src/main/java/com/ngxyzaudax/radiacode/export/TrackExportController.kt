package com.ngxyzaudax.radiacode.export

import android.content.Context
import android.net.ConnectivityManager
import com.ngxyzaudax.radiacode.log.RadiaLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class TrackExportController(private val context: Context) {
    private val component: String = "TrackExportController"
    private val requester = WifiNetworkRequester(
        context.getSystemService(ConnectivityManager::class.java),
    )
    private var server: TrackHttpServer? = null
    private val _state = MutableStateFlow<TrackExportState>(TrackExportState.WaitingForWifi)
    val state: StateFlow<TrackExportState> = _state.asStateFlow()

    fun start(): Unit {
        stop()
        _state.value = TrackExportState.WaitingForWifi
        runCatching { requester.request(::onWifi) }
            .onFailure { error -> fail("Wi-Fi request failed", error) }
    }

    fun stop(): Unit {
        requester.release()
        stopServer()
    }

    private fun onWifi(result: WifiRequestResult): Unit = when (result) {
        is WifiRequestResult.Address -> openServer(result.host)
        WifiRequestResult.Unavailable -> markUnavailable()
    }

    private fun markUnavailable(): Unit {
        if (_state.value !is TrackExportState.WaitingForWifi) return
        RadiaLog.warn(component, "Wi-Fi unavailable for track export")
        _state.value = TrackExportState.WifiUnavailable
    }

    private fun openServer(host: String): Unit {
        val listening = server
        val current = _state.value
        if (listening != null && current is TrackExportState.Ready && current.url == exportUrl(host, listening.listeningPort)) {
            return
        }
        stopServer()
        runCatching { TrackHttpServer.start(tracksDirectory(), context.cacheDir) }
            .onSuccess { started -> publish(host, started) }
            .onFailure { error -> fail("Export server failed to start", error) }
    }

    private fun publish(host: String, started: TrackHttpServer): Unit {
        server = started
        val url = exportUrl(host, started.listeningPort)
        _state.value = TrackExportState.Ready(url)
        RadiaLog.info(component, "Export server listening url=$url")
    }

    private fun fail(message: String, error: Throwable): Unit {
        RadiaLog.error(component, message, error)
        _state.value = TrackExportState.Failed(error.message ?: message)
    }

    private fun stopServer(): Unit {
        server?.let { running -> runCatching { running.stop() } }
        server = null
    }

    private fun tracksDirectory(): File = File(context.filesDir, "tracks")
}

private fun exportUrl(host: String, port: Int): String = "http://$host:$port/"
