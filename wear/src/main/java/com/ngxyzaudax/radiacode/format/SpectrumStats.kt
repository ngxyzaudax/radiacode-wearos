package com.ngxyzaudax.radiacode.format

import com.ngxyzaudax.radiacode.protocol.Spectrum
import java.util.Locale

fun formatSpectrumTotal(counts: IntArray?): String {
    val total = counts?.sum() ?: return NO_VALUE
    return when {
        total >= 1_000_000 -> String.format(Locale.US, "%.1fM", total / 1_000_000f)
        total >= 1_000 -> String.format(Locale.US, "%.1fk", total / 1_000f)
        else -> total.toString()
    }
}

fun formatSpectrumPeakEnergy(spectrum: Spectrum?): String {
    val counts = spectrum?.counts ?: return NO_VALUE
    if (counts.isEmpty()) return NO_VALUE
    val peakChannel = counts.indices.maxByOrNull { counts[it] } ?: return NO_VALUE
    val keV = spectrum.channelEnergy(peakChannel) / 1000f
    return String.format(Locale.US, "%.0f keV", keV)
}

fun formatExposureDuration(seconds: Long): String {
    if (seconds <= 0L) return NO_VALUE
    val days = seconds / 86_400
    val hours = (seconds % 86_400) / 3_600
    val minutes = (seconds % 3_600) / 60
    val secs = seconds % 60
    return when {
        days > 0 -> "${days}d ${hours}h"
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> if (secs > 0) "${minutes}m ${secs}s" else "${minutes}m"
        else -> "${secs}s"
    }
}

fun formatEnergyKeV(energyEv: Float): String =
    String.format(Locale.US, "%.0f", energyEv / 1000f)

fun formatAverageDoseRate(accumulatedDose: Float, durationSec: Long, doseLabel: String): String {
    if (durationSec <= 0L || accumulatedDose <= 0f) return NO_VALUE
    val hours = durationSec / 3600f
    val average = accumulatedDose / hours
    return "${formatMagnitude(average)} $doseLabel"
}
