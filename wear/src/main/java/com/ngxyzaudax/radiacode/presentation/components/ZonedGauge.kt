package com.ngxyzaudax.radiacode.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ngxyzaudax.radiacode.presentation.theme.MetricPalette
import com.ngxyzaudax.radiacode.presentation.theme.MetricZone
import com.ngxyzaudax.radiacode.presentation.theme.zoneColorAt

private const val SWEEP_START_DEGREES: Float = 160f
private const val SWEEP_DEGREES: Float = 220f
private const val BAND_GAP_DEGREES: Float = 4f

private val BAND_STROKE: Dp = 4.dp
private val VALUE_STROKE: Dp = 12.dp
private val BAND_TO_VALUE_GAP: Dp = 7.dp

/**
 * In-app twin of the tile gauge: a thin outer ring carrying the whole colour-coded scale, and a
 * thick inner ring showing where the reading sits within it. Sharing the geometry and palette with
 * the tiles means a glance at either surface reads the same way.
 */
@Composable
fun ZonedGauge(
    fraction: Float,
    zones: List<MetricZone>,
    modifier: Modifier = Modifier,
    diameter: Dp = 168.dp,
    content: @Composable () -> Unit = {},
): Unit {
    Box(modifier = modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawZoneBands(zones)
            drawValueRing(fraction, zoneColorAt(fraction, zones))
        }
        content()
    }
}

private fun DrawScope.drawZoneBands(zones: List<MetricZone>): Unit {
    val stroke = BAND_STROKE.toPx()
    zones.forEach { zone ->
        val start = SWEEP_START_DEGREES + SWEEP_DEGREES * zone.startFraction
        val sweep = SWEEP_DEGREES * (zone.endFraction - zone.startFraction)
        drawArc(
            color = zone.color,
            startAngle = start + BAND_GAP_DEGREES / 2f,
            sweepAngle = (sweep - BAND_GAP_DEGREES).coerceAtLeast(1f),
            useCenter = false,
            topLeft = Offset(stroke / 2f, stroke / 2f),
            size = Size(size.width - stroke, size.height - stroke),
            style = Stroke(width = stroke, cap = StrokeCap.Butt),
        )
    }
}

private fun DrawScope.drawValueRing(fraction: Float, color: Color): Unit {
    val stroke = VALUE_STROKE.toPx()
    val inset = BAND_STROKE.toPx() + BAND_TO_VALUE_GAP.toPx() + stroke / 2f
    val arcSize = Size(size.width - inset * 2f, size.height - inset * 2f)
    val topLeft = Offset(inset, inset)
    drawArc(
        color = MetricPalette.track,
        startAngle = SWEEP_START_DEGREES,
        sweepAngle = SWEEP_DEGREES,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = stroke, cap = StrokeCap.Round),
    )
    drawArc(
        color = color,
        startAngle = SWEEP_START_DEGREES,
        sweepAngle = SWEEP_DEGREES * fraction.coerceIn(0f, 1f),
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = stroke, cap = StrokeCap.Round),
    )
}
