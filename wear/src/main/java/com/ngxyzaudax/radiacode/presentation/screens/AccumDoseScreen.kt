package com.ngxyzaudax.radiacode.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.ngxyzaudax.radiacode.data.LiveReading
import com.ngxyzaudax.radiacode.device.MonitorState
import com.ngxyzaudax.radiacode.format.NO_VALUE
import com.ngxyzaudax.radiacode.format.accumulatedDoseSummary
import com.ngxyzaudax.radiacode.format.formatAverageDoseRate
import com.ngxyzaudax.radiacode.format.formatExposureDuration
import com.ngxyzaudax.radiacode.format.formatMagnitude
import com.ngxyzaudax.radiacode.presentation.components.MetricStat
import com.ngxyzaudax.radiacode.presentation.components.MetricStatRow
import com.ngxyzaudax.radiacode.presentation.components.ScreenActionButton
import com.ngxyzaudax.radiacode.presentation.components.ZonedGauge
import com.ngxyzaudax.radiacode.presentation.theme.MetricPalette
import com.ngxyzaudax.radiacode.presentation.theme.accumulatedDoseScale
import com.ngxyzaudax.radiacode.presentation.theme.ascendingZones
import com.ngxyzaudax.radiacode.presentation.theme.metricFraction
import com.ngxyzaudax.radiacode.protocol.doseAccumUnitLabel

private val SECTION_PADDING = 8.dp
private const val OFFLINE_LABEL: String = "NO LINK"

@Composable
fun AccumDoseScreen(
    monitorState: MonitorState?,
    reading: LiveReading,
    onResetDose: () -> Unit,
): Unit {
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val doseValue = monitorState?.accumulated?.dose ?: reading.accumulatedDose
    val durationSec = monitorState?.accumulated?.durationSecs ?: reading.accumulatedDurationSec
    val scale = accumulatedDoseScale(reading.alarmLimits)
    val zones = ascendingZones(scale)
    val summary = accumulatedDoseSummary(reading.copy(accumulatedDose = doseValue))
    val unit = doseAccumUnitLabel(reading.alarmLimits.doseUnit)
    val dangerLimit = reading.alarmLimits.l2Dose
    ScreenScaffold(scrollState = listState) { padding ->
        TransformingLazyColumn(state = listState, contentPadding = padding) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                    contentAlignment = Alignment.Center,
                ) {
                    ZonedGauge(fraction = metricFraction(doseValue, scale), zones = zones) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = summary.value,
                                style = MaterialTheme.typography.displaySmall,
                                color = summary.color,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = if (reading.connected) unit else OFFLINE_LABEL,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (reading.connected) MetricPalette.muted else MetricPalette.danger,
                            )
                        }
                    }
                }
            }
            item {
                MetricStatRow(
                    stats = listOf(
                        MetricStat(
                            value = formatExposureDuration(durationSec),
                            label = "EXPOSURE",
                            color = MetricPalette.muted,
                        ),
                        MetricStat(
                            value = formatAverageDoseRate(doseValue, durationSec, reading.doseLabel),
                            label = "AVG",
                            color = MetricPalette.dose,
                        ),
                        MetricStat(
                            value = if (dangerLimit > 0f) formatMagnitude(dangerLimit) else NO_VALUE,
                            label = "LIMIT",
                            color = MetricPalette.warn,
                        ),
                    ),
                    modifier = Modifier
                        .transformedHeight(this, transformationSpec)
                        .padding(horizontal = SECTION_PADDING),
                )
            }
            item {
                ScreenActionButton(this, transformationSpec, "Reset dose", onResetDose)
            }
        }
    }
}
