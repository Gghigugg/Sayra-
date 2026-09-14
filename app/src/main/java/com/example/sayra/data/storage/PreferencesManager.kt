package com.example.sayra.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.sayra.data.model.LanguageMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ModelOption(
    val id: String,
    val displayName: String,
    val description: String
)

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sayra_settings_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
        private const val KEY_SPEECH_RATE = "speech_rate"
        private const val KEY_SPEECH_PITCH = "speech_pitch"
        private const val KEY_LANGUAGE = "language_mode"
        private const val KEY_MODEL = "ai_model"
        private const val KEY_ORB_INTENSITY = "orb_intensity"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_THEME_MODE = "theme_mode" // "DARK", "LIGHT", "SYSTEM"
        private const val KEY_CONFIRM_SENSITIVE = "confirm_sensitive"
        private const val KEY_PRIVACY_MODE = "privacy_mode"
        private const val KEY_WAKE_WORD = "wake_word_enabled"
        private const val KEY_CHARACTER_ENABLED = "character_enabled"
        private const val KEY_CHARACTER_PERFORMANCE = "character_performance"
        private const val KEY_CHARACTER_VIEW_MODE = "character_view_mode"

        val AVAILABLE_MODELS = listOf(
            ModelOption("gemini-2.5-flash", "Gemini 2.5 Flash (Recommended)", "Fastest response, multimodal, tools & reasoning"),
            ModelOption("gemini-2.5-pro", "Gemini 2.5 Pro", "Advanced multi-step reasoning & complex tasks"),
            ModelOption("gemini-3.5-flash", "Gemini 3.5 Flash (Preview)", "High-speed next-gen preview")
        )
    }

    private val _onboardingDoneFlow = MutableStateFlow(isOnboardingDone())
    val onboardingDoneFlow: StateFlow<Boolean> = _onboardingDoneFlow.asStateFlow()

    private val _languageModeFlow = MutableStateFlow(getLanguageMode())
    val languageModeFlow: StateFlow<LanguageMode> = _languageModeFlow.asStateFlow()

    private val _darkThemeFlow = MutableStateFlow(isDarkTheme())
    val darkThemeFlow: StateFlow<Boolean> = _darkThemeFlow.asStateFlow()

    private val _privacyModeFlow = MutableStateFlow(isPrivacyModeEnabled())
    val privacyModeFlow: StateFlow<Boolean> = _privacyModeFlow.asStateFlow()

    private val _wakeWordFlow = MutableStateFlow(isWakeWordEnabled())
    val wakeWordFlow: StateFlow<Boolean> = _wakeWordFlow.asStateFlow()

    private val _modelFlow = MutableStateFlow(getAiModel())
    val modelFlow: StateFlow<String> = _modelFlow.asStateFlow()

    fun isOnboardingDone(): Boolean = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
    fun setOnboardingDone(done: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, done).apply()
        _onboardingDoneFlow.value = done
    }

    fun getSpeechRate(): Float = prefs.getFloat(KEY_SPEECH_RATE, 1.0f)
    fun setSpeechRate(rate: Float) {
        prefs.edit().putFloat(KEY_SPEECH_RATE, rate.coerceIn(0.5f, 2.0f)).apply()
    }

    fun getSpeechPitch(): Float = prefs.getFloat(KEY_SPEECH_PITCH, 1.05f)
    fun setSpeechPitch(pitch: Float) {
        prefs.edit().putFloat(KEY_SPEECH_PITCH, pitch.coerceIn(0.5f, 2.0f)).apply()
    }

    fun getLanguageMode(): LanguageMode {
        val name = prefs.getString(KEY_LANGUAGE, LanguageMode.AUTO.name) ?: LanguageMode.AUTO.name
        return try {
            LanguageMode.valueOf(name)
        } catch (_: Exception) {
            LanguageMode.AUTO
        }
    }

    fun setLanguageMode(mode: LanguageMode) {
        prefs.edit().putString(KEY_LANGUAGE, mode.name).apply()
        _languageModeFlow.value = mode
    }

    fun getAiModel(): String = prefs.getString(KEY_MODEL, "gemini-2.5-flash") ?: "gemini-2.5-flash"
    fun setAiModel(model: String) {
        prefs.edit().putString(KEY_MODEL, model).apply()
        _modelFlow.value = model
    }

    fun getOrbIntensity(): Float = prefs.getFloat(KEY_ORB_INTENSITY, 1.0f)
    fun setOrbIntensity(intensity: Float) {
        prefs.edit().putFloat(KEY_ORB_INTENSITY, intensity.coerceIn(0.5f, 2.0f)).apply()
    }

    fun isDarkTheme(): Boolean = prefs.getBoolean(KEY_DARK_THEME, true)
    fun setDarkTheme(dark: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_THEME, dark).apply()
        _darkThemeFlow.value = dark
    }

    fun getThemeMode(): String = prefs.getString(KEY_THEME_MODE, "DARK") ?: "DARK"
    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        if (mode == "DARK") setDarkTheme(true)
        else if (mode == "LIGHT") setDarkTheme(false)
    }

    fun isConfirmSensitiveEnabled(): Boolean = prefs.getBoolean(KEY_CONFIRM_SENSITIVE, true)
    fun setConfirmSensitiveEnabled(enable: Boolean) {
        prefs.edit().putBoolean(KEY_CONFIRM_SENSITIVE, enable).apply()
    }

    fun isPrivacyModeEnabled(): Boolean = prefs.getBoolean(KEY_PRIVACY_MODE, false)
    fun setPrivacyModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PRIVACY_MODE, enabled).apply()
        _privacyModeFlow.value = enabled
    }

    fun isWakeWordEnabled(): Boolean = prefs.getBoolean(KEY_WAKE_WORD, false)
    fun setWakeWordEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WAKE_WORD, enabled).apply()
        _wakeWordFlow.value = enabled
    }

    private val _characterEnabledFlow = MutableStateFlow(isCharacterEnabled())
    val characterEnabledFlow: StateFlow<Boolean> = _characterEnabledFlow.asStateFlow()

    fun isCharacterEnabled(): Boolean = prefs.getBoolean(KEY_CHARACTER_ENABLED, true)
    fun setCharacterEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CHARACTER_ENABLED, enabled).apply()
        _characterEnabledFlow.value = enabled
    }

    fun getCharacterPerformance(): String = prefs.getString(KEY_CHARACTER_PERFORMANCE, "HIGH") ?: "HIGH"
    fun setCharacterPerformance(perf: String) {
        prefs.edit().putString(KEY_CHARACTER_PERFORMANCE, perf).apply()
    }

    fun getCharacterViewMode(): String = prefs.getString(KEY_CHARACTER_VIEW_MODE, "CHARACTER") ?: "CHARACTER"
    fun setCharacterViewMode(mode: String) {
        prefs.edit().putString(KEY_CHARACTER_VIEW_MODE, mode).apply()
    }
}
