package com.ngxyzaudax.radiacode.presentation.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.EdgeButtonSize
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Picker
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.rememberPickerState
import com.ngxyzaudax.radiacode.presentation.components.AlarmSignalToggle
import com.ngxyzaudax.radiacode.presentation.theme.MetricPalette
import com.ngxyzaudax.radiacode.protocol.DeviceConfig

private val PICKER_WIDTH = 118.dp
private val PICKER_HEIGHT = 58.dp
private val TOGGLE_WIDTH = 78.dp
private val SAVE_RESERVED_HEIGHT = 42.dp
private val HEADER_TOP_PADDING = 22.dp

@Composable
fun AlarmEditorScreen(
    config: DeviceConfig?,
    key: AlarmKey,
    onUpdate: (DeviceConfig) -> Unit,
    onApply: () -> Unit,
    onBack: () -> Unit,
): Unit {
    val current = config ?: return
    val spec = alarmSpec(key)
    val scale = alarmStepScale(key, current.alarms)
    val pickerState = rememberPickerState(
        initialNumberOfOptions = scale.optionCount.coerceAtLeast(1),
        initiallySelectedIndex = scale.indexOf(spec.valueGetter(current.alarms)),
        shouldRepeatOptions = false,
    )
    var sound by remember { mutableStateOf(spec.soundGetter(current.soundCtrl)) }
    var vibro by remember { mutableStateOf(spec.vibroGetter(current.vibroCtrl)) }
    ScreenScaffold {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = HEADER_TOP_PADDING, bottom = SAVE_RESERVED_HEIGHT),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
            ) {
                EditorHeader(key, spec.unitLabel(current.alarms))
                Picker(
                    state = pickerState,
                    contentDescription = { scale.format(scale.valueAt(pickerState.selectedOptionIndex)) },
                    modifier = Modifier.size(PICKER_WIDTH, PICKER_HEIGHT),
                    gradientRatio = 0.2f,
                ) { index ->
                    Text(
                        text = scale.format(scale.valueAt(index)),
                        style = MaterialTheme.typography.numeralSmall,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AlarmSignalToggle("Sound", sound, TOGGLE_WIDTH) { sound = it }
                    AlarmSignalToggle("Vibro", vibro, TOGGLE_WIDTH) { vibro = it }
                }
            }
            EdgeButton(
                onClick = {
                    onUpdate(
                        applyAlarmEdit(
                            current,
                            key,
                            scale.valueAt(pickerState.selectedOptionIndex),
                            sound,
                            vibro,
                        ),
                    )
                    onApply()
                    onBack()
                },
                buttonSize = EdgeButtonSize.ExtraSmall,
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
private fun EditorHeader(key: AlarmKey, unit: String): Unit {
    Text(
        text = "${alarmSpec(key).levelLabel} · $unit",
        style = MaterialTheme.typography.labelSmall,
        color = levelAccent(key.level),
        textAlign = TextAlign.Center,
        maxLines = 1,
    )
}

private fun levelAccent(level: AlarmLevel) =
    when (level) {
        AlarmLevel.Warn -> MetricPalette.warn
        AlarmLevel.Danger -> MetricPalette.danger
    }
