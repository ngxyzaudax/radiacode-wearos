package com.ngxyzaudax.radiacode.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ngxyzaudax.radiacode.presentation.theme.MetricPalette
import com.ngxyzaudax.radiacode.presentation.theme.MetricScale
import com.ngxyzaudax.radiacode.presentation.theme.MetricZone
import com.ngxyzaudax.radiacode.presentation.theme.dangerFraction
import com.ngxyzaudax.radiacode.presentation.theme.metricFraction
import com.ngxyzaudax.radiacode.presentation.theme.warnFraction
import com.ngxyzaudax.radiacode.presentation.theme.zoneColorAt

private val BAR_GAP: Dp = 2.dp
private val BAR_CORNER: Dp = 2.dp
private val MIN_BAR_HEIGHT: Dp = 2.dp
private const val THRESHOLD_ALPHA: Float = 0.45f
private val THRESHOLD_STROKE: Dp = 1.dp

/**
 * History as zone-tinted bars with the alarm limits drawn across them, so the chart shows when a
 * reading crossed a threshold rather than only the shape of the curve.
 */
@Composable
fun ZonedBars(
    history: List<Float>,
    scale: MetricScale,
    zones: List<MetricZone>,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp,
): Unit {
    Canvas(modifier = modifier.fillMaxWidth().height(height)) {
        drawThreshold(scale.warnFraction, MetricPalette.warn)
        drawThreshold(scale.dangerFraction, MetricPalette.danger)
        if (history.isEmpty()) drawBaseline() else drawBars(history, scale, zones)
    }
}

private fun DrawScope.drawBars(
    history: List<Float>,
    scale: MetricScale,
    zones: List<MetricZone>,
): Unit {
    val gap = BAR_GAP.toPx()
    val slot = size.width / history.size
    val barWidth = (slot - gap).coerceAtLeast(1f)
    val minHeight = MIN_BAR_HEIGHT.toPx()
    history.forEachIndexed { index, value ->
        val fraction = metricFraction(value, scale)
        val barHeight = (size.height * fraction).coerceAtLeast(minHeight)
        drawRoundRect(
            color = zoneColorAt(fraction, zones),
            topLeft = Offset(index * slot + gap / 2f, size.height - barHeight),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(BAR_CORNER.toPx()),
        )
    }
}

private fun DrawScope.drawBaseline(): Unit {
    val minHeight = MIN_BAR_HEIGHT.toPx()
    drawRoundRect(
        color = MetricPalette.track,
        topLeft = Offset(0f, size.height - minHeight),
        size = Size(size.width, minHeight),
        cornerRadius = CornerRadius(BAR_CORNER.toPx()),
    )
}

private fun DrawScope.drawThreshold(fraction: Float, color: Color): Unit {
    if (fraction <= 0f) return
    val y = size.height - size.height * fraction.coerceIn(0f, 1f)
    drawLine(
        color = color.copy(alpha = THRESHOLD_ALPHA),
        start = Offset(0f, y),
        end = Offset(size.width, y),
        strokeWidth = THRESHOLD_STROKE.toPx(),
    )
}
