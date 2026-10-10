package com.ngxyzaudax.radiacode.presentation.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.ngxyzaudax.radiacode.format.formatExposureDuration
import com.ngxyzaudax.radiacode.presentation.components.MetricStat
import com.ngxyzaudax.radiacode.presentation.components.MetricStatRow
import com.ngxyzaudax.radiacode.presentation.components.ScreenHeader
import com.ngxyzaudax.radiacode.presentation.components.TrackActionButtons
import com.ngxyzaudax.radiacode.presentation.theme.MetricPalette
import com.ngxyzaudax.radiacode.track.readTrackSummary
import java.io.File

private val SECTION_PADDING = 8.dp

@Composable
fun TrackDetailScreen(
    file: File,
    onDelete: () -> Unit,
): Unit {
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val summary = remember(file.absolutePath, file.lastModified()) { readTrackSummary(file) }
    ScreenScaffold(scrollState = listState) { padding ->
        TransformingLazyColumn(state = listState, contentPadding = padding) {
            item { ScreenHeader(this, transformationSpec, "Track") }
            item {
                Caption(
                    text = summary.displayName,
                    color = MetricPalette.muted,
                    modifier = Modifier.transformedHeight(this, transformationSpec),
                )
            }
            item {
                MetricStatRow(
                    stats = summaryStats(summary.durationSec, summary.pointCount),
                    modifier = Modifier
                        .transformedHeight(this, transformationSpec)
                        .padding(horizontal = SECTION_PADDING),
                )
            }
            item {
                TrackActionButtons(
                    onDelete = onDelete,
                    modifier = Modifier
                        .transformedHeight(this, transformationSpec)
                        .padding(top = SECTION_PADDING),
                )
            }
        }
    }
}

@Composable
private fun Caption(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
): Unit {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.padding(horizontal = SECTION_PADDING),
    )
}

private fun summaryStats(durationSec: Long, pointCount: Int): List<MetricStat> = listOf(
    MetricStat(formatExposureDuration(durationSec), "DURATION", MetricPalette.dose),
    MetricStat(pointCount.toString(), "POINTS", MetricPalette.count),
)
