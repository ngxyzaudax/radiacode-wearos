package com.ngxyzaudax.radiacode.tile

import android.content.Context
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import com.ngxyzaudax.radiacode.complication.ComplicationReading
import com.ngxyzaudax.radiacode.format.formatCountRate
import com.ngxyzaudax.radiacode.format.formatDoseRate
import com.ngxyzaudax.radiacode.presentation.theme.ascendingZones
import com.ngxyzaudax.radiacode.presentation.theme.countRateScale
import com.ngxyzaudax.radiacode.presentation.theme.doseRateScale

internal fun buildDoseRateTile(
    requestParams: RequestBuilders.TileRequest,
    context: Context,
): TileBuilders.Tile {
    val reading = ComplicationReading.load(context)
    val scale = doseRateScale(reading.alarmLimits)
    return buildMetricTile(
        requestParams = requestParams,
        context = context,
        spec = MetricTileSpec(
            value = formatDoseRate(reading.doseRate, reading.connected),
            unit = reading.doseLabel,
            reading = if (reading.connected) reading.doseRate else 0f,
            scale = scale,
            zones = ascendingZones(scale),
            history = reading.doseRateHistory,
            connected = reading.connected,
        ),
    )
}

internal fun buildCountRateTile(
    requestParams: RequestBuilders.TileRequest,
    context: Context,
): TileBuilders.Tile {
    val reading = ComplicationReading.load(context)
    val scale = countRateScale(reading.alarmLimits)
    return buildMetricTile(
        requestParams = requestParams,
        context = context,
        spec = MetricTileSpec(
            value = formatCountRate(reading.countRate, reading.connected),
            unit = reading.countLabel,
            reading = if (reading.connected) reading.countRate else 0f,
            scale = scale,
            zones = ascendingZones(scale),
            history = reading.countRateHistory,
            connected = reading.connected,
        ),
    )
}
