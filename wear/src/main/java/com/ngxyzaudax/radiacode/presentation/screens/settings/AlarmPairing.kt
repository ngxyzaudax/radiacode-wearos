package com.ngxyzaudax.radiacode.presentation.screens.settings

import com.ngxyzaudax.radiacode.protocol.AlarmLimits

private const val LEVEL_SPREAD_FACTOR: Float = 2f

data class AlarmPair(
    val warn: Float,
    val danger: Float,
)

fun warnValue(metric: AlarmMetric, limits: AlarmLimits): Float =
    when (metric) {
        AlarmMetric.DoseRate -> limits.l1DoseRate
        AlarmMetric.CountRate -> limits.l1CountRate
        AlarmMetric.AccumDose -> limits.l1Dose
    }

fun dangerValue(metric: AlarmMetric, limits: AlarmLimits): Float =
    when (metric) {
        AlarmMetric.DoseRate -> limits.l2DoseRate
        AlarmMetric.CountRate -> limits.l2CountRate
        AlarmMetric.AccumDose -> limits.l2Dose
    }

fun withLevelValues(metric: AlarmMetric, limits: AlarmLimits, pair: AlarmPair): AlarmLimits =
    when (metric) {
        AlarmMetric.DoseRate -> limits.copy(l1DoseRate = pair.warn, l2DoseRate = pair.danger)
        AlarmMetric.CountRate -> limits.copy(l1CountRate = pair.warn, l2CountRate = pair.danger)
        AlarmMetric.AccumDose -> limits.copy(l1Dose = pair.warn, l2Dose = pair.danger)
    }

/**
 * The edited level always wins. Its counterpart is only recomputed when keeping it would leave the
 * pair out of order, so a deliberate spread the user already chose is never overwritten. A zero
 * edit means the level is disabled and never drags its counterpart along.
 */
fun pairedAlarmValues(
    key: AlarmKey,
    edited: Float,
    limits: AlarmLimits,
    scale: AlarmStepScale,
): AlarmPair =
    when (key.level) {
        AlarmLevel.Warn -> pairFromWarn(edited, dangerValue(key.metric, limits), scale)
        AlarmLevel.Danger -> pairFromDanger(edited, warnValue(key.metric, limits), scale)
    }

private fun pairFromWarn(warn: Float, danger: Float, scale: AlarmStepScale): AlarmPair {
    if (warn <= 0f || danger > warn) return AlarmPair(warn, danger)
    val cappedWarn = warn.coerceAtMost(scale.maxValue - scale.step)
    val derived = scale.snap(cappedWarn * LEVEL_SPREAD_FACTOR)
    return AlarmPair(cappedWarn, derived.coerceIn(cappedWarn + scale.step, scale.maxValue))
}

private fun pairFromDanger(danger: Float, warn: Float, scale: AlarmStepScale): AlarmPair {
    if (danger <= 0f) return AlarmPair(warn, danger)
    if (warn > 0f && warn < danger) return AlarmPair(warn, danger)
    val cappedDanger = danger.coerceAtLeast(scale.minValue + scale.step)
    val derived = scale.snap(cappedDanger / LEVEL_SPREAD_FACTOR)
    return AlarmPair(derived.coerceIn(scale.minValue, cappedDanger - scale.step), cappedDanger)
}
