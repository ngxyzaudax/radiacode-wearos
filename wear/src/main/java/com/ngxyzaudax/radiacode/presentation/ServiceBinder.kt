package com.ngxyzaudax.radiacode.presentation

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.ngxyzaudax.radiacode.service.MonitoringService
import kotlinx.coroutines.CompletableDeferred

internal class ServiceBinder(
    private val onConnected: (MonitoringService) -> Unit,
    private val onDisconnected: () -> Unit,
) {
    private var bindDeferred: CompletableDeferred<Unit>? = null
    var binding: Boolean = false
        private set
    var service: MonitoringService? = null
        private set

    val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = (binder as? MonitoringService.LocalBinder)?.service
            binding = false
            service?.let(onConnected)
            bindDeferred?.complete(Unit)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            binding = false
            onDisconnected()
            bindDeferred?.completeExceptionally(IllegalStateException("Service disconnected"))
        }
    }

    fun ensure(context: Context): Boolean {
        if (service != null || binding) return true
        if (!DevicePermissions.hasAll(context)) return false
        binding = true
        bindDeferred = CompletableDeferred()
        val intent = Intent(context, MonitoringService::class.java)
        context.startForegroundService(intent)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        return true
    }

    fun unbind(context: Context): Unit {
        if (service != null) runCatching { context.unbindService(connection) }
        service = null
        binding = false
    }

    suspend fun awaitBinding(): Unit {
        bindDeferred?.let { deferred -> deferred.await() }
    }
}
