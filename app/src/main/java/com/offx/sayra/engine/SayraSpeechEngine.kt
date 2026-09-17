package com.offx.sayra.engine

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

class SayraSpeechEngine(private val context: Context, private val onText: (String) -> Unit, private val onError: (Int) -> Unit) {
    private var recognizer: SpeechRecognizer? = null
    fun start(language: String = "hi-IN") {
        stop(); recognizer = SpeechRecognizer.createSpeechRecognizer(context).also { r ->
            r.setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: Bundle) { results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let(onText) }
                override fun onError(error: Int) { onError(error) }
                override fun onReadyForSpeech(p: Bundle?) = Unit; override fun onBeginningOfSpeech() = Unit; override fun onRmsChanged(v: Float) = Unit; override fun onBufferReceived(b: ByteArray?) = Unit; override fun onEndOfSpeech() = Unit; override fun onPartialResults(b: Bundle?) = Unit; override fun onEvent(t: Int, b: Bundle?) = Unit
            })
            r.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply { putExtra(RecognizerIntent.EXTRA_LANGUAGE, language); putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true) })
        }
    }
    fun stop() { recognizer?.destroy(); recognizer = null }
}
