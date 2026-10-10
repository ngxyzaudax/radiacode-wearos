package com.ngxyzaudax.radiacode.tile

import android.content.Context
import androidx.wear.protolayout.material3.Typography.NUMERAL_SMALL
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import com.ngxyzaudax.radiacode.complication.ComplicationReading
import com.ngxyzaudax.radiacode.format.formatCountRate
import com.ngxyzaudax.radiacode.format.formatDoseRate
import com.ngxyzaudax.radiacode.presentation.theme.MetricPalette
import com.ngxyzaudax.radiacode.presentation.theme.ascendingZones
import com.ngxyzaudax.radiacode.presentation.theme.countRateScale
import com.ngxyzaudax.radiacode.presentation.theme.doseRateScale
import com.ngxyzaudax.radiacode.presentation.theme.metricFraction
import com.ngxyzaudax.radiacode.presentation.theme.zoneColorAt

private const val OFFLINE_CAPTION: String = "NO LINK"
private const val COUNT_RING_INSET_DP: Float = 30f

internal fun buildRatesTile(
    requestParams: RequestBuilders.TileRequest,
    context: Context,
): TileBuilders.Tile {
    val reading = ComplicationReading.load(context)
    val doseScale = doseRateScale(reading.alarmLimits)
    val countScale = countRateScale(reading.alarmLimits)
    val doseZones = ascendingZones(doseScale)
    val countZones = ascendingZones(countScale)
    val doseFraction = metricFraction(if (reading.connected) reading.doseRate else 0f, doseScale)
    val countFraction = metricFraction(if (reading.connected) reading.countRate else 0f, countScale)
    return buildTile(context, requestParams) {
        val diameter = screenDiameterDp(PRIMARY_RING_RATIO)
        val gauge = zonedGauge(
            fraction = doseFraction,
            zones = doseZones,
            diameterDp = diameter,
            valueStrokeDp = VALUE_STROKE_DP,
            innerRings = listOf(
                concentricRing(
                    diameterDp = valueRingDiameterDp(diameter) - COUNT_RING_INSET_DP,
                    thicknessDp = RING_THICKNESS_DP,
                    fraction = countFraction,
                    color = zoneColorAt(countFraction, countZones),
                ),
            ),
            center = {
                centeredColumn(
                    listOf(
                        metricValueText(
                            formatDoseRate(reading.doseRate, reading.connected),
                            zoneColorAt(doseFraction, doseZones),
                            NUMERAL_SMALL,
                        ),
                        captionText(reading.doseLabel),
                        verticalGap(4f),
                        secondaryValueText(
                            countSummary(reading.countRate, reading.countLabel, reading.connected),
                            if (reading.connected) {
                                zoneColorAt(countFraction, countZones)
                            } else {
                                MetricPalette.danger
                            },
                        ),
                    ),
                )
            },
        )
        fullScreenLayout(onClick = openAppClickable(context), content = gauge)
    }
}

private fun countSummary(countRate: Float, label: String, connected: Boolean): String =
    if (connected) "${formatCountRate(countRate, true)} $label" else OFFLINE_CAPTION
