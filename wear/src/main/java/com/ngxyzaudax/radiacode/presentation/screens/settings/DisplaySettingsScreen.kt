package com.ngxyzaudax.radiacode.presentation.screens.settings

import androidx.compose.runtime.Composable
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import com.ngxyzaudax.radiacode.presentation.components.ScreenHeader
import com.ngxyzaudax.radiacode.presentation.components.SettingOptionRow
import com.ngxyzaudax.radiacode.protocol.BacklightOffTime
import com.ngxyzaudax.radiacode.protocol.DeviceConfig
import com.ngxyzaudax.radiacode.protocol.DisplayDirection

@Composable
fun DisplaySettingsScreen(
    config: DeviceConfig?,
    onUpdate: (DeviceConfig) -> Unit,
    onApply: () -> Unit,
): Unit {
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val current = config ?: return
    ScreenScaffold(scrollState = listState) { padding ->
        TransformingLazyColumn(state = listState, contentPadding = padding) {
            item { ScreenHeader(this, transformationSpec, "Display") }
            item {
                SettingOptionRow(this, transformationSpec, "Brightness ${current.brightness}") {
                    onUpdate(current.copy(brightness = (current.brightness + 1).coerceAtMost(9)))
                }
            }
            item {
                SettingOptionRow(this, transformationSpec, "Backlight: ${current.backlightOff.name}") {
                    val values = BacklightOffTime.entries
                    val index = values.indexOf(current.backlightOff)
                    onUpdate(current.copy(backlightOff = values[(index + 1) % values.size]))
                }
            }
            item {
                SettingOptionRow(this, transformationSpec, "Rotation: ${current.displayDir.name}") {
                    val values = DisplayDirection.entries
                    val index = values.indexOf(current.displayDir)
                    onUpdate(current.copy(displayDir = values[(index + 1) % values.size]))
                }
            }
            item { SettingOptionRow(this, transformationSpec, "Save to device", onApply) }
        }
    }
}
