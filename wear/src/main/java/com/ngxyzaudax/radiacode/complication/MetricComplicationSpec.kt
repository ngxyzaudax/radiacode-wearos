package com.ngxyzaudax.radiacode.complication

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import com.ngxyzaudax.radiacode.presentation.theme.MetricScale
import com.ngxyzaudax.radiacode.presentation.theme.MetricZone
import com.ngxyzaudax.radiacode.presentation.theme.metricFraction

internal data class MetricComplicationSpec(
    val title: String,
    val text: String,
    val longText: String,
    val value: Float,
    val scale: MetricScale,
    val zones: List<MetricZone>,
    val history: List<Float> = emptyList(),
    val unit: String = "",
)

internal fun buildMetricComplication(
    type: ComplicationType,
    spec: MetricComplicationSpec,
): ComplicationData {
    val fraction = metricFraction(spec.value, spec.scale)
    return when (type) {
        ComplicationType.RANGED_VALUE ->
            rangedComplication(spec.title, fraction, spec.zones, spec.text)
        ComplicationType.GOAL_PROGRESS ->
            goalComplication(spec.title, fraction, spec.zones, spec.text)
        ComplicationType.LONG_TEXT -> longComplication(spec.title, spec.longText)
        ComplicationType.SMALL_IMAGE ->
            gaugeImageComplication(spec.title, fraction, spec.zones, spec.text, spec.unit)
        ComplicationType.WEIGHTED_ELEMENTS ->
            weightedZoneComplication(spec.title, spec.historyFractions(), spec.zones)
        else -> shortComplication(spec.title, spec.text)
    }
}

internal fun buildGraphComplication(
    type: ComplicationType,
    spec: MetricComplicationSpec,
): ComplicationData =
    when (type) {
        ComplicationType.SMALL_IMAGE ->
            sparklineComplication(
                spec.title,
                spec.history,
                spec.scale,
                spec.zones,
                spec.text,
                spec.unit,
            )
        ComplicationType.WEIGHTED_ELEMENTS ->
            weightedZoneComplication(spec.title, spec.historyFractions(), spec.zones)
        ComplicationType.LONG_TEXT -> longComplication(spec.title, spec.longText)
        else -> shortComplication(spec.title, spec.text)
    }

private fun MetricComplicationSpec.historyFractions(): List<Float> =
    history.map { sample -> metricFraction(sample, scale) }
