package com.ngxyzaudax.radiacode.tile

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.wear.protolayout.ColorBuilders.ColorProp
import androidx.wear.protolayout.DimensionBuilders.degrees
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.LayoutElementBuilders.ARC_ANCHOR_START
import androidx.wear.protolayout.LayoutElementBuilders.ARC_DIRECTION_CLOCKWISE
import androidx.wear.protolayout.LayoutElementBuilders.Arc
import androidx.wear.protolayout.LayoutElementBuilders.ArcLine
import androidx.wear.protolayout.LayoutElementBuilders.Box
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.LayoutElementBuilders.STROKE_CAP_BUTT
import androidx.wear.protolayout.LayoutElementBuilders.STROKE_CAP_ROUND
import androidx.wear.protolayout.LayoutElementBuilders.VERTICAL_ALIGN_CENTER
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.ProgressIndicatorColors
import androidx.wear.protolayout.material3.circularProgressIndicator
import com.ngxyzaudax.radiacode.presentation.theme.MetricPalette
import com.ngxyzaudax.radiacode.presentation.theme.MetricZone
import com.ngxyzaudax.radiacode.presentation.theme.zoneColorAt

internal const val GAUGE_START_DEGREES: Float = 200f
internal const val GAUGE_SWEEP_DEGREES: Float = 320f
private const val BAND_GAP_DEGREES: Float = 5f
private const val BAND_THICKNESS_DP: Float = 5f
private const val BAND_TO_VALUE_INSET_DP: Float = 26f

/**
 * Ring stack that mirrors the range-band idiom used by Samsung Health gauges: a thin outer ring
 * shows the whole colour-coded scale at once, and a thick inner ring shows where the current
 * reading sits within it.
 */
internal fun MaterialScope.zonedGauge(
    fraction: Float,
    zones: List<MetricZone>,
    diameterDp: Float,
    valueStrokeDp: Float,
    innerRings: List<Box> = emptyList(),
    center: (MaterialScope.() -> LayoutElement)? = null,
): Box {
    val builder = Box.Builder().setWidth(dp(diameterDp)).setHeight(dp(diameterDp))
    zoneBands(zones).forEach(builder::addContent)
    builder.addContent(valueRing(fraction, zoneColorAt(fraction, zones), diameterDp, valueStrokeDp))
    innerRings.forEach(builder::addContent)
    center?.let { builder.addContent(it()) }
    return builder.build()
}

internal fun zoneBands(zones: List<MetricZone>): List<Arc> = zones.map(::zoneBandArc)

internal fun valueRingDiameterDp(outerDiameterDp: Float): Float =
    outerDiameterDp - BAND_TO_VALUE_INSET_DP

private fun zoneBandArc(zone: MetricZone): Arc {
    val start = GAUGE_START_DEGREES + GAUGE_SWEEP_DEGREES * zone.startFraction
    val sweep = GAUGE_SWEEP_DEGREES * (zone.endFraction - zone.startFraction)
    return Arc.Builder()
        .setAnchorAngle(degrees(start + BAND_GAP_DEGREES / 2f))
        .setAnchorType(ARC_ANCHOR_START)
        .setArcDirection(ARC_DIRECTION_CLOCKWISE)
        .setVerticalAlign(VERTICAL_ALIGN_CENTER)
        .addContent(
            ArcLine.Builder()
                .setLength(degrees((sweep - BAND_GAP_DEGREES).coerceAtLeast(1f)))
                .setThickness(dp(BAND_THICKNESS_DP))
                .setStrokeCap(STROKE_CAP_BUTT)
                .setColor(zone.color.colorProp())
                .build(),
        )
        .build()
}

private fun MaterialScope.valueRing(
    fraction: Float,
    color: Color,
    diameterDp: Float,
    strokeDp: Float,
): Box =
    circularProgressIndicator(
        staticProgress = fraction.coerceIn(0f, 1f),
        startAngleDegrees = GAUGE_START_DEGREES,
        endAngleDegrees = GAUGE_START_DEGREES + GAUGE_SWEEP_DEGREES,
        strokeWidth = strokeDp,
        colors = ProgressIndicatorColors(
            indicatorColor = color.layoutColor(),
            trackColor = MetricPalette.track.layoutColor(),
            trackOverflowColor = MetricPalette.danger.layoutColor(),
        ),
        size = dp((diameterDp - BAND_TO_VALUE_INSET_DP).coerceAtLeast(strokeDp * 3f)),
    )

/** Standalone thin arc used when several metrics share one tile and no track ring is wanted. */
internal fun metricArc(fraction: Float, color: Color, thicknessDp: Float): Arc =
    Arc.Builder()
        .setAnchorAngle(degrees(GAUGE_START_DEGREES))
        .setAnchorType(ARC_ANCHOR_START)
        .setArcDirection(ARC_DIRECTION_CLOCKWISE)
        .setVerticalAlign(VERTICAL_ALIGN_CENTER)
        .addContent(
            ArcLine.Builder()
                .setLength(degrees(GAUGE_SWEEP_DEGREES * fraction.coerceIn(0f, 1f)))
                .setThickness(dp(thicknessDp))
                .setStrokeCap(STROKE_CAP_ROUND)
                .setColor(color.colorProp())
                .build(),
        )
        .build()

internal fun trackArc(thicknessDp: Float): Arc =
    metricArc(fraction = 1f, color = MetricPalette.track, thicknessDp = thicknessDp)

private fun Color.colorProp(): ColorProp = ColorProp.Builder(toArgb()).build()
