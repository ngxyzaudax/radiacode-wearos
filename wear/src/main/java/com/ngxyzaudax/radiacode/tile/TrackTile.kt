package com.ngxyzaudax.radiacode.tile

import android.content.Context
import androidx.wear.protolayout.material3.ButtonColors
import androidx.wear.protolayout.material3.Typography.LABEL_MEDIUM
import androidx.wear.protolayout.material3.Typography.NUMERAL_LARGE
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textEdgeButton
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import com.ngxyzaudax.radiacode.complication.ComplicationReading
import com.ngxyzaudax.radiacode.gps.gpsStatus
import com.ngxyzaudax.radiacode.gps.isLocationAvailable
import com.ngxyzaudax.radiacode.gps.trackHint
import com.ngxyzaudax.radiacode.presentation.theme.MetricPalette

internal fun buildTrackTile(
    requestParams: RequestBuilders.TileRequest,
    context: Context,
): TileBuilders.Tile {
    val reading = ComplicationReading.load(context)
    val status = gpsStatus(reading, isLocationAvailable(context), System.currentTimeMillis())
    val accent = if (reading.tracking) MetricPalette.danger else MetricPalette.safe
    return buildTile(context, requestParams) {
        val action = textEdgeButton(
            onClick = reloadClickable(TILE_CLICK_TOGGLE_TRACK),
            colors = ButtonColors(
                containerColor = accent.dim(0.18f),
                labelColor = accent.layoutColor(),
            ),
            labelContent = {
                text(
                    (if (reading.tracking) "STOP" else "START").layoutString,
                    typography = LABEL_MEDIUM,
                )
            },
        )
        val body = centeredColumn(
            listOf(
                metricValueText("${reading.trackPointCount}", accent, NUMERAL_LARGE),
                captionText("POINTS"),
                verticalGap(8f),
                gpsIndicator(status),
                captionText(trackHint(reading, status), MetricPalette.muted),
                verticalGap(8f),
                action,
            ),
        )
        fullScreenLayout(onClick = null, content = body)
    }
}
