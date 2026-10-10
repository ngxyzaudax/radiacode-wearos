package com.ngxyzaudax.radiacode.protocol

enum class DoseDisplayUnit {
    MicroSievertPerHour,
    MicroRoentgenPerHour,
    ;

    fun toDeviceFlag(): Int =
        if (this == MicroSievertPerHour) 1 else 0

    companion object {
        fun fromDeviceFlag(flag: Int): DoseDisplayUnit =
            if (flag != 0) MicroSievertPerHour else MicroRoentgenPerHour
    }
}

enum class CountDisplayUnit {
    Cps,
    Cpm,
    ;

    fun toDeviceFlag(): Int =
        if (this == Cpm) 1 else 0

    companion object {
        fun fromDeviceFlag(flag: Int): CountDisplayUnit =
            if (flag != 0) Cpm else Cps
    }
}

fun doseDisplayFromRh(doseRateRh: Float, unit: DoseDisplayUnit): Float =
    when (unit) {
        DoseDisplayUnit.MicroSievertPerHour -> doseRateRh * 10_000f
        DoseDisplayUnit.MicroRoentgenPerHour -> doseRateRh * 1_000_000f
    }

fun countDisplayFromCps(countCps: Float, unit: CountDisplayUnit): Float =
    when (unit) {
        CountDisplayUnit.Cpm -> countCps * 60f
        CountDisplayUnit.Cps -> countCps
    }

fun doseUnitLabel(unit: DoseDisplayUnit): String =
    when (unit) {
        DoseDisplayUnit.MicroSievertPerHour -> "µSv/h"
        DoseDisplayUnit.MicroRoentgenPerHour -> "µR/h"
    }

fun doseAccumUnitLabel(unit: DoseDisplayUnit): String =
    when (unit) {
        DoseDisplayUnit.MicroSievertPerHour -> "µSv"
        DoseDisplayUnit.MicroRoentgenPerHour -> "µR"
    }

fun countUnitLabel(unit: CountDisplayUnit): String =
    when (unit) {
        CountDisplayUnit.Cps -> "CPS"
        CountDisplayUnit.Cpm -> "CPM"
    }
