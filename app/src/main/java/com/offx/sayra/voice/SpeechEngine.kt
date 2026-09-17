package com.offx.sayra.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

class SpeechEngine(private val context: Context, private val onText: (String) -> Unit) {
    private var recognizer: SpeechRecognizer? = null
    fun start() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: Bundle) { results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let(onText) }
                override fun onError(error: Int) {}
                override fun onReadyForSpeech(p: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(r: Float) {}
                override fun onBufferReceived(b: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(p: Bundle?) {}
                override fun onEvent(t: Int, p: Bundle?) {}
            })
            startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            })
        }
    }
    fun stop() { recognizer?.stopListening(); recognizer?.destroy(); recognizer = null }
    companion object {
        fun speak(context: Context, text: String, done: () -> Unit = {}) {
            val tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts.language = Locale.getDefault(); tts.setSpeechRate(.95f); tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "sayra")
                }
                done()
            }
        }
    }
}
