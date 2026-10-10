package com.ngxyzaudax.radiacode.presentation.theme

import com.ngxyzaudax.radiacode.protocol.CountDisplayUnit
import com.ngxyzaudax.radiacode.protocol.DoseDisplayUnit

private const val MICRO_ROENTGEN_PER_MICRO_SIEVERT: Float = 100f
private const val SECONDS_PER_MINUTE: Float = 60f

/**
 * Fallback thresholds are authored in µSv/h and CPS, while readings and device limits both arrive in
 * the unit the device is currently displaying. Fallbacks must therefore be rescaled before they are
 * compared against a reading, otherwise a µR/h value saturates a µSv/h scale by a factor of 100.
 */
fun doseFallbackInDisplayUnit(microSievert: Float, unit: DoseDisplayUnit): Float =
    when (unit) {
        DoseDisplayUnit.MicroSievertPerHour -> microSievert
        DoseDisplayUnit.MicroRoentgenPerHour -> microSievert * MICRO_ROENTGEN_PER_MICRO_SIEVERT
    }

fun countFallbackInDisplayUnit(countsPerSecond: Float, unit: CountDisplayUnit): Float =
    when (unit) {
        CountDisplayUnit.Cps -> countsPerSecond
        CountDisplayUnit.Cpm -> countsPerSecond * SECONDS_PER_MINUTE
    }
