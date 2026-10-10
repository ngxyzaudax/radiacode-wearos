package com.ngxyzaudax.radiacode.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ngxyzaudax.radiacode.protocol.AlarmLimits
import com.ngxyzaudax.radiacode.protocol.CountDisplayUnit
import com.ngxyzaudax.radiacode.protocol.DoseDisplayUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.snapshotStore by preferencesDataStore("reading_snapshot")

data class ReadingSnapshot(
    val doseRate: Float = 0f,
    val countRate: Float = 0f,
    val accumulatedDose: Float = 0f,
    val accumulatedDurationSec: Long = 0L,
    val doseLabel: String = "µSv/h",
    val countLabel: String = "CPS",
    val connected: Boolean = false,
    val deviceLabel: String = "",
    val tracking: Boolean = false,
    val trackPointCount: Int = 0,
    val batteryPercent: Float? = null,
    val temperatureC: Float? = null,
    val rssiDbm: Int? = null,
    val alarmLimits: AlarmLimits = AlarmLimits(),
    val severityHistory: List<Float> = emptyList(),
    val doseRateHistory: List<Float> = emptyList(),
    val countRateHistory: List<Float> = emptyList(),
)

class ReadingSnapshotStore(private val context: Context) {
    val snapshot: Flow<ReadingSnapshot> = context.snapshotStore.data.map { prefs ->
        ReadingSnapshot(
            doseRate = prefs[Keys.doseRate] ?: 0f,
            countRate = prefs[Keys.countRate] ?: 0f,
            accumulatedDose = prefs[Keys.accumulatedDose] ?: 0f,
            accumulatedDurationSec = prefs[Keys.accumulatedDurationSec] ?: 0L,
            doseLabel = prefs[Keys.doseLabel] ?: "µSv/h",
            countLabel = prefs[Keys.countLabel] ?: "CPS",
            connected = prefs[Keys.connected] == 1,
            deviceLabel = prefs[Keys.deviceLabel] ?: "",
            tracking = prefs[Keys.tracking] == 1,
            trackPointCount = prefs[Keys.trackPointCount] ?: 0,
            batteryPercent = prefs[Keys.batteryPercent],
            temperatureC = prefs[Keys.temperatureC],
            rssiDbm = prefs[Keys.rssiDbm],
            alarmLimits = readAlarmLimits(prefs),
            severityHistory = readHistory(prefs),
            doseRateHistory = readFloatList(prefs[Keys.doseRateHistory]),
            countRateHistory = readFloatList(prefs[Keys.countRateHistory]),
        )
    }

    suspend fun save(reading: LiveReading): Unit {
        context.snapshotStore.edit { prefs ->
            prefs[Keys.doseRate] = reading.doseRate
            prefs[Keys.countRate] = reading.countRate
            prefs[Keys.accumulatedDose] = reading.accumulatedDose
            prefs[Keys.accumulatedDurationSec] = reading.accumulatedDurationSec
            prefs[Keys.doseLabel] = reading.doseLabel
            prefs[Keys.countLabel] = reading.countLabel
            prefs[Keys.connected] = if (reading.connected) 1 else 0
            prefs[Keys.deviceLabel] = reading.deviceLabel
            prefs[Keys.tracking] = if (reading.tracking) 1 else 0
            prefs[Keys.trackPointCount] = reading.trackPointCount
            reading.batteryPercent?.let { prefs[Keys.batteryPercent] = it }
            reading.temperatureC?.let { prefs[Keys.temperatureC] = it }
            reading.rssiDbm?.let { prefs[Keys.rssiDbm] = it }
            writeAlarmLimits(prefs, reading.alarmLimits)
            writeHistory(prefs, reading.severityHistory)
            writeFloatList(prefs, Keys.doseRateHistory, reading.doseRateHistory)
            writeFloatList(prefs, Keys.countRateHistory, reading.countRateHistory)
        }
    }

