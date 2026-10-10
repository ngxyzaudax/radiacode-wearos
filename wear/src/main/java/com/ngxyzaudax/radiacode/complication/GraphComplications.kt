package com.ngxyzaudax.radiacode.complication

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import com.ngxyzaudax.radiacode.format.NO_VALUE
import com.ngxyzaudax.radiacode.format.formatCountRate
import com.ngxyzaudax.radiacode.format.formatDoseRate
import com.ngxyzaudax.radiacode.presentation.theme.ascendingZones
import com.ngxyzaudax.radiacode.presentation.theme.countRateScale
import com.ngxyzaudax.radiacode.presentation.theme.doseRateScale

class DoseRateGraphComplicationService : BaseRangedComplicationService() {
    override suspend fun buildComplication(request: ComplicationRequest): ComplicationData {
        val reading = reading()
        val scale = doseRateScale(reading.alarmLimits)
        val text = formatDoseRate(reading.doseRate, reading.connected)
        return buildGraphComplication(
            request.complicationType,
            MetricComplicationSpec(
                title = "Dose graph",
                text = text,
                longText = "$text ${reading.doseLabel}",
                unit = reading.doseLabel,
                value = reading.doseRate,
                scale = scale,
                zones = ascendingZones(scale),
                history = reading.doseRateHistory,
            ),
        )
    }
}

class CountRateGraphComplicationService : BaseRangedComplicationService() {
    override suspend fun buildComplication(request: ComplicationRequest): ComplicationData {
        val reading = reading()
        val scale = countRateScale(reading.alarmLimits)
        val text = formatCountRate(reading.countRate, reading.connected)
        return buildGraphComplication(
            request.complicationType,
            MetricComplicationSpec(
                title = "Count graph",
                text = text,
                longText = "$text ${reading.countLabel}",
                unit = reading.countLabel,
                value = reading.countRate,
                scale = scale,
                zones = ascendingZones(scale),
                history = reading.countRateHistory,
            ),
        )
    }
}

class DoseHistoryComplicationService : BaseRangedComplicationService() {
    override suspend fun buildComplication(request: ComplicationRequest): ComplicationData {
        val reading = reading()
        val scale = doseRateScale(reading.alarmLimits)
        val samples = reading.doseRateHistory
        return buildGraphComplication(
            request.complicationType,
            MetricComplicationSpec(
                title = "History",
                text = if (samples.isEmpty()) NO_VALUE else "${samples.size}p",
                longText = "${samples.size} dose samples",
                unit = reading.doseLabel,
                value = reading.doseRate,
                scale = scale,
                zones = ascendingZones(scale),
                history = samples,
            ),
        )
    }
}
