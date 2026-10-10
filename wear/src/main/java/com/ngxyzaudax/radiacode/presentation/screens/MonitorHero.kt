package com.ngxyzaudax.radiacode.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.ngxyzaudax.radiacode.data.LiveReading
import com.ngxyzaudax.radiacode.format.MetricSummary
import com.ngxyzaudax.radiacode.format.countRateSummary
import com.ngxyzaudax.radiacode.presentation.components.ZonedGauge
import com.ngxyzaudax.radiacode.presentation.theme.MetricPalette
import com.ngxyzaudax.radiacode.presentation.theme.MetricScale
import com.ngxyzaudax.radiacode.presentation.theme.MetricZone
import com.ngxyzaudax.radiacode.presentation.theme.metricFraction

private const val OFFLINE_LABEL: String = "NO LINK"

/**
 * Dose rate as the hero reading with the count rate nested underneath, matching the combined rates
 * tile so the app and the tile carousel present the same primary view.
 */
@Composable
fun MonitorHero(
    reading: LiveReading,
    dose: MetricSummary,
    doseValue: Float,
    scale: MetricScale,
    zones: List<MetricZone>,
    modifier: Modifier = Modifier,
): Unit {
    val count = countRateSummary(reading)
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        ZonedGauge(fraction = metricFraction(doseValue, scale), zones = zones) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = dose.value,
                    style = MaterialTheme.typography.displaySmall,
                    color = dose.color,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = if (reading.connected) dose.label else OFFLINE_LABEL,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (reading.connected) MetricPalette.muted else MetricPalette.danger,
                )
                Text(
                    text = "${count.value} ${count.label}",
                    style = MaterialTheme.typography.labelLarge,
                    color = count.color,
                )
            }
        }
    }
}
