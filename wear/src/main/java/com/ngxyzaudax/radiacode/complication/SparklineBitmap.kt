package com.ngxyzaudax.radiacode.complication

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.ngxyzaudax.radiacode.presentation.theme.MetricPalette
import com.ngxyzaudax.radiacode.presentation.theme.MetricScale
import com.ngxyzaudax.radiacode.presentation.theme.MetricZone
import com.ngxyzaudax.radiacode.presentation.theme.dangerFraction
import com.ngxyzaudax.radiacode.presentation.theme.metricFraction
import com.ngxyzaudax.radiacode.presentation.theme.warnFraction
import com.ngxyzaudax.radiacode.presentation.theme.zoneColorAt

private const val WIDTH: Int = 192
private const val HEIGHT: Int = 192
private const val BAR_GAP: Float = 2f
private const val MIN_BAR_HEIGHT: Float = 3f
private const val CORNER_RADIUS: Float = 2f
private const val THRESHOLD_STROKE: Float = 2f
private const val THRESHOLD_ALPHA: Int = 110
private const val CHART_TOP_RATIO: Float = 0.46f
private const val VALUE_BASELINE_RATIO: Float = 0.32f
private const val CAPTION_BASELINE_RATIO: Float = 0.42f

/**
 * History bars with the current reading above them. Each bar carries the colour of the alarm band
 * its own sample falls into, so a threshold crossing stays visible in the trend.
 */
fun renderSparklineBitmap(
    history: List<Float>,
    scale: MetricScale,
    zones: List<MetricZone>,
    value: String = "",
    caption: String = "",
): Bitmap {
    val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val chartTop = HEIGHT * CHART_TOP_RATIO
    drawThreshold(canvas, scale.warnFraction, MetricPalette.warn, chartTop)
    drawThreshold(canvas, scale.dangerFraction, MetricPalette.danger, chartTop)
    drawBars(canvas, history, scale, zones, chartTop)
    drawHeader(canvas, history, scale, zones, value, caption)
    return bitmap
}

private fun drawHeader(
    canvas: Canvas,
    history: List<Float>,
    scale: MetricScale,
    zones: List<MetricZone>,
    value: String,
    caption: String,
): Unit {
    val center = WIDTH / 2f
    if (value.isNotEmpty()) {
        val latest = history.lastOrNull() ?: 0f
        val color = zoneColorAt(metricFraction(latest, scale), zones)
        drawCenteredValue(canvas, value, color, center, HEIGHT * VALUE_BASELINE_RATIO)
    }
    if (caption.isNotEmpty()) {
        drawCenteredCaption(
            canvas,
            caption,
            MetricPalette.muted,
            center,
            HEIGHT * CAPTION_BASELINE_RATIO,
        )
    }
}

private fun drawBars(
    canvas: Canvas,
    history: List<Float>,
    scale: MetricScale,
    zones: List<MetricZone>,
    chartTop: Float,
): Unit {
    if (history.isEmpty()) return
    val chartHeight = HEIGHT - chartTop
    val slot = WIDTH.toFloat() / history.size
    history.forEachIndexed { index, sample ->
        val fraction = metricFraction(sample, scale)
        val barHeight = (chartHeight * fraction).coerceAtLeast(MIN_BAR_HEIGHT)
        canvas.drawRoundRect(
            index * slot + BAR_GAP / 2f,
            HEIGHT - barHeight,
            (index + 1) * slot - BAR_GAP / 2f,
            HEIGHT.toFloat(),
            CORNER_RADIUS,
            CORNER_RADIUS,
            fillPaint(zoneColorAt(fraction, zones).toArgb()),
        )
    }
}

private fun drawThreshold(
    canvas: Canvas,
    fraction: Float,
    color: Color,
    chartTop: Float,
): Unit {
    if (fraction <= 0f) return
    val y = HEIGHT - (HEIGHT - chartTop) * fraction.coerceIn(0f, 1f)
    canvas.drawLine(0f, y, WIDTH.toFloat(), y, thresholdPaint(color.toArgb()))
}

private fun fillPaint(colorArgb: Int): Paint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorArgb }

private fun thresholdPaint(colorArgb: Int): Paint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorArgb
        alpha = THRESHOLD_ALPHA
        strokeWidth = THRESHOLD_STROKE
    }
