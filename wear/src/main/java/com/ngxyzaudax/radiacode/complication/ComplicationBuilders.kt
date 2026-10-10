package com.ngxyzaudax.radiacode.complication

import android.graphics.Bitmap
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.GoalProgressComplicationData
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.SmallImage
import androidx.wear.watchface.complications.data.SmallImageComplicationData
import androidx.wear.watchface.complications.data.SmallImageType
import androidx.wear.watchface.complications.data.WeightedElementsComplicationData
import com.ngxyzaudax.radiacode.presentation.theme.MetricZone
import com.ngxyzaudax.radiacode.presentation.theme.zoneColorAt
import com.ngxyzaudax.radiacode.presentation.theme.zoneColorRamp

private const val FRACTION_MAX: Float = 1f

/**
 * Every ranged complication is expressed as a 0..1 fraction of its metric scale, which keeps the
 * colour ramp aligned with the zone boundaries regardless of the underlying physical units.
 */
internal fun rangedComplication(
    title: String,
    fraction: Float,
    zones: List<MetricZone>,
    text: String,
): ComplicationData =
    RangedValueComplicationData.Builder(
        value = fraction.coerceIn(0f, FRACTION_MAX),
        min = 0f,
        max = FRACTION_MAX,
        contentDescription = PlainComplicationText.Builder(title).build(),
    )
        .setText(PlainComplicationText.Builder(text).build())
        .setTitle(PlainComplicationText.Builder(title).build())
        .setColorRamp(zoneColorRamp(zones))
        .build()

internal fun goalComplication(
    title: String,
    fraction: Float,
    zones: List<MetricZone>,
    text: String,
): ComplicationData =
    GoalProgressComplicationData.Builder(
        value = fraction.coerceAtLeast(0f),
        targetValue = FRACTION_MAX,
        contentDescription = PlainComplicationText.Builder(title).build(),
    )
        .setText(PlainComplicationText.Builder(text).build())
        .setTitle(PlainComplicationText.Builder(title).build())
        .setColorRamp(zoneColorRamp(zones))
        .build()

internal fun shortComplication(title: String, body: String): ComplicationData =
    ShortTextComplicationData.Builder(
        text = PlainComplicationText.Builder(body).build(),
        contentDescription = PlainComplicationText.Builder(title).build(),
    ).setTitle(PlainComplicationText.Builder(title).build()).build()

internal fun longComplication(title: String, body: String): ComplicationData =
    LongTextComplicationData.Builder(
        text = PlainComplicationText.Builder(body).build(),
        contentDescription = PlainComplicationText.Builder(title).build(),
    ).setTitle(PlainComplicationText.Builder(title).build()).build()

internal fun gaugeImageComplication(
    title: String,
    fraction: Float,
    zones: List<MetricZone>,
    value: String = "",
    caption: String = "",
): ComplicationData =
    imageComplication(title, renderGaugeBitmap(fraction, zones, value, caption))

internal fun weightedZoneComplication(
    title: String,
    fractions: List<Float>,
    zones: List<MetricZone>,
): ComplicationData {
    if (fractions.isEmpty()) return shortComplication(title, "--")
    return WeightedElementsComplicationData.Builder(
        elements = fractions.map { fraction ->
            WeightedElementsComplicationData.Element(1f, zoneColorAt(fraction, zones).value.toInt())
        },
        contentDescription = PlainComplicationText.Builder(title).build(),
    ).build()
}

internal fun imageComplication(title: String, bitmap: Bitmap): ComplicationData =
    SmallImageComplicationData.Builder(
        SmallImage.Builder(Icon.createWithBitmap(bitmap), SmallImageType.PHOTO).build(),
        PlainComplicationText.Builder(title).build(),
    ).build()
