package com.ngxyzaudax.radiacode.track

import com.ngxyzaudax.radiacode.protocol.DoseDisplayUnit

fun trackDoseRateMicroRoentgen(displayDose: Float, unit: DoseDisplayUnit): Float =
    when (unit) {
        DoseDisplayUnit.MicroRoentgenPerHour -> displayDose
        DoseDisplayUnit.MicroSievertPerHour -> displayDose * 100f
    }
