package com.ngxyzaudax.radiacode.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.ngxyzaudax.radiacode.data.LiveReading
import com.ngxyzaudax.radiacode.format.doseRateSummary
import com.ngxyzaudax.radiacode.format.formatMagnitude
import com.ngxyzaudax.radiacode.presentation.components.MetricStat
import com.ngxyzaudax.radiacode.presentation.components.MetricStatRow
import com.ngxyzaudax.radiacode.presentation.components.ScreenActionButton
import com.ngxyzaudax.radiacode.presentation.components.ScreenHeader
import com.ngxyzaudax.radiacode.presentation.theme.MetricPalette
import java.io.File

private val SECTION_PADDING = 8.dp

@Composable
fun TrackScreen(
    reading: LiveReading,
    tracks: List<File>,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onOpenTrack: (File) -> Unit,
    onExport: (() -> Unit)?,
): Unit {
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    ScreenScaffold(scrollState = listState) { padding ->
        TransformingLazyColumn(state = listState, contentPadding = padding) {
            item { ScreenHeader(this, transformationSpec, "Track") }
            item {
                TrackPointCount(
                    reading = reading,
                    modifier = Modifier.transformedHeight(this, transformationSpec),
                )
            }
            item {
                MetricStatRow(
                    stats = trackStats(reading),
                    modifier = Modifier
                        .transformedHeight(this, transformationSpec)
                        .padding(horizontal = SECTION_PADDING),
                )
            }
            item {
                ScreenActionButton(
                    this,
                    transformationSpec,
                    if (reading.tracking) "Stop recording" else "Start GPS track",
                    if (reading.tracking) onStop else onStart,
                    emphasized = true,
                )
            }
            if (onExport != null) {
                item {
                    ScreenActionButton(this, transformationSpec, "Export to phone", onExport)
                }
            }
            items(tracks) { file ->
                ScreenActionButton(
                    this,
                    transformationSpec,
                    file.nameWithoutExtension,
                    { onOpenTrack(file) },
                )
            }
        }
    }
}

@Composable
private fun TrackPointCount(reading: LiveReading, modifier: Modifier = Modifier): Unit {
    val accent = if (reading.tracking) MetricPalette.danger else MetricPalette.safe
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "${reading.trackPointCount}",
            style = MaterialTheme.typography.displayMedium,
            color = accent,
        )
        Text(
            text = if (reading.tracking) "RECORDING" else "POINTS",
            style = MaterialTheme.typography.labelSmall,
            color = MetricPalette.muted,
        )
    }
}

private fun trackStats(reading: LiveReading): List<MetricStat> {
    val dose = doseRateSummary(reading)
    return listOf(
        MetricStat(dose.value, dose.label, dose.color),
        gpsStat(reading),
    )
}

private fun gpsStat(reading: LiveReading): MetricStat =
    MetricStat(
        value = reading.gpsAccuracyM?.let { "±${formatMagnitude(it)}" } ?: "--",
        label = "GPS m",
        color = if (reading.gpsAccuracyM != null) MetricPalette.dose else MetricPalette.muted,
    )
