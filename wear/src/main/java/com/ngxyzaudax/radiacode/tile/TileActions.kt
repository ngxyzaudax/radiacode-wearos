package com.ngxyzaudax.radiacode.tile

import android.content.Context
import android.content.Intent
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.tiles.RequestBuilders
import com.ngxyzaudax.radiacode.complication.ComplicationReading
import com.ngxyzaudax.radiacode.service.MonitoringService

internal const val TILE_CLICK_OPEN_APP: String = "open_app"
internal const val TILE_CLICK_TOGGLE_TRACK: String = "toggle_track"

private const val MAIN_ACTIVITY_CLASS: String = "com.ngxyzaudax.radiacode.presentation.MainActivity"

internal fun openAppClickable(context: Context): Clickable =
    Clickable.Builder()
        .setId(TILE_CLICK_OPEN_APP)
        .setOnClick(
            ActionBuilders.LaunchAction.Builder()
                .setAndroidActivity(
                    ActionBuilders.AndroidActivity.Builder()
                        .setPackageName(context.packageName)
                        .setClassName(MAIN_ACTIVITY_CLASS)
                        .build(),
                )
                .build(),
        )
        .build()

internal fun reloadClickable(id: String): Clickable =
    Clickable.Builder()
        .setId(id)
        .setOnClick(ActionBuilders.LoadAction.Builder().build())
        .build()

internal fun handleTrackTileAction(context: Context, requestParams: RequestBuilders.TileRequest): Unit {
    if (requestParams.currentState?.lastClickableId != TILE_CLICK_TOGGLE_TRACK) return
    val action = if (ComplicationReading.load(context).tracking) {
        MonitoringService.ACTION_STOP_TRACK
    } else {
        MonitoringService.ACTION_START_TRACK
    }
    context.startForegroundService(
        Intent(context, MonitoringService::class.java).setAction(action),
    )
}
