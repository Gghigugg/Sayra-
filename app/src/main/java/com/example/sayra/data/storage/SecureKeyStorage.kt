package com.example.sayra.data.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.example.BuildConfig
import java.nio.charset.StandardCharsets

class SecureKeyStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sayra_secure_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_GEMINI_API_KEY = "user_gemini_api_key"
        private const val KEY_CUSTOM_KEY_SAVED = "is_custom_key_saved"
    }

    /**
     * Obtains the active Gemini API key.
     * Prioritizes the user-provided BYOK key, then falls back to injected BuildConfig key.
     */
    fun getApiKey(): String {
        val storedObfuscated = prefs.getString(KEY_GEMINI_API_KEY, null)
        if (!storedObfuscated.isNullOrEmpty()) {
            try {
                val decoded = Base64.decode(storedObfuscated, Base64.DEFAULT)
                return String(decoded, StandardCharsets.UTF_8).trim()
            } catch (_: Exception) {
                // Return empty if corrupted
            }
        }

        // Fallback to BuildConfig if provided and not placeholder
        val buildKey = BuildConfig.GEMINI_API_KEY
        if (buildKey.isNotEmpty() && !buildKey.contains("MY_GEMINI_API_KEY")) {
            return buildKey.trim()
        }

        return ""
    }

    fun saveApiKey(apiKey: String) {
        val cleanKey = apiKey.trim()
        val encoded = Base64.encodeToString(cleanKey.toByteArray(StandardCharsets.UTF_8), Base64.DEFAULT)
        prefs.edit()
            .putString(KEY_GEMINI_API_KEY, encoded)
            .putBoolean(KEY_CUSTOM_KEY_SAVED, true)
            .apply()
    }

    fun removeApiKey() {
        prefs.edit()
            .remove(KEY_GEMINI_API_KEY)
            .putBoolean(KEY_CUSTOM_KEY_SAVED, false)
            .apply()
    }

    fun isCustomKeySaved(): Boolean {
        return prefs.getBoolean(KEY_CUSTOM_KEY_SAVED, false) && prefs.contains(KEY_GEMINI_API_KEY)
    }

    fun hasValidApiKey(): Boolean {
        return getApiKey().isNotEmpty()
    }

    /**
     * Returns a safely masked preview e.g. "AIzaSy...9xK2" without exposing the full key
     */
    fun getMaskedApiKey(): String {
        val key = getApiKey()
        if (key.isEmpty()) return "No key configured"
        if (key.length <= 8) return "••••••••"
        return "${key.take(6)}••••••••${key.takeLast(4)}"
    }
}
