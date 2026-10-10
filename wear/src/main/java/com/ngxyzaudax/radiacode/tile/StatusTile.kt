package com.ngxyzaudax.radiacode.tile

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders.Box
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.material3.CardColors
import androidx.wear.protolayout.material3.DataCardStyle.Companion.defaultCompactDataCardStyle
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.Typography.BODY_EXTRA_SMALL
import androidx.wear.protolayout.material3.Typography.NUMERAL_EXTRA_SMALL
import androidx.wear.protolayout.material3.buttonGroup
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textDataCard
import androidx.wear.protolayout.types.argb
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import com.ngxyzaudax.radiacode.complication.ComplicationReading
import com.ngxyzaudax.radiacode.data.LiveReading
import com.ngxyzaudax.radiacode.format.MetricSummary
import com.ngxyzaudax.radiacode.format.batterySummary
import com.ngxyzaudax.radiacode.format.countRateSummary
import com.ngxyzaudax.radiacode.format.doseRateSummary
import com.ngxyzaudax.radiacode.format.temperatureSummary
import com.ngxyzaudax.radiacode.presentation.theme.MetricPalette

private const val CARD_ELEVATED_ARGB: Int = 0xFF0D0F13.toInt()
private const val GRID_GAP_DP: Float = 4f
private const val GRID_RATIO: Float = 0.82f

private data class StatusCell(val value: String, val label: String, val color: Color)

internal fun buildStatusTile(
    requestParams: RequestBuilders.TileRequest,
    context: Context,
): TileBuilders.Tile {
    val reading = ComplicationReading.load(context)
    return buildTile(context, requestParams) {
        val side = screenDiameterDp(GRID_RATIO)
        val grid = Box.Builder()
            .setWidth(dp(side))
            .setHeight(dp(side))
            .addContent(statusGrid(reading))
            .build()
        fullScreenLayout(onClick = openAppClickable(context), content = grid)
    }
}

private fun MaterialScope.statusGrid(reading: LiveReading): LayoutElement =
    Column.Builder()
        .setWidth(expand())
        .setHeight(expand())
        .addContent(
            buttonGroup(height = expand(), spacing = GRID_GAP_DP) {
                buttonGroupItem { statusCard(doseRateSummary(reading).toCell()) }
                buttonGroupItem { statusCard(countRateSummary(reading).toCell()) }
            },
        )
        .addContent(verticalGap(GRID_GAP_DP))
        .addContent(
            buttonGroup(height = expand(), spacing = GRID_GAP_DP) {
                buttonGroupItem { statusCard(batterySummary(reading).toCell()) }
                buttonGroupItem { statusCard(temperatureSummary(reading).toCell()) }
            },
        )
        .build()

private fun MaterialScope.statusCard(cell: StatusCell): LayoutElement =
    textDataCard(
        onClick = reloadClickable(TILE_CLICK_OPEN_APP),
        width = expand(),
        height = expand(),
        style = defaultCompactDataCardStyle(),
        colors = CardColors(
            backgroundColor = CARD_ELEVATED_ARGB.argb,
            titleColor = cell.color.layoutColor(),
            contentColor = MetricPalette.muted.layoutColor(),
        ),
        title = { text(cell.value.layoutString, typography = NUMERAL_EXTRA_SMALL) },
        content = { text(cell.label.layoutString, typography = BODY_EXTRA_SMALL) },
    )

private fun MetricSummary.toCell(): StatusCell = StatusCell(value, label, color)
