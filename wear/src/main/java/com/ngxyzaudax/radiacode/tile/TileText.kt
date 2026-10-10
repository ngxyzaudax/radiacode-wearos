package com.ngxyzaudax.radiacode.tile

import androidx.compose.ui.graphics.Color
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders.Box
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.LayoutElementBuilders.Row
import androidx.wear.protolayout.LayoutElementBuilders.Spacer
import androidx.wear.protolayout.LayoutElementBuilders.VERTICAL_ALIGN_CENTER
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.Typography.LABEL_MEDIUM
import androidx.wear.protolayout.material3.Typography.LABEL_SMALL
import androidx.wear.protolayout.material3.Typography.NUMERAL_MEDIUM
import androidx.wear.protolayout.material3.Typography.NUMERAL_SMALL
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.types.layoutString
import com.ngxyzaudax.radiacode.presentation.theme.MetricPalette

private const val WIDE_VALUE_LENGTH: Int = 4

/** Keeps long readings inside the gauge opening instead of letting them collide with the ring. */
internal fun numeralTypographyFor(value: String): Int =
    if (value.length >= WIDE_VALUE_LENGTH) NUMERAL_SMALL else NUMERAL_MEDIUM

internal fun MaterialScope.metricValueText(
    value: String,
    color: Color,
    typography: Int = NUMERAL_MEDIUM,
): LayoutElement = text(value.layoutString, typography = typography, color = color.layoutColor())

internal fun MaterialScope.secondaryValueText(value: String, color: Color): LayoutElement =
    text(value.layoutString, typography = LABEL_MEDIUM, color = color.layoutColor())

internal fun MaterialScope.captionText(
    caption: String,
    color: Color = MetricPalette.muted,
): LayoutElement = text(caption.layoutString, typography = LABEL_SMALL, color = color.layoutColor())

internal fun centeredColumn(children: List<LayoutElement>): Box =
    Box.Builder()
        .setWidth(expand())
        .setHeight(expand())
        .setHorizontalAlignment(HORIZONTAL_ALIGN_CENTER)
        .setVerticalAlignment(VERTICAL_ALIGN_CENTER)
        .addContent(column(children))
        .build()

internal fun column(children: List<LayoutElement>): Column {
    val builder = Column.Builder().setHorizontalAlignment(HORIZONTAL_ALIGN_CENTER)
    children.forEach(builder::addContent)
    return builder.build()
}

internal fun centeredRow(children: List<LayoutElement>): Row {
    val builder = Row.Builder().setVerticalAlignment(VERTICAL_ALIGN_CENTER)
    children.forEach(builder::addContent)
    return builder.build()
}

internal fun verticalGap(heightDp: Float): Spacer =
    Spacer.Builder().setHeight(dp(heightDp)).build()

internal fun horizontalGap(widthDp: Float): Spacer =
    Spacer.Builder().setWidth(dp(widthDp)).build()
