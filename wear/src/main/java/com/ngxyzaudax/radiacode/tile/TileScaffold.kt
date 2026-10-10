package com.ngxyzaudax.radiacode.tile

import android.content.Context
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders.Box
import androidx.wear.protolayout.LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.LayoutElementBuilders.VERTICAL_ALIGN_BOTTOM
import androidx.wear.protolayout.LayoutElementBuilders.VERTICAL_ALIGN_CENTER
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.ModifiersBuilders.Modifiers
import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders

internal const val TILE_RESOURCES_VERSION: String = "5"
internal const val TILE_FRESHNESS_MS: Long = 60_000

internal fun buildTile(
    context: Context,
    requestParams: RequestBuilders.TileRequest,
    layout: MaterialScope.() -> LayoutElement,
): TileBuilders.Tile =
    TileBuilders.Tile.Builder()
        .setResourcesVersion(TILE_RESOURCES_VERSION)
        .setFreshnessIntervalMillis(TILE_FRESHNESS_MS)
        .setTileTimeline(TimelineBuilders.Timeline.fromLayoutElement(tileLayout(context, requestParams, layout)))
        .build()

internal fun MaterialScope.fullScreenLayout(
    onClick: Clickable?,
    content: LayoutElement,
    bottom: LayoutElement? = null,
): LayoutElement {
    val root = Box.Builder()
        .setWidth(dp(deviceConfiguration.screenWidthDp.toFloat()))
        .setHeight(dp(deviceConfiguration.screenHeightDp.toFloat()))
        .setHorizontalAlignment(HORIZONTAL_ALIGN_CENTER)
        .setVerticalAlignment(VERTICAL_ALIGN_CENTER)
    onClick?.let { root.setModifiers(Modifiers.Builder().setClickable(it).build()) }
    root.addContent(content)
    bottom?.let { root.addContent(bottomAligned(it)) }
    return root.build()
}

private fun bottomAligned(element: LayoutElement): Box =
    Box.Builder()
        .setWidth(expand())
        .setHeight(expand())
        .setHorizontalAlignment(HORIZONTAL_ALIGN_CENTER)
        .setVerticalAlignment(VERTICAL_ALIGN_BOTTOM)
        .addContent(element)
        .build()

private fun tileLayout(
    context: Context,
    requestParams: RequestBuilders.TileRequest,
    layout: MaterialScope.() -> LayoutElement,
): LayoutElement =
    materialScope(
        context = context,
        deviceConfiguration = requestParams.deviceConfiguration,
        allowDynamicTheme = false,
        defaultColorScheme = oledColorScheme(),
        layout = layout,
    )

internal fun emptyTileResources(): Resources =
    Resources.Builder().setVersion(TILE_RESOURCES_VERSION).build()
