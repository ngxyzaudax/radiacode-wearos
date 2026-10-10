package com.ngxyzaudax.radiacode.format

import java.util.Locale

const val NO_VALUE: String = "--"

fun formatMagnitude(value: Float): String {
    val decimals = when {
        value >= 100f -> 0
        value >= 10f -> 1
        else -> 2
    }
    return String.format(Locale.US, "%.${decimals}f", value)
}

fun formatDoseRate(value: Float, connected: Boolean): String =
    if (connected) formatMagnitude(value) else NO_VALUE

fun formatCountRate(value: Float, connected: Boolean): String =
    if (connected) formatMagnitude(value) else NO_VALUE

fun formatAccumulatedDose(value: Float): String = formatMagnitude(value)

fun formatPercent(value: Float?): String = value?.let { "${it.toInt()}%" } ?: NO_VALUE

fun formatCelsius(value: Float?): String = value?.let { "${it.toInt()}°" } ?: NO_VALUE

fun formatDecibelMilliwatt(value: Int?): String = value?.let { "$it dBm" } ?: NO_VALUE
