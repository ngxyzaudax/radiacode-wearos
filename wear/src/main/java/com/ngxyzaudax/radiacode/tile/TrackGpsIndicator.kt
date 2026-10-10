package com.ngxyzaudax.radiacode.tile

import androidx.compose.ui.graphics.toArgb
import androidx.wear.protolayout.ColorBuilders.ColorProp
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.LayoutElementBuilders.Box
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.ModifiersBuilders.Background
import androidx.wear.protolayout.ModifiersBuilders.Corner
import androidx.wear.protolayout.ModifiersBuilders.Modifiers
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.Typography.LABEL_SMALL
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.types.layoutString
import com.ngxyzaudax.radiacode.gps.GpsStatus
import com.ngxyzaudax.radiacode.gps.gpsColor
import com.ngxyzaudax.radiacode.gps.gpsLabel

private const val DOT_DP: Float = 8f

internal fun MaterialScope.gpsIndicator(status: GpsStatus): LayoutElement =
    centeredRow(
        listOf(
            gpsDot(status),
            horizontalGap(6f),
            text(
                gpsLabel(status).layoutString,
                typography = LABEL_SMALL,
                color = gpsColor(status).layoutColor(),
            ),
        ),
    )

private fun gpsDot(status: GpsStatus): Box =
    Box.Builder()
        .setWidth(dp(DOT_DP))
        .setHeight(dp(DOT_DP))
        .setModifiers(dotModifiers(status))
        .build()

private fun dotModifiers(status: GpsStatus): Modifiers =
    Modifiers.Builder()
        .setBackground(
            Background.Builder()
                .setColor(ColorProp.Builder(gpsColor(status).toArgb()).build())
                .setCorner(Corner.Builder().setRadius(dp(DOT_DP / 2f)).build())
                .build(),
        )
        .build()
