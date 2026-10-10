package com.ngxyzaudax.radiacode.presentation

enum class AppRoute(val route: String) {
    Dashboard("dashboard"),
    TrackDetail("track/{fileName}"),
    TrackExport("track/export"),
    Settings("settings"),
    SettingsUnits("settings/units"),
    SettingsAlarms("settings/alarms"),
    SettingsAlarmValue("settings/alarms/{key}"),
    SettingsSignals("settings/signals"),
    SettingsSoundEvents("settings/signals/sound"),
    SettingsVibroEvents("settings/signals/vibro"),
    SettingsDisplay("settings/display"),
    SettingsApp("settings/app"),
    SettingsDeviceActions("settings/device"),
    ;

    companion object {
        fun alarmValueRoute(key: String): String = "settings/alarms/$key"

        fun trackDetailRoute(fileName: String): String =
            "track/${android.net.Uri.encode(fileName)}"
    }
}
