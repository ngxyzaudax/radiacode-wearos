package com.ngxyzaudax.radiacode.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.ngxyzaudax.radiacode.data.LiveReading
import com.ngxyzaudax.radiacode.device.DeviceInfo
import com.ngxyzaudax.radiacode.format.MetricSummary
import com.ngxyzaudax.radiacode.format.batterySummary
import com.ngxyzaudax.radiacode.format.signalSummary
import com.ngxyzaudax.radiacode.format.temperatureSummary
import com.ngxyzaudax.radiacode.presentation.components.MetricStat
import com.ngxyzaudax.radiacode.presentation.components.MetricStatRow
import com.ngxyzaudax.radiacode.presentation.theme.MetricPalette

private val SECTION_PADDING = 14.dp
private val ROW_GAP = 6.dp

@Composable
fun DeviceStats(
    reading: LiveReading,
    deviceInfo: DeviceInfo?,
    modifier: Modifier = Modifier,
): Unit {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MetricStatRow(
            stats = listOf(
                batterySummary(reading).toStat(),
                temperatureSummary(reading).toStat(),
                signalSummary(reading).toStat(),
            ),
            modifier = Modifier.padding(horizontal = SECTION_PADDING),
        )
        Text(
            text = deviceIdentity(reading, deviceInfo),
            style = MaterialTheme.typography.labelSmall,
            color = MetricPalette.muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = ROW_GAP, start = SECTION_PADDING, end = SECTION_PADDING),
        )
    }
}

private fun deviceIdentity(reading: LiveReading, deviceInfo: DeviceInfo?): String = buildString {
    append(deviceModelLabel(reading, deviceInfo))
    deviceInfo?.let { append("\nFirmware ${it.firmwareMajor}.${it.firmwareMinor}") }
}

private fun deviceModelLabel(reading: LiveReading, deviceInfo: DeviceInfo?): String {
    val model = deviceInfo?.model?.takeIf { it.isNotBlank() }
    if (model != null) return model
    return reading.deviceLabel.ifBlank { "RadiaCode" }
}

private fun MetricSummary.toStat(): MetricStat = MetricStat(value, label, color)
