package com.ngxyzaudax.radiacode.presentation.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.ngxyzaudax.radiacode.data.LiveReading
import com.ngxyzaudax.radiacode.device.MonitorState
import com.ngxyzaudax.radiacode.format.MetricSummary
import com.ngxyzaudax.radiacode.format.accumulatedDoseSummary
import com.ngxyzaudax.radiacode.format.batterySummary
import com.ngxyzaudax.radiacode.format.doseRateSummary
import com.ngxyzaudax.radiacode.format.temperatureSummary
import com.ngxyzaudax.radiacode.presentation.components.MetricStat
import com.ngxyzaudax.radiacode.presentation.components.MetricStatRow
import com.ngxyzaudax.radiacode.presentation.components.ScreenActionButton
import com.ngxyzaudax.radiacode.presentation.components.ZonedBars
import com.ngxyzaudax.radiacode.presentation.theme.MetricLevel
import com.ngxyzaudax.radiacode.presentation.theme.ascendingLevel
import com.ngxyzaudax.radiacode.presentation.theme.ascendingZones
import com.ngxyzaudax.radiacode.presentation.theme.doseRateScale
import com.ngxyzaudax.radiacode.presentation.theme.levelColor

private val SECTION_PADDING = 8.dp

@Composable
fun MonitorScreen(
    monitorState: MonitorState?,
    reading: LiveReading,
    onOpenSettings: () -> Unit,
): Unit {
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val doseValue = monitorState?.samples?.lastOrNull()?.doseRate ?: reading.doseRate
    val scale = doseRateScale(reading.alarmLimits)
    val zones = ascendingZones(scale)
    val level = ascendingLevel(doseValue, scale)
    ScreenScaffold(scrollState = listState) { padding ->
        TransformingLazyColumn(state = listState, contentPadding = padding) {
            item {
                MonitorHero(
                    reading = reading,
                    dose = doseRateSummary(reading),
                    doseValue = doseValue,
                    scale = scale,
                    zones = zones,
                    modifier = Modifier.transformedHeight(this, transformationSpec),
                )
            }
            if (level != MetricLevel.Normal) {
                item { AlarmBanner(level, this, transformationSpec) }
            }
            item {
                MetricStatRow(
                    stats = listOf(
                        accumulatedDoseSummary(reading).toStat(),
                        batterySummary(reading).toStat(),
                        temperatureSummary(reading).toStat(),
                    ),
                    modifier = Modifier
                        .transformedHeight(this, transformationSpec)
                        .padding(horizontal = SECTION_PADDING),
                )
            }
            item {
                ZonedBars(
                    history = reading.doseRateHistory,
                    scale = scale,
                    zones = zones,
                    modifier = Modifier
                        .transformedHeight(this, transformationSpec)
                        .padding(horizontal = SECTION_PADDING),
                )
            }
            item {
                ScreenActionButton(this, transformationSpec, "Settings", onOpenSettings)
            }
        }
    }
}

@Composable
private fun AlarmBanner(
    level: MetricLevel,
    scope: TransformingLazyColumnItemScope,
    transformationSpec: TransformationSpec,
): Unit = with(scope) {
    Text(
        text = if (level == MetricLevel.Danger) "DANGER" else "WARNING",
        style = MaterialTheme.typography.labelLarge,
        color = levelColor(level),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
    )
}

private fun MetricSummary.toStat(): MetricStat =
    MetricStat(value = value, label = label, color = color)
