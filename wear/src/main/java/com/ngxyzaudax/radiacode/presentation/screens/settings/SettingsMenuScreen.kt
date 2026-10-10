package com.ngxyzaudax.radiacode.presentation.screens.settings

import androidx.compose.runtime.Composable
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import com.ngxyzaudax.radiacode.presentation.AppRoute
import com.ngxyzaudax.radiacode.presentation.components.ScreenHeader
import com.ngxyzaudax.radiacode.presentation.components.SettingOptionRow

@Composable
fun SettingsMenuScreen(onNavigate: (AppRoute) -> Unit): Unit {
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    ScreenScaffold(scrollState = listState) { padding ->
        TransformingLazyColumn(state = listState, contentPadding = padding) {
            item { ScreenHeader(this, transformationSpec, "Settings") }
            item { SettingOptionRow(this, transformationSpec, "Units", { onNavigate(AppRoute.SettingsUnits) }) }
            item { SettingOptionRow(this, transformationSpec, "Alarms", { onNavigate(AppRoute.SettingsAlarms) }) }
            item { SettingOptionRow(this, transformationSpec, "Signals", { onNavigate(AppRoute.SettingsSignals) }) }
            item { SettingOptionRow(this, transformationSpec, "Display", { onNavigate(AppRoute.SettingsDisplay) }) }
            item { SettingOptionRow(this, transformationSpec, "App", { onNavigate(AppRoute.SettingsApp) }) }
            item {
                SettingOptionRow(this, transformationSpec, "Device actions", {
                    onNavigate(AppRoute.SettingsDeviceActions)
                })
            }
        }
    }
}
