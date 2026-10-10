package com.ngxyzaudax.radiacode.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.ngxyzaudax.radiacode.presentation.theme.MetricPalette
import com.ngxyzaudax.radiacode.protocol.Spectrum

private val GRID_FRACTIONS: List<Float> = listOf(0.25f, 0.5f, 0.75f)
private val LINE_WIDTH = 2.dp
private val GRID_ALPHA = 0.18f

@Composable
fun SpectrumChart(
    spectrum: Spectrum?,
    logarithmic: Boolean,
    modifier: Modifier = Modifier,
): Unit {
    Canvas(modifier = modifier.fillMaxWidth().height(120.dp)) {
        val gridColor = MetricPalette.muted.copy(alpha = GRID_ALPHA)
        GRID_FRACTIONS.forEach { fraction ->
            val y = size.height * (1f - fraction)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f,
            )
        }
        val points = spectrumPoints(spectrum, size.width, size.height, logarithmic)
        if (points.isEmpty()) return@Canvas
        val path: Path = spectrumPath(points)
        drawPath(
            path = path,
            color = Color(0xFF7AD4FF),
            style = Stroke(
                width = LINE_WIDTH.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}
