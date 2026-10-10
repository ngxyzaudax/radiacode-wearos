package com.ngxyzaudax.radiacode.presentation.screens.settings

import com.ngxyzaudax.radiacode.protocol.AlarmLimits
import com.ngxyzaudax.radiacode.protocol.CountDisplayUnit
import com.ngxyzaudax.radiacode.protocol.DoseDisplayUnit
import java.util.Locale
import kotlin.math.roundToInt

data class AlarmStepScale(
    val step: Float,
    val minValue: Float,
    val maxValue: Float,
    val decimals: Int,
) {
    val optionCount: Int =
        ((maxValue - minValue) / step).roundToInt().coerceAtLeast(0) + 1

    fun valueAt(index: Int): Float =
        (minValue + index * step).coerceIn(minValue, maxValue)

    fun indexOf(value: Float): Int {
        if (optionCount <= 1) return 0
        val raw = ((value - minValue) / step).roundToInt()
        return raw.coerceIn(0, optionCount - 1)
    }

    fun format(value: Float): String =
        String.format(Locale.US, "%.${decimals}f", valueAt(indexOf(value)))

    fun snap(value: Float): Float = valueAt(indexOf(value))
}

/**
 * Both levels of a metric share the same picker range. Warn is never capped by danger, because the
 * counterpart is derived on save instead of restricting what the user can choose.
 */
fun alarmStepScale(key: AlarmKey, limits: AlarmLimits): AlarmStepScale {
    val max = metricMax(key, limits)
    return when (key.metric) {
        AlarmMetric.CountRate -> when (limits.countUnit) {
            CountDisplayUnit.Cps -> AlarmStepScale(step = 1f, minValue = 0f, maxValue = max, decimals = 0)
            CountDisplayUnit.Cpm -> AlarmStepScale(step = 6f, minValue = 0f, maxValue = max, decimals = 0)
        }
        AlarmMetric.DoseRate, AlarmMetric.AccumDose -> when (limits.doseUnit) {
            DoseDisplayUnit.MicroSievertPerHour -> AlarmStepScale(
                step = 0.05f,
                minValue = 0f,
                maxValue = max,
                decimals = 2,
            )
            DoseDisplayUnit.MicroRoentgenPerHour -> AlarmStepScale(
                step = 1f,
                minValue = 0f,
                maxValue = max,
                decimals = 0,
            )
        }
    }
}

private fun metricMax(key: AlarmKey, limits: AlarmLimits): Float =
    when (key.metric) {
        AlarmMetric.CountRate -> when (limits.countUnit) {
            CountDisplayUnit.Cps -> 1000f
            CountDisplayUnit.Cpm -> 6000f
        }
        AlarmMetric.DoseRate -> when (limits.doseUnit) {
            DoseDisplayUnit.MicroSievertPerHour -> 10f
            DoseDisplayUnit.MicroRoentgenPerHour -> 1000f
        }
        AlarmMetric.AccumDose -> when (limits.doseUnit) {
            DoseDisplayUnit.MicroSievertPerHour -> 20f
            DoseDisplayUnit.MicroRoentgenPerHour -> 2000f
        }
    }
