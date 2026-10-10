package com.ngxyzaudax.radiacode.tile

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import com.ngxyzaudax.radiacode.presentation.theme.MetricPalette
import com.ngxyzaudax.radiacode.presentation.theme.MetricScale
import com.ngxyzaudax.radiacode.presentation.theme.MetricZone
import com.ngxyzaudax.radiacode.presentation.theme.metricFraction
import com.ngxyzaudax.radiacode.presentation.theme.zoneColorAt

private const val OFFLINE_CAPTION: String = "NO LINK"

internal data class MetricTileSpec(
    val value: String,
    val unit: String,
    val reading: Float,
    val scale: MetricScale,
    val zones: List<MetricZone>,
    val history: List<Float>,
    val connected: Boolean,
    val footer: String? = null,
)

internal fun buildMetricTile(
    requestParams: RequestBuilders.TileRequest,
    context: Context,
    spec: MetricTileSpec,
): TileBuilders.Tile {
    val fraction = metricFraction(spec.reading, spec.scale)
    val color = zoneColorAt(fraction, spec.zones)
    return buildTile(context, requestParams) {
        val gauge = zonedGauge(
            fraction = fraction,
            zones = spec.zones,
            diameterDp = screenDiameterDp(PRIMARY_RING_RATIO),
            valueStrokeDp = VALUE_STROKE_DP,
            center = { metricCenter(spec, color) },
        )
        fullScreenLayout(onClick = openAppClickable(context), content = gauge)
    }
}

private fun MaterialScope.metricCenter(spec: MetricTileSpec, color: Color): LayoutElement = centeredColumn(
    listOfNotNull(
        metricValueText(spec.value, color, numeralTypographyFor(spec.value)),
        captionText(
            if (spec.connected) spec.unit else OFFLINE_CAPTION,
            if (spec.connected) MetricPalette.muted else MetricPalette.danger,
        ),
        verticalGap(6f),
        metricFooter(spec),
    ),
)

private fun MaterialScope.metricFooter(spec: MetricTileSpec): LayoutElement? =
    when {
        spec.footer != null -> captionText(spec.footer, MetricPalette.muted)
        spec.history.isNotEmpty() -> historyBars(spec.history, spec.scale, spec.zones, BAR_ROW_HEIGHT_DP, BAR_SAMPLE_COUNT)
        else -> null
    }
