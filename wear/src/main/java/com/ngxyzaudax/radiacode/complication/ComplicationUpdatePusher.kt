package com.ngxyzaudax.radiacode.complication

import android.content.ComponentName
import android.content.Context
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.ngxyzaudax.radiacode.AppForeground
import com.ngxyzaudax.radiacode.log.RadiaLog

internal object ComplicationUpdatePusher {
    private const val component: String = "ComplicationUpdate"
    private const val THROTTLE_MS: Long = 10_000
    private var lastPushMs: Long = 0L

    private val providers: List<Class<*>> = listOf(
        DoseRateComplicationService::class.java,
        CountRateComplicationService::class.java,
        AccumulatedDoseComplicationService::class.java,
        TrackingComplicationService::class.java,
        BatteryComplicationService::class.java,
        TemperatureComplicationService::class.java,
        SignalComplicationService::class.java,
        DoseHistoryComplicationService::class.java,
        DoseRateGraphComplicationService::class.java,
        CountRateGraphComplicationService::class.java,
    )

    fun pushAll(context: Context): Unit {
        if (AppForeground.isForeground) return
        val now = System.currentTimeMillis()
        if (now - lastPushMs < THROTTLE_MS) return
        lastPushMs = now
        providers.forEach { provider ->
            runCatching {
                ComplicationDataSourceUpdateRequester
                    .create(context, ComponentName(context, provider))
                    .requestUpdateAll()
            }.onFailure { error ->
                RadiaLog.warn(component, "Update failed provider=${provider.simpleName} ${error.message}")
            }
        }
    }
}
