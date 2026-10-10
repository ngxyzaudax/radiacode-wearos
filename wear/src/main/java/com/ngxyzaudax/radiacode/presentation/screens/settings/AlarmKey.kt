package com.ngxyzaudax.radiacode.presentation.screens.settings

enum class AlarmMetric {
    DoseRate,
    CountRate,
    AccumDose,
}

enum class AlarmLevel {
    Warn,
    Danger,
}

enum class AlarmKey(
    val routeKey: String,
    val metric: AlarmMetric,
    val level: AlarmLevel,
) {
    DoseRateWarn("dose_rate_warn", AlarmMetric.DoseRate, AlarmLevel.Warn),
    DoseRateDanger("dose_rate_danger", AlarmMetric.DoseRate, AlarmLevel.Danger),
    CountRateWarn("count_rate_warn", AlarmMetric.CountRate, AlarmLevel.Warn),
    CountRateDanger("count_rate_danger", AlarmMetric.CountRate, AlarmLevel.Danger),
    AccumDoseWarn("accum_dose_warn", AlarmMetric.AccumDose, AlarmLevel.Warn),
    AccumDoseDanger("accum_dose_danger", AlarmMetric.AccumDose, AlarmLevel.Danger),
    ;

    companion object {
        fun fromRouteKey(key: String): AlarmKey? =
            entries.firstOrNull { it.routeKey == key }
    }
}

fun alarmKeysByMetric(): List<Pair<AlarmMetric, List<AlarmKey>>> =
    AlarmMetric.entries.map { metric ->
        metric to AlarmKey.entries.filter { it.metric == metric }
    }
