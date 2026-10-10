package com.ngxyzaudax.radiacode.presentation.theme

import kotlin.math.log10
import kotlin.math.pow

private const val DECADE_SPAN: Int = 3
private const val DANGER_HEADROOM: Float = 10f
private const val MIN_LINEAR_SPAN: Float = 0.01f
private const val MIN_DECADE_ANCHOR: Float = 0.0001f

/**
 * Mapping from a physical metric value onto the 0..1 fraction used by every gauge, bar and arc.
 * A null [warn] or [danger] means the device has no limit configured for that metric.
 *
 * A logarithmic scale is anchored on the danger limit rather than on the next power of ten: it ends
 * exactly [DANGER_HEADROOM] times above danger and spans [DECADE_SPAN] decades below that. Anchoring
 * on the limit keeps the danger band a predictable third of the sweep for any limit value, whereas
 * rounding up to a decade boundary can push danger as low as a tenth of the sweep and flood the
 * gauge with red.
 */
data class MetricScale(
    val min: Float,
    val max: Float,
    val warn: Float?,
    val danger: Float?,
    val logarithmic: Boolean,
)

fun linearScale(min: Float, max: Float, warn: Float?, danger: Float?): MetricScale =
    MetricScale(
        min = min,
        max = max.coerceAtLeast(min + MIN_LINEAR_SPAN),
        warn = warn,
        danger = danger,
        logarithmic = false,
    )

fun decadeScale(warn: Float?, danger: Float?, fallbackTop: Float): MetricScale {
    val top = decadeAnchor(warn, danger, fallbackTop) * DANGER_HEADROOM
    return MetricScale(
        min = top / 10f.pow(DECADE_SPAN),
        max = top,
        warn = warn,
        danger = danger,
        logarithmic = true,
    )
}

private fun decadeAnchor(warn: Float?, danger: Float?, fallbackTop: Float): Float =
    (danger ?: warn?.times(10f) ?: fallbackTop).coerceAtLeast(MIN_DECADE_ANCHOR)

fun metricFraction(value: Float, scale: MetricScale): Float =
    if (scale.logarithmic) decadeFraction(value, scale) else linearFraction(value, scale)

private fun linearFraction(value: Float, scale: MetricScale): Float =
    ((value - scale.min) / (scale.max - scale.min)).coerceIn(0f, 1f)

private fun decadeFraction(value: Float, scale: MetricScale): Float {
    val clamped = value.coerceIn(scale.min, scale.max)
    return (log10(clamped / scale.min) / log10(scale.max / scale.min)).coerceIn(0f, 1f)
}

val MetricScale.warnFraction: Float
    get() = warn?.let { metricFraction(it, this) } ?: 0f

val MetricScale.dangerFraction: Float
    get() = danger?.let { metricFraction(it, this) } ?: 0f
