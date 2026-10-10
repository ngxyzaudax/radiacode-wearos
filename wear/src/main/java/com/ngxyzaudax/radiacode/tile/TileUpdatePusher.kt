package com.ngxyzaudax.radiacode.tile

import android.content.Context
import androidx.wear.tiles.TileService
import com.ngxyzaudax.radiacode.AppForeground

internal object TileUpdatePusher {
    private var lastPushMs: Long = 0L
    private const val THROTTLE_MS: Long = 15_000

    private val tileServices: List<Class<out TileService>> = listOf(
        RatesTileService::class.java,
        DoseRateTileService::class.java,
        CountRateTileService::class.java,
        StatusTileService::class.java,
        TrackTileService::class.java,
        AccumDoseTileService::class.java,
    )

    fun pushTrack(context: Context): Unit {
        if (AppForeground.isForeground) return
        runCatching { TileService.getUpdater(context).requestUpdate(TrackTileService::class.java) }
    }

    fun pushAll(context: Context): Unit {
        if (AppForeground.isForeground) return
        val now = System.currentTimeMillis()
        if (now - lastPushMs < THROTTLE_MS) return
        lastPushMs = now
        tileServices.forEach { service ->
            runCatching { TileService.getUpdater(context).requestUpdate(service) }
        }
    }
}
