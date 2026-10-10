package com.ngxyzaudax.radiacode.presentation.screens.settings

import androidx.compose.runtime.Composable
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import com.ngxyzaudax.radiacode.presentation.components.ScreenHeader
import com.ngxyzaudax.radiacode.presentation.components.SettingOptionRow
import com.ngxyzaudax.radiacode.presentation.components.SettingSectionCaption
import com.ngxyzaudax.radiacode.protocol.DeviceConfig

@Composable
fun AlarmSettingsScreen(
    config: DeviceConfig?,
    onNavigateEditor: (AlarmKey) -> Unit,
): Unit {
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val current = config ?: return
    ScreenScaffold(scrollState = listState) { padding ->
        TransformingLazyColumn(state = listState, contentPadding = padding) {
            item { ScreenHeader(this, transformationSpec, "Alarms") }
            alarmKeysByMetric().forEach { (metric, keys) ->
                item { SettingSectionCaption(this, transformationSpec, metricSectionLabel(metric)) }
                keys.forEach { key ->
                    item {
                        val spec = alarmSpec(key)
                        val scale = alarmStepScale(key, current.alarms)
                        val value = spec.valueGetter(current.alarms)
                        val unit = spec.unitLabel(current.alarms)
                        val markers = signalMarkers(
                            spec.soundGetter(current.soundCtrl),
                            spec.vibroGetter(current.vibroCtrl),
                        )
                        SettingOptionRow(
                            this,
                            transformationSpec,
                            "${spec.levelLabel} ${scale.format(value)} $unit  $markers",
                        ) {
                            onNavigateEditor(key)
                        }
                    }
                }
            }
        }
    }
}
