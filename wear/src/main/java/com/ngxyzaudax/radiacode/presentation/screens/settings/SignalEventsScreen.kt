package com.ngxyzaudax.radiacode.presentation.screens.settings

import androidx.compose.runtime.Composable
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.CheckboxButton
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import com.ngxyzaudax.radiacode.presentation.components.ScreenHeader
import com.ngxyzaudax.radiacode.presentation.components.SettingOptionRow
import com.ngxyzaudax.radiacode.protocol.DeviceConfig

@Composable
fun SignalEventsScreen(
    title: String,
    config: DeviceConfig?,
    kind: SignalEventKind,
    specs: List<SignalEventSpec>,
    onUpdate: (DeviceConfig) -> Unit,
    onApply: () -> Unit,
): Unit {
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val current = config ?: return
    ScreenScaffold(scrollState = listState) { padding ->
        TransformingLazyColumn(state = listState, contentPadding = padding) {
            item { ScreenHeader(this, transformationSpec, title) }
            specs.forEach { spec ->
                item {
                    val flags = signalFlags(current, kind)
                    val checked = spec.getter(flags)
                    CheckboxButton(
                        checked = checked,
                        onCheckedChange = { enabled ->
                            onUpdate(updateSignalFlags(current, kind, spec, enabled))
                        },
                        label = { Text(spec.label) },
                    )
                }
            }
            item { SettingOptionRow(this, transformationSpec, "Save to device", onApply) }
        }
    }
}
