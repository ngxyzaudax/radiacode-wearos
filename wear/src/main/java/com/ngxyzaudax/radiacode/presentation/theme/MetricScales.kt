package com.ngxyzaudax.radiacode.presentation.theme

import com.ngxyzaudax.radiacode.protocol.AlarmLimits

private const val DOSE_RATE_WARN_FALLBACK: Float = 0.3f
private const val DOSE_RATE_DANGER_FALLBACK: Float = 1f
private const val COUNT_RATE_WARN_FALLBACK: Float = 30f
private const val COUNT_RATE_DANGER_FALLBACK: Float = 100f
private const val ACCUMULATED_DOSE_WARN_FALLBACK: Float = 0.5f
private const val ACCUMULATED_DOSE_DANGER_FALLBACK: Float = 2f
private const val ACCUMULATED_DOSE_HEADROOM: Float = 1.25f
private const val MAX_WARN_OF_DANGER: Float = 0.9f

const val BATTERY_WARN_PERCENT: Float = 35f
const val BATTERY_DANGER_PERCENT: Float = 15f

private data class AlarmBand(val warn: Float, val danger: Float)

private fun Float.asLimit(): Float? = takeIf { it > 0f }

/**
 * Devices that report no alarm limits still need colour bands, otherwise every gauge renders as a
 * single flat colour. Unset limits therefore fall back to conventional survey thresholds, and warn
 * is always kept below danger so the bands cannot invert.
 */
private fun alarmBand(
    configuredWarn: Float,
    configuredDanger: Float,
    warnFallback: Float,
    dangerFallback: Float,
): AlarmBand {
    val danger = configuredDanger.asLimit() ?: dangerFallback
    val warn = (configuredWarn.asLimit() ?: warnFallback).coerceAtMost(danger * MAX_WARN_OF_DANGER)
    return AlarmBand(warn, danger)
}

fun doseRateScale(limits: AlarmLimits): MetricScale {
    val dangerFallback = doseFallbackInDisplayUnit(DOSE_RATE_DANGER_FALLBACK, limits.doseUnit)
    val band = alarmBand(
        limits.l1DoseRate,
        limits.l2DoseRate,
        doseFallbackInDisplayUnit(DOSE_RATE_WARN_FALLBACK, limits.doseUnit),
        dangerFallback,
    )
    return decadeScale(band.warn, band.danger, dangerFallback)
}

fun countRateScale(limits: AlarmLimits): MetricScale {
    val dangerFallback = countFallbackInDisplayUnit(COUNT_RATE_DANGER_FALLBACK, limits.countUnit)
    val band = alarmBand(
        limits.l1CountRate,
        limits.l2CountRate,
        countFallbackInDisplayUnit(COUNT_RATE_WARN_FALLBACK, limits.countUnit),
        dangerFallback,
    )
    return decadeScale(band.warn, band.danger, dangerFallback)
}

fun accumulatedDoseScale(limits: AlarmLimits): MetricScale {
    val band = alarmBand(
        limits.l1Dose,
        limits.l2Dose,
        doseFallbackInDisplayUnit(ACCUMULATED_DOSE_WARN_FALLBACK, limits.doseUnit),
        doseFallbackInDisplayUnit(ACCUMULATED_DOSE_DANGER_FALLBACK, limits.doseUnit),
    )
    return linearScale(
        min = 0f,
        max = band.danger * ACCUMULATED_DOSE_HEADROOM,
        warn = band.warn,
        danger = band.danger,
    )
}

fun batteryScale(): MetricScale =
    linearScale(min = 0f, max = 100f, warn = BATTERY_WARN_PERCENT, danger = BATTERY_DANGER_PERCENT)

fun temperatureScale(): MetricScale =
    linearScale(min = -10f, max = 60f, warn = 40f, danger = 50f)

fun signalScale(): MetricScale =
    linearScale(min = -100f, max = -40f, warn = -85f, danger = -95f)
