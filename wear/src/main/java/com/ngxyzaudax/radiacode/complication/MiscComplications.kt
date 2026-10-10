package com.ngxyzaudax.radiacode.complication

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import com.ngxyzaudax.radiacode.format.formatCelsius
import com.ngxyzaudax.radiacode.format.formatDecibelMilliwatt
import com.ngxyzaudax.radiacode.presentation.theme.ascendingZones
import com.ngxyzaudax.radiacode.presentation.theme.descendingZones
import com.ngxyzaudax.radiacode.presentation.theme.signalScale
import com.ngxyzaudax.radiacode.presentation.theme.temperatureScale

private const val SIGNAL_FLOOR_DBM: Float = -100f

class TrackingComplicationService : BaseRangedComplicationService() {
    override suspend fun buildComplication(request: ComplicationRequest): ComplicationData {
        val reading = reading()
        val text = if (reading.tracking) "REC ${reading.trackPointCount}" else "Idle"
        return shortComplication("Track", text)
    }
}

class TemperatureComplicationService : BaseRangedComplicationService() {
    override suspend fun buildComplication(request: ComplicationRequest): ComplicationData {
        val reading = reading()
        val scale = temperatureScale()
        val text = formatCelsius(reading.temperatureC)
        return buildMetricComplication(
            request.complicationType,
            MetricComplicationSpec(
                title = "Temp",
                text = text,
                longText = "Device temperature $text",
                value = reading.temperatureC ?: 0f,
                scale = scale,
                zones = ascendingZones(scale),
            ),
        )
    }
}

class SignalComplicationService : BaseRangedComplicationService() {
    override suspend fun buildComplication(request: ComplicationRequest): ComplicationData {
        val reading = reading()
        val scale = signalScale()
        val text = formatDecibelMilliwatt(reading.rssiDbm)
        return buildMetricComplication(
            request.complicationType,
            MetricComplicationSpec(
                title = "Signal",
                text = text,
                longText = "Link strength $text",
                value = reading.rssiDbm?.toFloat() ?: SIGNAL_FLOOR_DBM,
                scale = scale,
                zones = descendingZones(scale),
            ),
        )
    }
}
