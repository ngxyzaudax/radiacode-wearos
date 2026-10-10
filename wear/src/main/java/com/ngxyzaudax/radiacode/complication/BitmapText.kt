package com.ngxyzaudax.radiacode.complication

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

private const val WIDE_VALUE_LENGTH: Int = 4
private const val VALUE_TEXT_SIZE: Float = 40f
private const val NARROW_VALUE_TEXT_SIZE: Float = 46f
private const val CAPTION_TEXT_SIZE: Float = 18f

/**
 * A ring alone tells the wearer nothing without a number, so image complications carry their reading
 * baked in. Wide values step down a size so they stay inside the ring opening.
 */
internal fun drawCenteredValue(
    canvas: Canvas,
    value: String,
    color: Color,
    centerX: Float,
    baselineY: Float,
): Unit {
    val size = if (value.length >= WIDE_VALUE_LENGTH) VALUE_TEXT_SIZE else NARROW_VALUE_TEXT_SIZE
    canvas.drawText(value, centerX, baselineY, valuePaint(color.toArgb(), size))
}

internal fun drawCenteredCaption(
    canvas: Canvas,
    caption: String,
    color: Color,
    centerX: Float,
    baselineY: Float,
): Unit = canvas.drawText(caption, centerX, baselineY, captionPaint(color.toArgb()))

private fun valuePaint(colorArgb: Int, textSize: Float): Paint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorArgb
        this.textSize = textSize
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

private fun captionPaint(colorArgb: Int): Paint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorArgb
        textSize = CAPTION_TEXT_SIZE
        textAlign = Paint.Align.CENTER
    }
