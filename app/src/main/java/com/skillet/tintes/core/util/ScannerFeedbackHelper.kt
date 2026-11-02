package com.skillet.tintes.core.util

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RawRes
import com.skillet.tintes.R

object ScannerFeedbackHelper {

    /**
     * Emite el sonido de escaneo (usa R.raw.barcode por defecto) y una vibración breve.
     */
    fun playBeepAndVibrate(context: Context, @RawRes customSoundResId: Int? = R.raw.barcode) {
        val soundId = customSoundResId ?: R.raw.barcode

        var playedSuccessfully = false
        try {
            val mediaPlayer = MediaPlayer.create(context, soundId)
            if (mediaPlayer != null) {
                mediaPlayer.setOnCompletionListener { mp ->
                    mp.release()
                }
                mediaPlayer.start()
                playedSuccessfully = true
            }
        } catch (_: Exception) {
            playedSuccessfully = false
        }

        if (!playedSuccessfully) {
            playDefaultTone()
        }

        // Vibración Háptica
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (vibrator?.hasVibrator() == true) {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(100)
                }
            }
        } catch (_: Exception) {
            // Ignorar en caso de que el dispositivo no tenga soporte de vibración
        }
    }

    private fun playDefaultTone() {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 90)
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        } catch (_: Exception) {
            // Ignorar
        }
    }
}
