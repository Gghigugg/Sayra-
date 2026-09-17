package com.example.sayra.engine

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.sayra.data.model.LanguageMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechRecognitionManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var activeLanguageMode = LanguageMode.AUTO
    private var continuousWakeWordMode = false
    private var shouldKeepListening = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private val _finalResult = MutableStateFlow<String?>(null)
    val finalResult: StateFlow<String?> = _finalResult.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    var onSpeechFinal: ((String) -> Unit)? = null
    var onWakeWordDetected: (() -> Unit)? = null

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    /**
     * Starts normal one-shot recognition, or wake-word aware continuous recognition.
     * Continuous mode is intentionally opt-in and is not a background service by itself.
     */
    fun startListening(
        languageMode: LanguageMode = LanguageMode.AUTO,
        continuousWakeWord: Boolean = false
    ) {
        stopListening()

        activeLanguageMode = languageMode
        continuousWakeWordMode = continuousWakeWord
        shouldKeepListening = continuousWakeWordMode

        _errorState.value = null
        _partialText.value = ""
        _finalResult.value = null

        if (!isAvailable()) {
            _errorState.value = "Speech recognition is not supported on this device"
            return
        }

        startRecognizer()
    }

    private fun startRecognizer() {
        if (!shouldKeepListening && speechRecognizer != null) return

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                }

                override fun onBeginningOfSpeech() {
                    _isListening.value = true
                }

                override fun onRmsChanged(rmsdB: Float) {
                    val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                    _rmsLevel.value = normalized
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _isListening.value = false
                    _rmsLevel.value = 0f
                }

                override fun onError(error: Int) {
                    _isListening.value = false
                    _rmsLevel.value = 0f

                    if (shouldKeepListening && error != SpeechRecognizer.ERROR_CLIENT) {
                        // Recreate the recognizer after transient recognition errors.
                        speechRecognizer?.destroy()
                        speechRecognizer = null
                        startRecognizer()
                        return
                    }

                    val msg = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> null
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error during speech recognition"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timed out"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Voice recognizer is busy"
                        SpeechRecognizer.ERROR_SERVER -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                        else -> "Recognition error ($error)"
                    }
                    if (msg != null && error != SpeechRecognizer.ERROR_NO_MATCH) {
                        _errorState.value = msg
                    }
                }

                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    _rmsLevel.value = 0f

                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spoken = matches?.firstOrNull()?.trim().orEmpty()

                    if (spoken.isNotEmpty()) {
                        _finalResult.value = spoken
                        _partialText.value = spoken

                        if (continuousWakeWordMode) {
                            val wakeIndex = findWakeWordIndex(spoken)
                            if (wakeIndex >= 0) {
                                onWakeWordDetected?.invoke()
                                val command = spoken.substring(wakeIndex + WAKE_WORD.length).trim()
                                if (command.isNotBlank()) {
                                    onSpeechFinal?.invoke(command)
                                }
                            }
                        } else {
                            onSpeechFinal?.invoke(spoken)
                        }
                    }

                    if (shouldKeepListening) {
                        speechRecognizer?.destroy()
                        speechRecognizer = null
                        startRecognizer()
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull().orEmpty()
                    if (text.isNotEmpty()) {
                        _partialText.value = text
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)

            when (activeLanguageMode) {
                LanguageMode.HINDI -> {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
                }
                LanguageMode.ENGLISH -> {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US")
                }
                LanguageMode.HINGLISH, LanguageMode.AUTO -> {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                    putExtra(
                        "android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES",
                        arrayOf("hi-IN", "en-US", "en-IN")
                    )
                }
            }
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _errorState.value = "Failed to start listening: ${e.message}"
            _isListening.value = false
            if (shouldKeepListening) {
                speechRecognizer?.destroy()
                speechRecognizer = null
            }
        }
    }

    private fun findWakeWordIndex(spoken: String): Int {
        val normalized = spoken.lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        return normalized.indexOf(WAKE_WORD)
    }

    fun stopListening() {
        shouldKeepListening = false
        continuousWakeWordMode = false
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
        _isListening.value = false
        _rmsLevel.value = 0f
    }

    companion object {
        private const val WAKE_WORD = "hey sayra"
    }
}
