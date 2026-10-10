package com.ngxyzaudax.radiacode.tile

import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.LayoutElementBuilders.Box
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.LayoutElementBuilders.Row
import androidx.wear.protolayout.LayoutElementBuilders.VERTICAL_ALIGN_BOTTOM
import androidx.wear.protolayout.modifiers.LayoutModifier
import androidx.wear.protolayout.modifiers.background
import androidx.wear.protolayout.modifiers.clip
import androidx.wear.protolayout.modifiers.toProtoLayoutModifiers
import androidx.wear.protolayout.types.LayoutColor
import com.ngxyzaudax.radiacode.presentation.theme.MetricPalette
import com.ngxyzaudax.radiacode.presentation.theme.MetricScale
import com.ngxyzaudax.radiacode.presentation.theme.MetricZone
import com.ngxyzaudax.radiacode.presentation.theme.metricFraction
import com.ngxyzaudax.radiacode.presentation.theme.zoneColorAt

private const val BAR_WIDTH_DP: Float = 4f
private const val BAR_SPACING_DP: Float = 2f
private const val BAR_CORNER_DP: Float = 2f
private const val MIN_BAR_HEIGHT_DP: Float = 2f

/**
 * Bar chart of recent samples where each bar is tinted by the zone its own value falls into, so the
 * history shows when a reading crossed a threshold rather than only its shape.
 */
internal fun historyBars(
    history: List<Float>,
    scale: MetricScale,
    zones: List<MetricZone>,
    heightDp: Float,
    barCount: Int,
): LayoutElement {
    val samples = history.takeLast(barCount)
    if (samples.isEmpty()) return emptyBaseline(barCount)
    val builder = Row.Builder()
        .setWidth(dp(barSpanDp(samples.size)))
        .setHeight(dp(heightDp))
        .setVerticalAlignment(VERTICAL_ALIGN_BOTTOM)
    samples.forEachIndexed { index, value ->
        if (index > 0) builder.addContent(barSpacer())
        val fraction = metricFraction(value, scale)
        builder.addContent(bar(fraction, heightDp, zoneColorAt(fraction, zones).layoutColor()))
    }
    return builder.build()
}

private fun bar(fraction: Float, heightDp: Float, color: LayoutColor): Box =
    filledBox(
        widthDp = BAR_WIDTH_DP,
        heightDp = (heightDp * fraction).coerceAtLeast(MIN_BAR_HEIGHT_DP),
        color = color,
    )

private fun barSpacer(): Box =
    Box.Builder().setWidth(dp(BAR_SPACING_DP)).setHeight(dp(MIN_BAR_HEIGHT_DP)).build()

private fun emptyBaseline(barCount: Int): LayoutElement =
    filledBox(
        widthDp = barSpanDp(barCount),
        heightDp = MIN_BAR_HEIGHT_DP,
        color = MetricPalette.track.layoutColor(),
    )

private fun filledBox(widthDp: Float, heightDp: Float, color: LayoutColor): Box =
    Box.Builder()
        .setWidth(dp(widthDp))
        .setHeight(dp(heightDp))
        .setModifiers(
            LayoutModifier.background(color).clip(BAR_CORNER_DP).toProtoLayoutModifiers(),
        )
        .build()

private fun barSpanDp(count: Int): Float =
    count * BAR_WIDTH_DP + (count - 1).coerceAtLeast(0) * BAR_SPACING_DP
