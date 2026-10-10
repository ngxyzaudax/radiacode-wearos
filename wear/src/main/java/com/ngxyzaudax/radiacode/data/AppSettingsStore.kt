package com.ngxyzaudax.radiacode.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ngxyzaudax.radiacode.protocol.CountDisplayUnit
import com.ngxyzaudax.radiacode.protocol.DoseDisplayUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

data class AppSettings(
    val doseUnit: DoseDisplayUnit = DoseDisplayUnit.MicroSievertPerHour,
    val countUnit: CountDisplayUnit = CountDisplayUnit.Cps,
    val pollIntervalMs: Long = 1_000,
    val monitorWindowMs: Long = 60_000,
    val autoReconnect: Boolean = true,
    val useNanoSvH: Boolean = false,
)

class AppSettingsStore(private val context: Context) {
    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs -> readSettings(prefs) }

    suspend fun update(transform: (AppSettings) -> AppSettings): Unit {
        context.dataStore.edit { prefs ->
            val next = transform(readSettings(prefs))
            prefs[Keys.doseUnit] = next.doseUnit.name
            prefs[Keys.countUnit] = next.countUnit.name
            prefs[Keys.pollIntervalMs] = next.pollIntervalMs.toInt()
            prefs[Keys.monitorWindowMs] = next.monitorWindowMs.toInt()
            prefs[Keys.autoReconnect] = next.autoReconnect
            prefs[Keys.useNanoSvH] = next.useNanoSvH
        }
    }

    private fun readSettings(prefs: androidx.datastore.preferences.core.Preferences): AppSettings =
        AppSettings(
            doseUnit = prefs[Keys.doseUnit]?.let { DoseDisplayUnit.valueOf(it) }
                ?: DoseDisplayUnit.MicroSievertPerHour,
            countUnit = prefs[Keys.countUnit]?.let { CountDisplayUnit.valueOf(it) }
                ?: CountDisplayUnit.Cps,
            pollIntervalMs = prefs[Keys.pollIntervalMs]?.toLong() ?: 1_000,
            monitorWindowMs = prefs[Keys.monitorWindowMs]?.toLong() ?: 60_000,
            autoReconnect = prefs[Keys.autoReconnect] ?: true,
            useNanoSvH = prefs[Keys.useNanoSvH] ?: false,
        )

    private object Keys {
        val doseUnit = stringPreferencesKey("dose_unit")
        val countUnit = stringPreferencesKey("count_unit")
        val pollIntervalMs = intPreferencesKey("poll_interval_ms")
        val monitorWindowMs = intPreferencesKey("monitor_window_ms")
        val autoReconnect = booleanPreferencesKey("auto_reconnect")
        val useNanoSvH = booleanPreferencesKey("use_nano_sv_h")
    }
}
