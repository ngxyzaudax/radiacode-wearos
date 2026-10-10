package com.ngxyzaudax.radiacode.format

import androidx.compose.ui.graphics.Color
import com.ngxyzaudax.radiacode.data.LiveReading
import com.ngxyzaudax.radiacode.presentation.theme.MetricPalette
import com.ngxyzaudax.radiacode.presentation.theme.MetricScale
import com.ngxyzaudax.radiacode.presentation.theme.accumulatedDoseScale
import com.ngxyzaudax.radiacode.presentation.theme.ascendingZones
import com.ngxyzaudax.radiacode.presentation.theme.batteryScale
import com.ngxyzaudax.radiacode.presentation.theme.countRateScale
import com.ngxyzaudax.radiacode.presentation.theme.descendingZones
import com.ngxyzaudax.radiacode.presentation.theme.doseRateScale
import com.ngxyzaudax.radiacode.presentation.theme.metricFraction
import com.ngxyzaudax.radiacode.presentation.theme.signalScale
import com.ngxyzaudax.radiacode.presentation.theme.temperatureScale
import com.ngxyzaudax.radiacode.presentation.theme.zoneColorAt

/**
 * One reading rendered as text plus the colour of the alarm band it falls in. Tiles, complications
 * and the app all read these, which is what keeps a value the same colour on every surface.
 */
data class MetricSummary(
    val value: String,
    val label: String,
    val color: Color,
)

fun doseRateSummary(reading: LiveReading): MetricSummary {
    val scale = doseRateScale(reading.alarmLimits)
    return MetricSummary(
        value = formatDoseRate(reading.doseRate, reading.connected),
        label = reading.doseLabel,
        color = ascendingColor(reading.doseRate, reading.connected, scale),
    )
}

fun countRateSummary(reading: LiveReading): MetricSummary {
    val scale = countRateScale(reading.alarmLimits)
    return MetricSummary(
        value = formatCountRate(reading.countRate, reading.connected),
        label = reading.countLabel,
        color = ascendingColor(reading.countRate, reading.connected, scale),
    )
}

fun accumulatedDoseSummary(reading: LiveReading): MetricSummary {
    val scale = accumulatedDoseScale(reading.alarmLimits)
    return MetricSummary(
        value = formatAccumulatedDose(reading.accumulatedDose),
        label = reading.doseLabel.removeSuffix("/h"),
        color = ascendingColor(reading.accumulatedDose, reading.connected, scale),
    )
}

fun batterySummary(reading: LiveReading): MetricSummary {
    val scale = batteryScale()
    return MetricSummary(
        value = formatPercent(reading.batteryPercent),
        label = "BAT",
        color = reading.batteryPercent
            ?.let { zoneColorAt(metricFraction(it, scale), descendingZones(scale)) }
            ?: MetricPalette.muted,
    )
}

fun temperatureSummary(reading: LiveReading): MetricSummary {
    val scale = temperatureScale()
    return MetricSummary(
        value = formatCelsius(reading.temperatureC),
        label = "TEMP",
        color = reading.temperatureC
            ?.let { zoneColorAt(metricFraction(it, scale), ascendingZones(scale)) }
            ?: MetricPalette.muted,
    )
}

fun signalSummary(reading: LiveReading): MetricSummary {
    val scale = signalScale()
    return MetricSummary(
        value = reading.rssiDbm?.toString() ?: NO_VALUE,
        label = "dBm",
        color = reading.rssiDbm
            ?.let { zoneColorAt(metricFraction(it.toFloat(), scale), descendingZones(scale)) }
            ?: MetricPalette.muted,
    )
}

private fun ascendingColor(value: Float, connected: Boolean, scale: MetricScale): Color =
    if (connected) {
        zoneColorAt(metricFraction(value, scale), ascendingZones(scale))
    } else {
        MetricPalette.muted
    }
