package com.ngxyzaudax.radiacode.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.ngxyzaudax.radiacode.presentation.theme.MetricPalette

private val CARD_CORNER = 18.dp
private val CARD_PADDING = 8.dp
private val ROW_GAP = 6.dp

data class MetricStat(
    val value: String,
    val label: String,
    val color: Color,
)

/** Compact value-over-label card, the in-app counterpart of the status tile cells. */
@Composable
fun MetricStatCard(stat: MetricStat, modifier: Modifier = Modifier): Unit {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(CARD_CORNER))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(CARD_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stat.value,
            style = MaterialTheme.typography.titleMedium,
            color = stat.color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stat.label,
            style = MaterialTheme.typography.labelSmall,
            color = MetricPalette.muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun MetricStatRow(stats: List<MetricStat>, modifier: Modifier = Modifier): Unit {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ROW_GAP),
    ) {
        stats.forEach { stat ->
            MetricStatCard(stat = stat, modifier = Modifier.weight(1f))
        }
    }
}
