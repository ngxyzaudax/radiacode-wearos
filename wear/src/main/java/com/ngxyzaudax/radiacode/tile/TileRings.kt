package com.ngxyzaudax.radiacode.tile

import androidx.compose.ui.graphics.Color
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.LayoutElementBuilders.Box
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.material3.MaterialScope
import kotlin.math.min

internal const val PRIMARY_RING_RATIO: Float = 0.92f
internal const val RING_THICKNESS_DP: Float = 7f
internal const val RING_GAP_DP: Float = 26f
internal const val VALUE_STROKE_DP: Float = 11f
internal const val BAR_ROW_HEIGHT_DP: Float = 15f
internal const val BAR_SAMPLE_COUNT: Int = 12

internal fun MaterialScope.screenDiameterDp(ratio: Float): Float =
    min(deviceConfiguration.screenWidthDp, deviceConfiguration.screenHeightDp) * ratio

internal fun concentricRing(
    diameterDp: Float,
    thicknessDp: Float,
    fraction: Float,
    color: Color,
): Box =
    Box.Builder()
        .setWidth(dp(diameterDp))
        .setHeight(dp(diameterDp))
        .addContent(trackArc(thicknessDp))
        .addContent(metricArc(fraction, color, thicknessDp))
        .build()

internal fun ringStack(
    diameterDp: Float,
    rings: List<Box>,
    center: LayoutElement,
): Box {
    val builder = Box.Builder().setWidth(dp(diameterDp)).setHeight(dp(diameterDp))
    rings.forEach(builder::addContent)
    builder.addContent(center)
    return builder.build()
}
