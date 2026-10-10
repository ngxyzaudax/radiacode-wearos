package com.ngxyzaudax.radiacode.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.ngxyzaudax.radiacode.R
import com.ngxyzaudax.radiacode.RadiaCodeApp
import com.ngxyzaudax.radiacode.data.LiveReading
import com.ngxyzaudax.radiacode.presentation.MainActivity

internal object MonitoringNotification {
    private const val CHANNEL_ID = "radiacode_monitor"
    const val NOTIFICATION_ID = 1
    private var lastPostedText: String? = null

    fun createChannel(context: Context): Unit {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "RadiaCode", NotificationManager.IMPORTANCE_LOW),
        )
    }

    fun build(context: Context, reading: LiveReading): Notification {
        val launchIntent = PendingIntent.getActivity(
            context,
            0,
            android.content.Intent(context, MainActivity::class.java).apply {
                action = android.content.Intent.ACTION_MAIN
                addCategory(android.content.Intent.CATEGORY_LAUNCHER)
                flags = android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val text = statusText(context, reading)
        lastPostedText = text
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_stat_radiacode)
            .setContentIntent(launchIntent)
            .setOngoing(true)
        OngoingActivity.Builder(context, NOTIFICATION_ID, builder)
            .setStaticIcon(R.drawable.ic_stat_radiacode)
            .setTouchIntent(launchIntent)
            .setStatus(Status.Builder().addPart("dose", Status.TextPart(text)).build())
            .build()
            .apply(context)
        return builder.build()
    }

    fun update(context: Context, reading: LiveReading): Unit {
        if (statusText(context, reading) == lastPostedText) return
        context.getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, build(context, reading))
    }

    private fun statusText(context: Context, reading: LiveReading): String =
        if (reading.connected) {
            "${String.format("%.2f", reading.doseRate)} ${reading.doseLabel}"
        } else {
            context.getString(R.string.not_connected)
        }
}
