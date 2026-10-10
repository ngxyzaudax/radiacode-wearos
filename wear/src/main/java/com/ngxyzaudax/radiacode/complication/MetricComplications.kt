package com.ngxyzaudax.radiacode.complication

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import com.ngxyzaudax.radiacode.format.formatAccumulatedDose
import com.ngxyzaudax.radiacode.format.formatCountRate
import com.ngxyzaudax.radiacode.format.formatDoseRate
import com.ngxyzaudax.radiacode.format.formatPercent
import com.ngxyzaudax.radiacode.presentation.theme.accumulatedDoseScale
import com.ngxyzaudax.radiacode.presentation.theme.ascendingZones
import com.ngxyzaudax.radiacode.presentation.theme.batteryScale
import com.ngxyzaudax.radiacode.presentation.theme.countRateScale
import com.ngxyzaudax.radiacode.presentation.theme.descendingZones
import com.ngxyzaudax.radiacode.presentation.theme.doseRateScale

class DoseRateComplicationService : BaseRangedComplicationService() {
    override suspend fun buildComplication(request: ComplicationRequest): ComplicationData {
        val reading = reading()
        val scale = doseRateScale(reading.alarmLimits)
        val text = formatDoseRate(reading.doseRate, reading.connected)
        return buildMetricComplication(
            request.complicationType,
            MetricComplicationSpec(
                title = "Dose",
                text = text,
                longText = "$text ${reading.doseLabel}",
                unit = reading.doseLabel,
                value = if (reading.connected) reading.doseRate else 0f,
                scale = scale,
                zones = ascendingZones(scale),
                history = reading.doseRateHistory,
            ),
        )
    }
}

class CountRateComplicationService : BaseRangedComplicationService() {
    override suspend fun buildComplication(request: ComplicationRequest): ComplicationData {
        val reading = reading()
        val scale = countRateScale(reading.alarmLimits)
        val text = formatCountRate(reading.countRate, reading.connected)
        return buildMetricComplication(
            request.complicationType,
            MetricComplicationSpec(
                title = "Count",
                text = text,
                longText = "$text ${reading.countLabel}",
                unit = reading.countLabel,
                value = if (reading.connected) reading.countRate else 0f,
                scale = scale,
                zones = ascendingZones(scale),
                history = reading.countRateHistory,
            ),
        )
    }
}

class AccumulatedDoseComplicationService : BaseRangedComplicationService() {
    override suspend fun buildComplication(request: ComplicationRequest): ComplicationData {
        val reading = reading()
        val scale = accumulatedDoseScale(reading.alarmLimits)
        val text = formatAccumulatedDose(reading.accumulatedDose)
        return buildMetricComplication(
            request.complicationType,
            MetricComplicationSpec(
                title = "Accum",
                text = text,
                longText = "$text ${reading.doseLabel.removeSuffix("/h")}",
                unit = reading.doseLabel.removeSuffix("/h"),
                value = reading.accumulatedDose,
                scale = scale,
                zones = ascendingZones(scale),
            ),
        )
    }
}

class BatteryComplicationService : BaseRangedComplicationService() {
    override suspend fun buildComplication(request: ComplicationRequest): ComplicationData {
        val reading = reading()
        val scale = batteryScale()
        val text = formatPercent(reading.batteryPercent)
        return buildMetricComplication(
            request.complicationType,
            MetricComplicationSpec(
                title = "Battery",
                text = text,
                longText = "Device battery $text",
                unit = "BAT",
                value = reading.batteryPercent ?: 0f,
                scale = scale,
                zones = descendingZones(scale),
            ),
        )
    }
}
