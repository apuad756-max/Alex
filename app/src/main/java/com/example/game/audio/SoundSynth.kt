package com.example.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class SoundSynth(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var soundEnabled = true
    private var hapticEnabled = true

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun toggleSound(): Boolean {
        soundEnabled = !soundEnabled
        return soundEnabled
    }

    fun isSoundEnabled(): Boolean = soundEnabled

    fun toggleHaptics(): Boolean {
        hapticEnabled = !hapticEnabled
        return hapticEnabled
    }

    fun isHapticsEnabled(): Boolean = hapticEnabled

    fun vibrate(durationMs: Long = 20, amplitude: Int = 120) {
        if (!hapticEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255)))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (e: Exception) {
            // Ignore if vibration fails
        }
    }

    fun playDash() {
        if (!soundEnabled) return
        vibrate(15, 80)
        scope.launch {
            playToneSweep(startFreq = 400f, endFreq = 900f, durationMs = 80, waveform = Waveform.SINE)
        }
    }

    fun playSlash() {
        if (!soundEnabled) return
        vibrate(30, 180)
        scope.launch {
            playNoiseSweep(durationMs = 90)
        }
    }

    fun playShardPickup() {
        if (!soundEnabled) return
        vibrate(10, 60)
        scope.launch {
            playTone(880f, 40)
            playTone(1320f, 60)
        }
    }

    fun playCombo(combo: Int) {
        if (!soundEnabled) return
        vibrate(25, 120)
        scope.launch {
            val baseFreq = 523f + (combo * 65f).coerceAtMost(800f)
            playTone(baseFreq, 50)
            playTone(baseFreq * 1.25f, 60)
            playTone(baseFreq * 1.5f, 80)
        }
    }

    fun playHit() {
        if (!soundEnabled) return
        vibrate(50, 240)
        scope.launch {
            playToneSweep(startFreq = 300f, endFreq = 80f, durationMs = 120, waveform = Waveform.SQUARE)
        }
    }

    fun playNova() {
        if (!soundEnabled) return
        vibrate(80, 255)
        scope.launch {
            playToneSweep(startFreq = 200f, endFreq = 1200f, durationMs = 250, waveform = Waveform.TRIANGLE)
        }
    }

    fun playGameOver() {
        if (!soundEnabled) return
        vibrate(100, 200)
        scope.launch {
            playToneSweep(startFreq = 450f, endFreq = 120f, durationMs = 300, waveform = Waveform.SINE)
        }
    }

    fun playClick() {
        if (!soundEnabled) return
        vibrate(10, 50)
        scope.launch {
            playTone(700f, 25)
        }
    }

    private enum class Waveform { SINE, SQUARE, TRIANGLE }

    private fun playTone(freq: Float, durationMs: Int) {
        try {
            val sampleRate = 22050
            val numSamples = (durationMs * sampleRate / 1000)
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val time = i.toDouble() / sampleRate
                val env = 1.0 - (i.toDouble() / numSamples) // decay envelope
                val sample = (sin(2.0 * PI * freq * time) * Short.MAX_VALUE * 0.4 * env).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            writeAndPlay(buffer, sampleRate)
        } catch (e: Exception) {
            // AudioTrack error handling
        }
    }

    private fun playToneSweep(startFreq: Float, endFreq: Float, durationMs: Int, waveform: Waveform) {
        try {
            val sampleRate = 22050
            val numSamples = (durationMs * sampleRate / 1000)
            val buffer = ShortArray(numSamples)
            var phase = 0.0

            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val currentFreq = startFreq + (endFreq - startFreq) * progress
                val env = 1.0 - (progress * 0.8) // decay

                phase += 2.0 * PI * currentFreq / sampleRate
                val rawSample = when (waveform) {
                    Waveform.SINE -> sin(phase)
                    Waveform.SQUARE -> if (sin(phase) >= 0) 0.6 else -0.6
                    Waveform.TRIANGLE -> (2.0 / PI) * kotlin.math.asin(sin(phase))
                }

                val sample = (rawSample * Short.MAX_VALUE * 0.35 * env).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            writeAndPlay(buffer, sampleRate)
        } catch (e: Exception) {
            // AudioTrack error handling
        }
    }

    private fun playNoiseSweep(durationMs: Int) {
        try {
            val sampleRate = 22050
            val numSamples = (durationMs * sampleRate / 1000)
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val env = 1.0 - progress
                val noise = (Math.random() * 2.0 - 1.0)
                val sample = (noise * Short.MAX_VALUE * 0.3 * env).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            writeAndPlay(buffer, sampleRate)
        } catch (e: Exception) {
            // AudioTrack error handling
        }
    }

    private fun writeAndPlay(buffer: ShortArray, sampleRate: Int) {
        var audioTrack: AudioTrack? = null
        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()

            // Release after playing duration
            Thread.sleep((buffer.size.toLong() * 1000L / sampleRate) + 20L)
        } catch (e: Exception) {
            // Ignore audio track interruption
        } finally {
            try {
                audioTrack?.stop()
                audioTrack?.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
