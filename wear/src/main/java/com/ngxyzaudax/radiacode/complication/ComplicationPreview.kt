package com.ngxyzaudax.radiacode.complication

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import com.ngxyzaudax.radiacode.presentation.theme.MetricScale
import com.ngxyzaudax.radiacode.presentation.theme.MetricZone
import com.ngxyzaudax.radiacode.presentation.theme.ascendingZones
import com.ngxyzaudax.radiacode.presentation.theme.linearScale

private const val PREVIEW_TITLE: String = "RadiaCode"
private const val PREVIEW_FRACTION: Float = 0.42f
private val PREVIEW_SCALE: MetricScale = linearScale(min = 0f, max = 1f, warn = 0.5f, danger = 0.8f)
private val PREVIEW_HISTORY: List<Float> =
    listOf(0.1f, 0.15f, 0.12f, 0.3f, 0.28f, 0.45f, 0.4f, 0.55f, 0.62f, 0.5f, 0.42f)

internal fun sparklineComplication(
    title: String,
    history: List<Float>,
    scale: MetricScale,
    zones: List<MetricZone>,
    value: String = "",
    caption: String = "",
): ComplicationData =
    imageComplication(title, renderSparklineBitmap(history, scale, zones, value, caption))

internal fun previewComplication(type: ComplicationType): ComplicationData? {
    val zones = ascendingZones(PREVIEW_SCALE)
    return when (type) {
        ComplicationType.RANGED_VALUE ->
            rangedComplication(PREVIEW_TITLE, PREVIEW_FRACTION, zones, "0.42")
        ComplicationType.GOAL_PROGRESS ->
            goalComplication(PREVIEW_TITLE, PREVIEW_FRACTION, zones, "0.42")
        ComplicationType.SHORT_TEXT -> shortComplication(PREVIEW_TITLE, "0.42")
        ComplicationType.LONG_TEXT -> longComplication(PREVIEW_TITLE, "0.42 µSv/h")
        ComplicationType.SMALL_IMAGE ->
            gaugeImageComplication(PREVIEW_TITLE, PREVIEW_FRACTION, zones)
        ComplicationType.WEIGHTED_ELEMENTS ->
            weightedZoneComplication(PREVIEW_TITLE, PREVIEW_HISTORY, zones)
        else -> null
    }
}
