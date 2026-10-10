package com.ngxyzaudax.radiacode.presentation.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.ngxyzaudax.radiacode.export.TrackExportController
import com.ngxyzaudax.radiacode.export.TrackExportState
import com.ngxyzaudax.radiacode.presentation.components.QrCodeImage

private const val WATCH_WIFI_SETTINGS: String =
    "com.google.android.clockwork.settings.connectivity.wifi.ADD_NETWORK_SETTINGS"
private val QR_SIZE = 100.dp

@Composable
fun TrackExportScreen(): Unit {
    val context = LocalContext.current
    val view = LocalView.current
    val controller = remember { TrackExportController(context.applicationContext) }
    val state by controller.state.collectAsState()
    DisposableEffect(controller) {
        view.keepScreenOn = true
        controller.start()
        onDispose {
            view.keepScreenOn = false
            controller.stop()
        }
    }
    ScreenScaffold {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ExportBody(state = state, onOpenWifiSettings = { openWatchWifiSettings(context) })
        }
    }
}

@Composable
private fun ExportBody(state: TrackExportState, onOpenWifiSettings: () -> Unit): Unit = when (state) {
    TrackExportState.WaitingForWifi -> StatusLine("Connecting Wi-Fi")
    TrackExportState.WifiUnavailable -> WifiUnavailable(onOpenWifiSettings)
    is TrackExportState.Failed -> StatusLine(state.message)
    is TrackExportState.Ready -> ReadyExport(state.url)
}

@Composable
private fun ReadyExport(url: String): Unit {
    StatusLine("Scan to download")
    QrCodeImage(content = url, modifier = Modifier.size(QR_SIZE).padding(vertical = 4.dp))
    StatusLine(url)
}

@Composable
private fun WifiUnavailable(onOpenWifiSettings: () -> Unit): Unit {
    StatusLine("Wi-Fi unavailable")
    Button(onClick = onOpenWifiSettings) { Text("Open Wi-Fi settings") }
}

@Composable
private fun StatusLine(text: String): Unit {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(top = 6.dp),
    )
}

private fun openWatchWifiSettings(context: Context): Unit {
    val opened = runCatching {
        context.startActivity(Intent(WATCH_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }.isSuccess
    if (opened) return
    runCatching {
        context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
