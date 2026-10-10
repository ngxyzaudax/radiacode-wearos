package com.ngxyzaudax.radiacode.presentation.theme

import androidx.compose.ui.graphics.Color

enum class MetricLevel {
    Normal,
    Warn,
    Danger,
}

/**
 * Which alarm band a reading currently occupies, derived from the same limits that drive the gauge
 * zones so a banner can never disagree with the colour of the ring beside it.
 */
fun ascendingLevel(value: Float, scale: MetricScale): MetricLevel = when {
    scale.danger?.let { value >= it } == true -> MetricLevel.Danger
    scale.warn?.let { value >= it } == true -> MetricLevel.Warn
    else -> MetricLevel.Normal
}

fun descendingLevel(value: Float, scale: MetricScale): MetricLevel = when {
    scale.danger?.let { value <= it } == true -> MetricLevel.Danger
    scale.warn?.let { value <= it } == true -> MetricLevel.Warn
    else -> MetricLevel.Normal
}

fun levelColor(level: MetricLevel): Color = when (level) {
    MetricLevel.Normal -> MetricPalette.safe
    MetricLevel.Warn -> MetricPalette.warn
    MetricLevel.Danger -> MetricPalette.danger
}
