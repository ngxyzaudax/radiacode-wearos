package com.ngxyzaudax.radiacode.alert

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class WatchAlerter(context: Context) {
    private val vibrator: Vibrator? = vibratorFrom(context)
    private val toneGenerator: ToneGenerator? = runCatching {
        ToneGenerator(AudioManager.STREAM_ALARM, 80)
    }.getOrNull()

    fun vibrate(levelTwo: Boolean): Unit {
        val effect = if (levelTwo) levelTwoPattern() else levelOnePattern()
        vibrator?.vibrate(effect)
    }

    fun beep(levelTwo: Boolean): Unit {
        val tone = if (levelTwo) ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD else ToneGenerator.TONE_PROP_BEEP
        toneGenerator?.startTone(tone, if (levelTwo) 400 else 200)
    }

    private fun levelOnePattern(): VibrationEffect =
        VibrationEffect.createWaveform(longArrayOf(0, 120, 80, 120), -1)

    private fun levelTwoPattern(): VibrationEffect =
        VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 200, 100, 300), -1)

    private fun vibratorFrom(context: Context): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
}
