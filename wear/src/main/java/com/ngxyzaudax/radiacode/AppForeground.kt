package com.ngxyzaudax.radiacode

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.util.concurrent.atomic.AtomicInteger

/**
 * Tiles and complications are invisible while the app UI is on screen, so refreshing them there
 * only steals main thread time from rendering. This tracks whether any activity is resumed.
 */
object AppForeground {
    private val resumedActivities = AtomicInteger(0)

    val isForeground: Boolean get() = resumedActivities.get() > 0

    fun track(application: Application): Unit =
        application.registerActivityLifecycleCallbacks(callbacks)

    private val callbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityResumed(activity: Activity): Unit {
            resumedActivities.incrementAndGet()
        }

        override fun onActivityPaused(activity: Activity): Unit {
            resumedActivities.decrementAndGet()
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?): Unit = Unit

        override fun onActivityStarted(activity: Activity): Unit = Unit

        override fun onActivityStopped(activity: Activity): Unit = Unit

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle): Unit = Unit

        override fun onActivityDestroyed(activity: Activity): Unit = Unit
    }
}
