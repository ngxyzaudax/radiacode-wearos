package com.ngxyzaudax.radiacode.protocol

import kotlin.math.roundToInt

private fun doseUnitFactor(unit: DoseDisplayUnit): Float =
    when (unit) {
        DoseDisplayUnit.MicroSievertPerHour -> 100f
        DoseDisplayUnit.MicroRoentgenPerHour -> 1f
    }

fun encodeDoseAlarm(display: Float, unit: DoseDisplayUnit): Int =
    (display * doseUnitFactor(unit)).roundToInt().coerceAtLeast(0)

fun decodeDoseAlarm(raw: Int, unit: DoseDisplayUnit): Float =
    raw / doseUnitFactor(unit)

fun encodeDoseAccum(display: Float, unit: DoseDisplayUnit): Int =
    (display * doseUnitFactor(unit)).roundToInt().coerceAtLeast(0)

fun decodeDoseAccum(raw: Int, unit: DoseDisplayUnit): Float =
    raw / doseUnitFactor(unit)

fun encodeCountAlarm(display: Float, unit: CountDisplayUnit): Int {
    val multiplier = when (unit) {
        CountDisplayUnit.Cpm -> 1f / 6f
        CountDisplayUnit.Cps -> 10f
    }
    return (display * multiplier).roundToInt().coerceAtLeast(0)
}

fun decodeCountAlarm(raw: Int, unit: CountDisplayUnit): Float {
    val multiplier = when (unit) {
        CountDisplayUnit.Cpm -> 60f
        CountDisplayUnit.Cps -> 1f
    }
    return raw / 10f * multiplier
}
