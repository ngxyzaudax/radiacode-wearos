package com.ngxyzaudax.radiacode.complication

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.ui.graphics.toArgb
import com.ngxyzaudax.radiacode.presentation.theme.MetricPalette
import com.ngxyzaudax.radiacode.presentation.theme.MetricZone
import com.ngxyzaudax.radiacode.presentation.theme.zoneColorAt

private const val BITMAP_SIZE: Int = 192
private const val SWEEP_START_DEGREES: Float = 160f
private const val SWEEP_DEGREES: Float = 220f
private const val BAND_STROKE: Float = 7f
private const val VALUE_STROKE: Float = 18f
private const val BAND_TO_VALUE_GAP: Float = 6f
private const val BAND_GAP_DEGREES: Float = 4f
private const val VALUE_BASELINE_RATIO: Float = 0.52f
private const val CAPTION_BASELINE_RATIO: Float = 0.68f

/**
 * Complication gauge: an outer colour-coded scale ring, an inner value ring, and the reading itself
 * in the middle so the image is legible without any surrounding text slot.
 */
fun renderGaugeBitmap(
    fraction: Float,
    zones: List<MetricZone>,
    value: String = "",
    caption: String = "",
): Bitmap {
    val bitmap = Bitmap.createBitmap(BITMAP_SIZE, BITMAP_SIZE, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val color = zoneColorAt(fraction, zones)
    drawBands(canvas, zones)
    drawValueRing(canvas, fraction, color.toArgb())
    val center = BITMAP_SIZE / 2f
    if (value.isNotEmpty()) {
        drawCenteredValue(canvas, value, color, center, BITMAP_SIZE * VALUE_BASELINE_RATIO)
    }
    if (caption.isNotEmpty()) {
        drawCenteredCaption(
            canvas,
            caption,
            MetricPalette.muted,
            center,
            BITMAP_SIZE * CAPTION_BASELINE_RATIO,
        )
    }
    return bitmap
}

private fun drawBands(canvas: Canvas, zones: List<MetricZone>): Unit {
    val rect = arcRect(BAND_STROKE / 2f)
    zones.forEach { zone ->
        val start = SWEEP_START_DEGREES + SWEEP_DEGREES * zone.startFraction
        val sweep = SWEEP_DEGREES * (zone.endFraction - zone.startFraction)
        canvas.drawArc(
            rect,
            start + BAND_GAP_DEGREES / 2f,
            (sweep - BAND_GAP_DEGREES).coerceAtLeast(1f),
            false,
            strokePaint(zone.color.toArgb(), BAND_STROKE, rounded = false),
        )
    }
}

private fun drawValueRing(canvas: Canvas, fraction: Float, colorArgb: Int): Unit {
    val rect = arcRect(BAND_STROKE + BAND_TO_VALUE_GAP + VALUE_STROKE / 2f)
    canvas.drawArc(
        rect,
        SWEEP_START_DEGREES,
        SWEEP_DEGREES,
        false,
        strokePaint(MetricPalette.track.toArgb(), VALUE_STROKE, rounded = true),
    )
    canvas.drawArc(
        rect,
        SWEEP_START_DEGREES,
        SWEEP_DEGREES * fraction.coerceIn(0f, 1f),
        false,
        strokePaint(colorArgb, VALUE_STROKE, rounded = true),
    )
}

private fun arcRect(inset: Float): RectF =
    RectF(inset, inset, BITMAP_SIZE - inset, BITMAP_SIZE - inset)

private fun strokePaint(colorArgb: Int, width: Float, rounded: Boolean): Paint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorArgb
        style = Paint.Style.STROKE
        strokeWidth = width
        strokeCap = if (rounded) Paint.Cap.ROUND else Paint.Cap.BUTT
    }
