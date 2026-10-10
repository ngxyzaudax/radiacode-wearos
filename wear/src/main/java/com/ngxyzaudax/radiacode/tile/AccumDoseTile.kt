package com.ngxyzaudax.radiacode.tile

import android.content.Context
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import com.ngxyzaudax.radiacode.complication.ComplicationReading
import com.ngxyzaudax.radiacode.format.formatAccumulatedDose
import com.ngxyzaudax.radiacode.format.formatExposureDuration
import com.ngxyzaudax.radiacode.presentation.theme.accumulatedDoseScale
import com.ngxyzaudax.radiacode.presentation.theme.ascendingZones
import com.ngxyzaudax.radiacode.protocol.doseAccumUnitLabel

internal fun buildAccumDoseTile(
    requestParams: RequestBuilders.TileRequest,
    context: Context,
): TileBuilders.Tile {
    val reading = ComplicationReading.load(context)
    val scale = accumulatedDoseScale(reading.alarmLimits)
    val unit = doseAccumUnitLabel(reading.alarmLimits.doseUnit)
    val duration = formatExposureDuration(reading.accumulatedDurationSec)
    return buildMetricTile(
        requestParams = requestParams,
        context = context,
        spec = MetricTileSpec(
            value = formatAccumulatedDose(reading.accumulatedDose),
            unit = unit,
            reading = if (reading.connected) reading.accumulatedDose else 0f,
            scale = scale,
            zones = ascendingZones(scale),
            history = emptyList(),
            connected = reading.connected,
            footer = if (reading.connected && reading.accumulatedDurationSec > 0L) duration else null,
        ),
    )
}
