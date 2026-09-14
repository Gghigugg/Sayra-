package com.example.sayra.engine

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.sin

class TextToSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speakingAmplitude = MutableStateFlow(0f)
    val speakingAmplitude: StateFlow<Float> = _speakingAmplitude.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)
    private var waveformJob: Job? = null

    private var speechRate: Float = 1.0f
    private var speechPitch: Float = 1.05f

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setSpeechRate(speechRate)
            tts?.setPitch(speechPitch)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    startSpeakingWaveform()
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    stopSpeakingWaveform()
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    stopSpeakingWaveform()
                }
            })
        }
    }

    fun updateSettings(rate: Float, pitch: Float) {
        this.speechRate = rate
        this.speechPitch = pitch
        tts?.setSpeechRate(rate)
        tts?.setPitch(pitch)
    }

    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        if (!isInitialized || text.isBlank()) return

        stop()

        // Clean out emojis, markdown asterisks and bullets for natural human voice
        val cleanSpeech = cleanTextForSpeech(text)

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "sayra_response_${System.currentTimeMillis()}")
        }

        tts?.speak(cleanSpeech, TextToSpeech.QUEUE_FLUSH, params, params.getString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID))
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
        _isSpeaking.value = false
        stopSpeakingWaveform()
    }

    fun shutdown() {
        stop()
        try {
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
    }

    private fun startSpeakingWaveform() {
        waveformJob?.cancel()
        waveformJob = scope.launch {
            var step = 0.0
            while (isActive && _isSpeaking.value) {
                // Multi-harmonic cadence wave simulating real voice modulation
                val wave = 0.45f + 0.35f * sin(step).toFloat() + 0.2f * sin(step * 2.3).toFloat()
                _speakingAmplitude.value = wave.coerceIn(0.1f, 1.0f)
                step += 0.35
                delay(40)
            }
            _speakingAmplitude.value = 0f
        }
    }

    private fun stopSpeakingWaveform() {
        waveformJob?.cancel()
        waveformJob = null
        _speakingAmplitude.value = 0f
    }

    private fun cleanTextForSpeech(input: String): String {
        return input
            .replace(Regex("[*#_`~>]"), "") // Markdown formatting
            .replace(Regex("\\[(.*?)\\]\\(.*?\\)"), "$1") // Links
            .replace(Regex("\\bhttps?://\\S+"), "web link") // URLs
            .trim()
    }
}
