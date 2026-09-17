package com.example.sayra.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sayra.character.CharacterAttentionTarget
import com.example.sayra.character.CharacterExpression
import com.example.sayra.character.CharacterPerformanceMode
import com.example.sayra.character.CharacterState
import com.example.sayra.character.CharacterStateController
import com.example.sayra.character.CharacterViewMode
import com.example.sayra.data.db.ConversationEntity
import com.example.sayra.data.db.ConversationRepository
import com.example.sayra.data.db.MemoryEntity
import com.example.sayra.data.db.MemoryRepository
import com.example.sayra.data.db.SayraDatabase
import com.example.sayra.data.gemini.ChatMessage
import com.example.sayra.data.gemini.GeminiClient
import com.example.sayra.data.gemini.GeminiResult
import com.example.sayra.data.gemini.GeminiToolCall
import com.example.sayra.data.model.ActionConfirmation
import com.example.sayra.data.model.LanguageMode
import com.example.sayra.data.model.OrbState
import com.example.sayra.data.model.ToolExecutionInfo
import com.example.sayra.data.storage.PreferencesManager
import com.example.sayra.data.storage.SecureKeyStorage
import com.example.sayra.engine.AndroidActionController
import com.example.sayra.engine.AudioFeedbackManager
import com.example.sayra.engine.HapticManager
import com.example.sayra.engine.SpeechRecognitionManager
import com.example.sayra.engine.TextToSpeechManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SayraViewModel(application: Application) : AndroidViewModel(application) {

    val secureStorage = SecureKeyStorage(application)
    val prefsManager = PreferencesManager(application)
    private val database = SayraDatabase.getInstance(application)
    val repository = ConversationRepository(database.conversationDao())
    val memoryRepository = MemoryRepository(database.memoryDao())

    val actionController = AndroidActionController(application)
    val speechManager = SpeechRecognitionManager(application)
    val ttsManager = TextToSpeechManager(application)
    val audioFeedback = AudioFeedbackManager()
    val haptic = HapticManager(application)
    private val geminiClient = GeminiClient()

    private val _orbState = MutableStateFlow(OrbState.IDLE)
    val orbState: StateFlow<OrbState> = _orbState.asStateFlow()

    private val _userPrompt = MutableStateFlow("")
    val userPrompt: StateFlow<String> = _userPrompt.asStateFlow()

    private val _assistantResponse = MutableStateFlow("")
    val assistantResponse: StateFlow<String> = _assistantResponse.asStateFlow()

    private val _toolExecution = MutableStateFlow<ToolExecutionInfo?>(null)
    val toolExecution: StateFlow<ToolExecutionInfo?> = _toolExecution.asStateFlow()

    private val _isTextInputOpen = MutableStateFlow(false)
    val isTextInputOpen: StateFlow<Boolean> = _isTextInputOpen.asStateFlow()

    private val _textInputValue = MutableStateFlow("")
    val textInputValue: StateFlow<String> = _textInputValue.asStateFlow()

    private val _actionConfirmation = MutableStateFlow<ActionConfirmation?>(null)
    val actionConfirmation: StateFlow<ActionConfirmation?> = _actionConfirmation.asStateFlow()

    val conversations: StateFlow<List<ConversationEntity>> = repository.allConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memories: StateFlow<List<MemoryEntity>> = memoryRepository.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isOnboardingDone: StateFlow<Boolean> = prefsManager.onboardingDoneFlow
    val languageMode: StateFlow<LanguageMode> = prefsManager.languageModeFlow
    val isDarkTheme: StateFlow<Boolean> = prefsManager.darkThemeFlow
    val isPrivacyMode: StateFlow<Boolean> = prefsManager.privacyModeFlow
    val isWakeWordEnabled: StateFlow<Boolean> = prefsManager.wakeWordFlow

    private val _speechRate = MutableStateFlow(prefsManager.getSpeechRate())
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _speechPitch = MutableStateFlow(prefsManager.getSpeechPitch())
    val speechPitch: StateFlow<Float> = _speechPitch.asStateFlow()

    private val _orbIntensity = MutableStateFlow(prefsManager.getOrbIntensity())
    val orbIntensity: StateFlow<Float> = _orbIntensity.asStateFlow()

    private val _aiModel = MutableStateFlow(prefsManager.getAiModel())
    val aiModel: StateFlow<String> = prefsManager.modelFlow

    val characterStateController = CharacterStateController(viewModelScope)
    val isCharacterEnabled: StateFlow<Boolean> = prefsManager.characterEnabledFlow
    val characterState: StateFlow<CharacterState> = characterStateController.characterState
    val characterExpression: StateFlow<CharacterExpression> = characterStateController.expression
    val characterAttentionTarget: StateFlow<CharacterAttentionTarget> = characterStateController.attentionTarget

    private val _characterPerformanceMode = MutableStateFlow(
        try {
            CharacterPerformanceMode.valueOf(prefsManager.getCharacterPerformance())
        } catch (_: Exception) {
            CharacterPerformanceMode.HIGH
        }
    )
    val characterPerformanceMode: StateFlow<CharacterPerformanceMode> = _characterPerformanceMode.asStateFlow()

    private val _characterViewMode = MutableStateFlow(
        try {
            CharacterViewMode.valueOf(prefsManager.getCharacterViewMode())
        } catch (_: Exception) {
            CharacterViewMode.CHARACTER
        }
    )
    val characterViewMode: StateFlow<CharacterViewMode> = _characterViewMode.asStateFlow()

    val audioAmplitude: StateFlow<Float> = combine(
        speechManager.rmsLevel,
        ttsManager.speakingAmplitude,
        _orbState
    ) { micRms, ttsAmp, state ->
        when (state) {
            OrbState.LISTENING -> micRms
            OrbState.SPEAKING -> ttsAmp
            else -> 0f
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    init {
        ttsManager.updateSettings(_speechRate.value, _speechPitch.value)

        speechManager.onSpeechFinal = { spokenText ->
            if (spokenText.isNotBlank()) {
                _userPrompt.value = spokenText
                processUserRequest(spokenText)
            }
        }

        viewModelScope.launch {
            speechManager.partialText.collect { partial ->
                if (speechManager.isListening.value && partial.isNotEmpty()) {
                    _userPrompt.value = partial
                }
            }
        }

        viewModelScope.launch {
            speechManager.errorState.collect { err ->
                if (err != null) {
                    _orbState.value = OrbState.ERROR
                    audioFeedback.playError()
                    haptic.error()
                    _assistantResponse.value = err
                    delay(2500)
                    if (_orbState.value == OrbState.ERROR) {
                        _orbState.value = OrbState.IDLE
                    }
                }
            }
        }

        viewModelScope.launch {
            ttsManager.isSpeaking.collect { speaking ->
                if (!speaking && _orbState.value == OrbState.SPEAKING) {
                    _orbState.value = OrbState.IDLE
                }
            }
        }

        viewModelScope.launch {
            _orbState.collect { state ->
                characterStateController.updateFromOrbState(state, _assistantResponse.value.ifEmpty { _userPrompt.value })
            }
        }

        viewModelScope.launch {
            _assistantResponse.collect { resp ->
                if (_orbState.value == OrbState.SPEAKING && resp.isNotBlank()) {
                    characterStateController.updateFromOrbState(OrbState.SPEAKING, resp)
                }
            }
        }
    }

    fun toggleListening() {
        haptic.tap()

        if (ttsManager.isSpeaking.value) {
            ttsManager.stop()
            _orbState.value = OrbState.IDLE
            return
        }

        if (speechManager.isListening.value) {
            speechManager.stopListening()
            audioFeedback.playListenStop()
            _orbState.value = OrbState.IDLE
        } else {
            _toolExecution.value = null
            _assistantResponse.value = ""
            _userPrompt.value = ""
            _orbState.value = OrbState.LISTENING
            audioFeedback.playListenStart()
            haptic.listenStart()
            speechManager.startListening(
                languageMode = prefsManager.getLanguageMode(),
                continuousWakeWord = prefsManager.isWakeWordEnabled()
            )
        }
    }

    fun toggleTextInput() {
        haptic.tap()
        _isTextInputOpen.value = !_isTextInputOpen.value
    }

    fun onTextInputChange(text: String) {
        _textInputValue.value = text
    }

    fun sendTextMessage(text: String) {
        if (text.isBlank()) return
        haptic.tap()
        _isTextInputOpen.value = false
        _textInputValue.value = ""
        _userPrompt.value = text
        _toolExecution.value = null
        _assistantResponse.value = ""
        processUserRequest(text)
    }

    fun processUserRequest(prompt: String) {
        val apiKey = secureStorage.getApiKey()
        if (apiKey.isEmpty()) {
            _orbState.value = OrbState.ERROR
            audioFeedback.playError()
            haptic.error()
            val noKeyMsg = "Connect your Gemini API key to start using SAYRA."
            _assistantResponse.value = noKeyMsg
            ttsManager.speak(noKeyMsg)
            return
        }

        _orbState.value = OrbState.THINKING

        viewModelScope.launch {
            val recentList = if (!prefsManager.isPrivacyModeEnabled()) {
                conversations.value.take(4).reversed().flatMap {
                    listOf(
                        ChatMessage("user", it.userQuery),
                        ChatMessage("model", it.assistantResponse)
                    )
                }
            } else emptyList()

            val rememberedFacts = if (!prefsManager.isPrivacyModeEnabled()) {
                memoryRepository.getAllMemoriesList().map { it.fact }
            } else emptyList()

            val result = geminiClient.generateAssistantResponse(
                apiKey = apiKey,
                userPrompt = prompt,
                conversationHistory = recentList,
                model = _aiModel.value,
                knownMemories = rememberedFacts
            )

            when (result) {
                is GeminiResult.Success -> handleGeminiSuccess(prompt, result.text, result.toolCall)
                is GeminiResult.Error -> {
                    _orbState.value = OrbState.ERROR
                    audioFeedback.playError()
                    haptic.error()
                    val errText = if (result.isAuthError) {
                        "Your Gemini API key could not be verified. Please check your key in Settings and try again."
                    } else result.message
                    _assistantResponse.value = errText
                    ttsManager.speak(errText)
                    if (!prefsManager.isPrivacyModeEnabled()) {
                        repository.saveConversation(prompt, errText, null, false)
                    }
                }
            }
        }
    }

    private suspend fun handleGeminiSuccess(
        prompt: String,
        responseText: String,
        toolCall: GeminiToolCall?
    ) {
        var actionResult: ToolExecutionInfo? = null

        if (toolCall != null) {
            if (toolCall.name == "makePhoneCall" && prefsManager.isConfirmSensitiveEnabled()) {
                val phone = toolCall.args["phoneNumber"]?.toString() ?: ""
                val contact = toolCall.args["contactName"]?.toString()
                val targetName = if (!contact.isNullOrBlank()) contact else phone
                _actionConfirmation.value = ActionConfirmation(
                    title = "Confirm Phone Call",
                    description = "Do you want SAYRA to call $targetName ($phone)?",
                    confirmLabel = "Call Now",
                    onConfirm = {
                        _actionConfirmation.value = null
                        val exec = actionController.makePhoneCall(phone, contact)
                        executeToolSuccessFlow(prompt, responseText, exec)
                    },
                    onCancel = {
                        _actionConfirmation.value = null
                        val cancelText = "Call to $targetName cancelled."
                        _assistantResponse.value = cancelText
                        ttsManager.speak(cancelText)
                    }
                )
                return
            }

            if (toolCall.name == "composeSms" && prefsManager.isConfirmSensitiveEnabled()) {
                val phone = toolCall.args["phoneNumber"]?.toString().orEmpty()
                val msg = toolCall.args["message"]?.toString().orEmpty()
                val target = if (phone.isNotBlank()) phone else "recipient"
                _actionConfirmation.value = ActionConfirmation(
                    title = "Confirm Message",
                    description = "Do you want SAYRA to open SMS for $target with message: \"$msg\"?",
                    confirmLabel = "Open SMS",
                    onConfirm = {
                        _actionConfirmation.value = null
                        val exec = actionController.composeSms(phone, msg)
                        executeToolSuccessFlow(prompt, responseText, exec)
                    },
                    onCancel = {
                        _actionConfirmation.value = null
                        val cancelText = "Message sending cancelled."
                        _assistantResponse.value = cancelText
                        ttsManager.speak(cancelText)
                    }
                )
                return
            }

            actionResult = executeTool(toolCall)
            _toolExecution.value = actionResult
            if (actionResult.isSuccess) {
                _orbState.value = OrbState.ACTION_SUCCESS
                audioFeedback.playSuccess()
                haptic.actionSuccess()
                delay(500)
            } else {
                _orbState.value = OrbState.ERROR
                audioFeedback.playError()
                haptic.error()
                delay(400)
            }
        }

        val speechText = if (responseText.isNotBlank()) responseText else actionResult?.details ?: "Done."
        _assistantResponse.value = speechText
        _orbState.value = OrbState.SPEAKING
        ttsManager.speak(speechText)

        if (!prefsManager.isPrivacyModeEnabled()) {
            repository.saveConversation(prompt, speechText, actionResult?.displayName, actionResult?.isSuccess ?: true)
        }
    }

    private fun executeToolSuccessFlow(prompt: String, responseText: String, exec: ToolExecutionInfo) {
        _toolExecution.value = exec
        _orbState.value = OrbState.ACTION_SUCCESS
        audioFeedback.playSuccess()
        haptic.actionSuccess()
        val speechText = if (responseText.isNotBlank()) responseText else exec.details
        _assistantResponse.value = speechText
        _orbState.value = OrbState.SPEAKING
        ttsManager.speak(speechText)
        if (!prefsManager.isPrivacyModeEnabled()) {
            viewModelScope.launch {
                repository.saveConversation(prompt, speechText, exec.displayName, exec.isSuccess)
            }
        }
    }

    private suspend fun executeTool(toolCall: GeminiToolCall): ToolExecutionInfo {
        return when (toolCall.name) {
            "openApp" -> actionController.openApp(toolCall.args["appName"]?.toString() ?: "App")
            "openSettings" -> actionController.openSettings(toolCall.args["settingType"]?.toString() ?: "settings")
            "getBatteryStatus" -> actionController.getBatteryStatus()
            "controlFlashlight" -> actionController.controlFlashlight((toolCall.args["turnOn"] as? Boolean) ?: true)
            "controlVolume" -> {
                val dir = toolCall.args["direction"]?.toString() ?: "up"
                val level = (toolCall.args["level"] as? Number)?.toInt()
                actionController.controlVolume(dir, level)
            }
            "controlBrightness" -> actionController.controlBrightness((toolCall.args["levelPercent"] as? Number)?.toInt() ?: 50)
            "openUrl" -> actionController.openUrl(toolCall.args["url"]?.toString() ?: "https://google.com")
            "saveMemory" -> {
                if (prefsManager.isPrivacyModeEnabled()) {
                    ToolExecutionInfo("saveMemory", "Privacy Mode Active", "Privacy Mode is currently on. SAYRA will not store new memories.", false, "privacy")
                } else {
                    val fact = toolCall.args["fact"]?.toString() ?: ""
                    val category = toolCall.args["category"]?.toString() ?: "general"
                    if (fact.isNotBlank()) {
                        memoryRepository.saveMemory(fact, category)
                        ToolExecutionInfo("saveMemory", "Memory Saved", "Remembered: \"$fact\"", true, "memory")
                    } else ToolExecutionInfo("saveMemory", "Memory", "No fact provided to remember", false, "memory")
                }
            }
            "recallMemory" -> {
                val current = memoryRepository.getAllMemoriesList()
                if (current.isEmpty()) ToolExecutionInfo("recallMemory", "Memories", "I don't have any saved memories yet.", true, "memory")
                else ToolExecutionInfo("recallMemory", "Memories (${current.size})", "Here is what I remember: ${current.joinToString("; ") { it.fact }}", true, "memory")
            }
            "makePhoneCall" -> actionController.makePhoneCall(
                toolCall.args["phoneNumber"]?.toString() ?: "",
                toolCall.args["contactName"]?.toString()
            )
            "composeSms" -> actionController.composeSms(toolCall.args["phoneNumber"]?.toString(), toolCall.args["message"]?.toString() ?: "")
            "createReminder" -> actionController.createReminder(
                toolCall.args["title"]?.toString() ?: "Reminder",
                toolCall.args["time"]?.toString() ?: "today"
            )
            "searchWeb" -> actionController.searchWeb(toolCall.args["query"]?.toString() ?: "")
            "getDeviceInfo" -> actionController.getDeviceInfo()
            else -> ToolExecutionInfo(toolCall.name, "Action", "Executed ${toolCall.name}", true)
        }
    }

    fun testApiKey(key: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = geminiClient.testConnection(key, _aiModel.value)
            res.fold({ onResult(true, it) }, { onResult(false, it.message ?: "Connection test failed") })
        }
    }

    fun saveApiKey(key: String) {
        secureStorage.saveApiKey(key)
        haptic.actionSuccess()
    }

    fun removeApiKey() {
        secureStorage.removeApiKey()
        haptic.tap()
    }

    fun updateSpeechSettings(rate: Float, pitch: Float) {
        _speechRate.value = rate
        _speechPitch.value = pitch
        prefsManager.setSpeechRate(rate)
        prefsManager.setSpeechPitch(pitch)
        ttsManager.updateSettings(rate, pitch)
    }

    fun updateLanguageMode(mode: LanguageMode) = prefsManager.setLanguageMode(mode)
    fun updateAiModel(model: String) {
        _aiModel.value = model
        prefsManager.setAiModel(model)
    }
    fun updateOrbIntensity(intensity: Float) {
        _orbIntensity.value = intensity
        prefsManager.setOrbIntensity(intensity)
    }
    fun updateDarkTheme(dark: Boolean) = prefsManager.setDarkTheme(dark)
    fun updatePrivacyMode(enabled: Boolean) {
        prefsManager.setPrivacyModeEnabled(enabled)
        haptic.tap()
    }

    fun toggleCharacterViewMode() {
        val next = if (_characterViewMode.value == CharacterViewMode.CHARACTER) CharacterViewMode.ORB else CharacterViewMode.CHARACTER
        _characterViewMode.value = next
        prefsManager.setCharacterViewMode(next.name)
        characterStateController.setViewMode(next)
        haptic.tap()
    }
    fun updateCharacterEnabled(enabled: Boolean) {
        prefsManager.setCharacterEnabled(enabled)
        haptic.tap()
    }
    fun updateCharacterPerformanceMode(mode: CharacterPerformanceMode) {
        _characterPerformanceMode.value = mode
        prefsManager.setCharacterPerformance(mode.name)
        characterStateController.setPerformanceMode(mode)
        haptic.tap()
    }
    fun updateWakeWord(enabled: Boolean) {
        prefsManager.setWakeWordEnabled(enabled)
        haptic.tap()
        if (!enabled) {
            speechManager.stopListening()
            if (_orbState.value == OrbState.LISTENING) _orbState.value = OrbState.IDLE
        }
    }

    fun completeOnboarding() {
        prefsManager.setOnboardingDone(true)
        haptic.actionSuccess()
    }

    fun saveUserMemory(fact: String, category: String = "general") {
        viewModelScope.launch {
            memoryRepository.saveMemory(fact, category)
            haptic.actionSuccess()
        }
    }
    fun updateUserMemory(id: Long, fact: String, category: String = "general") {
        viewModelScope.launch {
            memoryRepository.updateMemory(id, fact, category)
            haptic.tap()
        }
    }
    fun deleteUserMemory(id: Long) {
        viewModelScope.launch {
            memoryRepository.deleteMemory(id)
            haptic.tap()
        }
    }
    fun clearAllUserMemories() {
        viewModelScope.launch {
            memoryRepository.clearAllMemories()
            haptic.actionSuccess()
        }
    }
    fun deleteConversation(id: Long) {
        viewModelScope.launch { repository.deleteById(id) }
    }
    fun clearAllConversations() {
        viewModelScope.launch { repository.clearHistory() }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.stopListening()
        ttsManager.shutdown()
    }
}
