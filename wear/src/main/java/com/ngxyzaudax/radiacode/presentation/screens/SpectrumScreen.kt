package com.ngxyzaudax.radiacode.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.ngxyzaudax.radiacode.device.SpectrumState
import com.ngxyzaudax.radiacode.format.formatEnergyKeV
import com.ngxyzaudax.radiacode.format.formatExposureDuration
import com.ngxyzaudax.radiacode.format.formatSpectrumPeakEnergy
import com.ngxyzaudax.radiacode.format.formatSpectrumTotal
import com.ngxyzaudax.radiacode.presentation.components.MetricStat
import com.ngxyzaudax.radiacode.presentation.components.MetricStatRow
import com.ngxyzaudax.radiacode.presentation.components.ScreenActionButton
import com.ngxyzaudax.radiacode.presentation.components.SpectrumChart
import com.ngxyzaudax.radiacode.presentation.components.spectrumEnergyRange
import com.ngxyzaudax.radiacode.presentation.theme.MetricPalette

private val SECTION_PADDING = 8.dp

@Composable
fun SpectrumScreen(
    state: SpectrumState?,
    onResetSpectrum: () -> Unit,
): Unit {
    var logarithmic by remember { mutableStateOf(true) }
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val spectrum = state?.accumulated ?: state?.live
    val (minEnergy, maxEnergy) = spectrumEnergyRange(spectrum)
    ScreenScaffold(scrollState = listState) { padding ->
        TransformingLazyColumn(state = listState, contentPadding = padding) {
            item {
                SpectrumChart(
                    spectrum = spectrum,
                    logarithmic = logarithmic,
                    modifier = Modifier.transformedHeight(this, transformationSpec),
                )
            }
            item {
                EnergyRangeRow(
                    minEnergy = minEnergy,
                    maxEnergy = maxEnergy,
                    modifier = Modifier
                        .transformedHeight(this, transformationSpec)
                        .padding(horizontal = SECTION_PADDING),
                )
            }
            item {
                MetricStatRow(
                    stats = listOf(
                        MetricStat(
                            value = formatSpectrumTotal(spectrum?.counts),
                            label = "COUNTS",
                            color = MetricPalette.dose,
                        ),
                        MetricStat(
                            value = formatSpectrumPeakEnergy(spectrum),
                            label = "PEAK",
                            color = MetricPalette.dose,
                        ),
                        MetricStat(
                            value = formatExposureDuration(spectrum?.durationSec ?: 0L),
                            label = "TIME",
                            color = MetricPalette.muted,
                        ),
                    ),
                    modifier = Modifier
                        .transformedHeight(this, transformationSpec)
                        .padding(horizontal = SECTION_PADDING),
                )
            }
            item {
                ScreenActionButton(
                    this,
                    transformationSpec,
                    if (logarithmic) "Scale: logarithmic" else "Scale: linear",
                    { logarithmic = !logarithmic },
                )
            }
            item {
                ScreenActionButton(this, transformationSpec, "Reset spectrum", onResetSpectrum)
            }
        }
    }
}

@Composable
private fun EnergyRangeRow(
    minEnergy: Float,
    maxEnergy: Float,
    modifier: Modifier = Modifier,
): Unit {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "${formatEnergyKeV(minEnergy)} keV",
            style = MaterialTheme.typography.labelSmall,
            color = MetricPalette.muted,
        )
        Text(
            text = "${formatEnergyKeV(maxEnergy)} keV",
            style = MaterialTheme.typography.labelSmall,
            color = MetricPalette.muted,
            textAlign = TextAlign.End,
        )
    }
}
