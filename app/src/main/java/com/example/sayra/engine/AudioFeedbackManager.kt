package com.example.sayra.engine

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class AudioFeedbackManager {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val sampleRate = 44100

    fun playListenStart() {
        scope.launch {
            playTones(
                frequencies = floatArrayOf(587.33f, 880f), // D5, A5
                durationMs = 90
            )
        }
    }

    fun playListenStop() {
        scope.launch {
            playTones(
                frequencies = floatArrayOf(783.99f, 523.25f), // G5, C5
                durationMs = 80
            )
        }
    }

    fun playSuccess() {
        scope.launch {
            playTones(
                frequencies = floatArrayOf(659.25f, 987.77f), // E5, B5
                durationMs = 120
            )
        }
    }

    fun playError() {
        scope.launch {
            playTones(
                frequencies = floatArrayOf(349.23f, 261.63f), // F4, C4
                durationMs = 110
            )
        }
    }

    private fun playTones(frequencies: FloatArray, durationMs: Int) {
        try {
            val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toFloat() / sampleRate
                val envelope = exp(-3.0 * (i.toDouble() / numSamples)).toFloat() // Smooth exponential fade out

                var sample = 0f
                for (freq in frequencies) {
                    sample += sin(2.0 * PI * freq * t).toFloat()
                }
                sample /= frequencies.size
                buffer[i] = (sample * envelope * 0.35f * Short.MAX_VALUE).toInt().toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
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
            Thread.sleep(durationMs.toLong() + 50)
            audioTrack.release()
        } catch (_: Exception) {
            // Ignore audio generation failures gracefully
        }
    }
}
