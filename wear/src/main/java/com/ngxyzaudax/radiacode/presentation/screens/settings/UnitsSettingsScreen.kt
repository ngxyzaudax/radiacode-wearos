package com.ngxyzaudax.radiacode.presentation.screens.settings

import androidx.compose.runtime.Composable
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import com.ngxyzaudax.radiacode.presentation.components.ScreenHeader
import com.ngxyzaudax.radiacode.presentation.components.SettingOptionRow
import com.ngxyzaudax.radiacode.protocol.CountDisplayUnit
import com.ngxyzaudax.radiacode.protocol.DeviceConfig
import com.ngxyzaudax.radiacode.protocol.DoseDisplayUnit

@Composable
fun UnitsSettingsScreen(
    config: DeviceConfig?,
    onUpdate: (DeviceConfig) -> Unit,
    onApply: () -> Unit,
): Unit {
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val current = config ?: return
    ScreenScaffold(scrollState = listState) { padding ->
        TransformingLazyColumn(state = listState, contentPadding = padding) {
            item { ScreenHeader(this, transformationSpec, "Units") }
            item {
                SettingOptionRow(
                    this,
                    transformationSpec,
                    if (current.alarms.doseUnit == DoseDisplayUnit.MicroSievertPerHour) "Dose: µSv/h" else "Dose: µR/h",
                ) {
                    val nextUnit = if (current.alarms.doseUnit == DoseDisplayUnit.MicroSievertPerHour) {
                        DoseDisplayUnit.MicroRoentgenPerHour
                    } else {
                        DoseDisplayUnit.MicroSievertPerHour
                    }
                    onUpdate(current.copy(alarms = current.alarms.copy(doseUnit = nextUnit)))
                }
            }
            item {
                SettingOptionRow(
                    this,
                    transformationSpec,
                    if (current.alarms.countUnit == CountDisplayUnit.Cps) "Count: CPS" else "Count: CPM",
                ) {
                    val nextUnit = if (current.alarms.countUnit == CountDisplayUnit.Cps) {
                        CountDisplayUnit.Cpm
                    } else {
                        CountDisplayUnit.Cps
                    }
                    onUpdate(current.copy(alarms = current.alarms.copy(countUnit = nextUnit)))
                }
            }
            item { SettingOptionRow(this, transformationSpec, "Save to device", onApply) }
        }
    }
}
