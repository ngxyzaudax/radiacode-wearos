package com.ngxyzaudax.radiacode.log

import android.util.Log

object RadiaLog {
    const val TAG: String = "RadiaCode"

    fun debug(component: String, message: String): Unit {
        Log.d(TAG, format(component, message))
    }

    fun info(component: String, message: String): Unit {
        Log.i(TAG, format(component, message))
    }

    fun warn(component: String, message: String): Unit {
        Log.w(TAG, format(component, message))
    }

    fun error(component: String, message: String, cause: Throwable? = null): Unit {
        if (cause == null) Log.e(TAG, format(component, message))
        else Log.e(TAG, format(component, message), cause)
    }

    private fun format(component: String, message: String): String = "$component | $message"
}