    private object Keys {
        val doseRate = floatPreferencesKey("dose_rate")
        val countRate = floatPreferencesKey("count_rate")
        val accumulatedDose = floatPreferencesKey("accumulated_dose")
        val accumulatedDurationSec = androidx.datastore.preferences.core.longPreferencesKey("accumulated_duration_sec")
        val doseLabel = stringPreferencesKey("dose_label")
        val countLabel = stringPreferencesKey("count_label")
        val connected = intPreferencesKey("connected")
        val deviceLabel = stringPreferencesKey("device_label")
        val tracking = intPreferencesKey("tracking")
        val trackPointCount = intPreferencesKey("track_point_count")
        val batteryPercent = floatPreferencesKey("battery_percent")
        val temperatureC = floatPreferencesKey("temperature_c")
        val rssiDbm = intPreferencesKey("rssi_dbm")
        val l1DoseRate = floatPreferencesKey("l1_dose_rate")
        val l2DoseRate = floatPreferencesKey("l2_dose_rate")
        val l1CountRate = floatPreferencesKey("l1_count_rate")
        val l2CountRate = floatPreferencesKey("l2_count_rate")
        val l1Dose = floatPreferencesKey("l1_dose")
        val l2Dose = floatPreferencesKey("l2_dose")
        val doseUnit = stringPreferencesKey("dose_unit")
        val countUnit = stringPreferencesKey("count_unit")
        val severityHistory = stringPreferencesKey("severity_history")
        val doseRateHistory = stringPreferencesKey("dose_rate_history")
        val countRateHistory = stringPreferencesKey("count_rate_history")
    }

    private fun readAlarmLimits(prefs: androidx.datastore.preferences.core.Preferences): AlarmLimits =
        AlarmLimits(
            l1DoseRate = prefs[Keys.l1DoseRate] ?: 0f,
            l2DoseRate = prefs[Keys.l2DoseRate] ?: 0f,
            l1CountRate = prefs[Keys.l1CountRate] ?: 0f,
            l2CountRate = prefs[Keys.l2CountRate] ?: 0f,
            l1Dose = prefs[Keys.l1Dose] ?: 0f,
            l2Dose = prefs[Keys.l2Dose] ?: 0f,
            doseUnit = prefs[Keys.doseUnit]?.let { DoseDisplayUnit.valueOf(it) }
                ?: DoseDisplayUnit.MicroSievertPerHour,
            countUnit = prefs[Keys.countUnit]?.let { CountDisplayUnit.valueOf(it) }
                ?: CountDisplayUnit.Cps,
        )

    private fun writeAlarmLimits(
        prefs: androidx.datastore.preferences.core.MutablePreferences,
        limits: AlarmLimits,
    ): Unit {
        prefs[Keys.l1DoseRate] = limits.l1DoseRate
        prefs[Keys.l2DoseRate] = limits.l2DoseRate
        prefs[Keys.l1CountRate] = limits.l1CountRate
        prefs[Keys.l2CountRate] = limits.l2CountRate
        prefs[Keys.l1Dose] = limits.l1Dose
        prefs[Keys.l2Dose] = limits.l2Dose
        prefs[Keys.doseUnit] = limits.doseUnit.name
        prefs[Keys.countUnit] = limits.countUnit.name
    }

    private fun readHistory(prefs: androidx.datastore.preferences.core.Preferences): List<Float> =
        readFloatList(prefs[Keys.severityHistory])

    private fun writeHistory(
        prefs: androidx.datastore.preferences.core.MutablePreferences,
        history: List<Float>,
    ): Unit = writeFloatList(prefs, Keys.severityHistory, history)

    private fun readFloatList(raw: String?): List<Float> =
        raw?.split(",")?.mapNotNull { it.toFloatOrNull() } ?: emptyList()

    private fun writeFloatList(
        prefs: androidx.datastore.preferences.core.MutablePreferences,
        key: androidx.datastore.preferences.core.Preferences.Key<String>,
        values: List<Float>,
    ): Unit {
        prefs[key] = values.joinToString(",")
    }
}
