package com.offx.sayra.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.offx.sayra.actions.ActionController
import com.offx.sayra.data.GeminiClient
import com.offx.sayra.data.SayraStore
import com.offx.sayra.voice.SpeechEngine
import kotlinx.coroutines.launch

class SayraViewModel(app: Application) : AndroidViewModel(app) {
    enum class State { IDLE, LISTENING, THINKING, SPEAKING, ERROR }
    var state by mutableStateOf(State.IDLE); private set
    var status by mutableStateOf("Ready"); private set
    var lastText by mutableStateOf(""); private set
    private val store = SayraStore(app)
    private val speech = SpeechEngine(app) { text -> handleText(text) }

    fun toggleListening(context: Context) {
        if (state == State.LISTENING) stopListening() else startListening()
    }
    fun startListening() {
        state = State.LISTENING; status = "Listening…"; speech.start()
    }
    fun stopListening() { speech.stop(); state = State.IDLE; status = "Ready" }

    private fun handleText(text: String) {
        lastText = text
        if (text.lowercase().contains("hey sayra")) {
            val command = text.lowercase().substringAfter("hey sayra").trim()
            if (command.isNotEmpty()) processCommand(command) else { status = "Yes, I'm listening"; return }
        } else if (state == State.LISTENING) processCommand(text)
    }

    private fun processCommand(command: String) {
        state = State.THINKING; status = "Thinking…"
        viewModelScope.launch {
            val key = store.apiKey()
            if (key.isBlank()) {
                state = State.ERROR; status = "Add your Gemini API key in Settings"; return@launch
            }
            val result = ActionController(getApplication()).tryLocalAction(command)
            if (result != null) {
                speak(result); return@launch
            }
            val answer = GeminiClient(key).generate(command)
            speak(answer)
        }
    }

    private fun speak(text: String) {
        lastText = text; state = State.SPEAKING; status = "SAYRA"
        SpeechEngine.speak(getApplication(), text) { state = State.IDLE; status = "Ready" }
    }

    fun apiKey() = store.apiKey()
    fun saveApiKey(value: String) { store.saveApiKey(value) }
    fun wakeWordEnabled() = store.wakeWordEnabled()
    fun setWakeWordEnabled(enabled: Boolean) { store.setWakeWordEnabled(enabled) }
}
