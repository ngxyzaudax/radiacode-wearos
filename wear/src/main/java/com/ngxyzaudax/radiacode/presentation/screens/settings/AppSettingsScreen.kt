package com.ngxyzaudax.radiacode.presentation.screens.settings

import androidx.compose.runtime.Composable
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import com.ngxyzaudax.radiacode.data.AppSettings
import com.ngxyzaudax.radiacode.presentation.components.ScreenHeader
import com.ngxyzaudax.radiacode.presentation.components.SettingOptionRow
import com.ngxyzaudax.radiacode.presentation.components.SettingToggleRow

@Composable
fun AppSettingsScreen(
    settings: AppSettings,
    onUpdate: (transform: (AppSettings) -> AppSettings) -> Unit,
): Unit {
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    ScreenScaffold(scrollState = listState) { padding ->
        TransformingLazyColumn(state = listState, contentPadding = padding) {
            item { ScreenHeader(this, transformationSpec, "App") }
            item {
                SettingOptionRow(this, transformationSpec, "Poll ${settings.pollIntervalMs} ms") {
                    onUpdate { current -> current.copy(pollIntervalMs = if (current.pollIntervalMs == 1000L) 2000L else 1000L) }
                }
            }
            item {
                SettingToggleRow(this, transformationSpec, "Auto reconnect", settings.autoReconnect) { enabled ->
                    onUpdate { current -> current.copy(autoReconnect = enabled) }
                }
            }
        }
    }
}
