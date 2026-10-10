package com.ngxyzaudax.radiacode.presentation.screens.settings

import androidx.compose.runtime.Composable
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import com.ngxyzaudax.radiacode.presentation.AppRoute
import com.ngxyzaudax.radiacode.presentation.components.ScreenHeader
import com.ngxyzaudax.radiacode.presentation.components.SettingOptionRow
import com.ngxyzaudax.radiacode.presentation.components.SettingToggleRow
import com.ngxyzaudax.radiacode.protocol.AlarmSignalMode
import com.ngxyzaudax.radiacode.protocol.DeviceConfig

@Composable
fun SignalSettingsScreen(
    config: DeviceConfig?,
    onUpdate: (DeviceConfig) -> Unit,
    onApply: () -> Unit,
    onNavigate: (AppRoute) -> Unit,
): Unit {
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val current = config ?: return
    ScreenScaffold(scrollState = listState) { padding ->
        TransformingLazyColumn(state = listState, contentPadding = padding) {
            item { ScreenHeader(this, transformationSpec, "Signals") }
            item {
                SettingToggleRow(this, transformationSpec, "Sound", current.soundOn) {
                    onUpdate(current.copy(soundOn = it))
                }
            }
            item {
                SettingToggleRow(this, transformationSpec, "Vibration", current.vibroOn) {
                    onUpdate(current.copy(vibroOn = it))
                }
            }
            item {
                SettingToggleRow(this, transformationSpec, "LED", current.ledsOn) {
                    onUpdate(current.copy(ledsOn = it))
                }
            }
            item {
                SettingOptionRow(this, transformationSpec, "Other sound events") {
                    onNavigate(AppRoute.SettingsSoundEvents)
                }
            }
            item {
                SettingOptionRow(this, transformationSpec, "Other vibration events") {
                    onNavigate(AppRoute.SettingsVibroEvents)
                }
            }
            item {
                SettingOptionRow(
                    this,
                    transformationSpec,
                    "Alarm mode: ${current.alarmMode.name}",
                ) {
                    val next = if (current.alarmMode == AlarmSignalMode.Continuous) {
                        AlarmSignalMode.Once
                    } else {
                        AlarmSignalMode.Continuous
                    }
                    onUpdate(current.copy(alarmMode = next))
                }
            }
            item { SettingOptionRow(this, transformationSpec, "Save to device", onApply) }
        }
    }
}
