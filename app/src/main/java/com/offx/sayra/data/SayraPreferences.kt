package com.offx.sayra.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SayraPreferences(context: Context) {
    private val prefs = EncryptedSharedPreferences.create(
        "sayra_secure",
        MasterKey.DEFAULT_MASTER_KEY_ALIAS,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    var apiKey: String get() = prefs.getString("api_key", "") ?: ""; set(v) = prefs.edit().putString("api_key", v).apply()
    var wakeWordEnabled: Boolean get() = prefs.getBoolean("wake_word", true); set(v) = prefs.edit().putBoolean("wake_word", v).apply()
    var language: String get() = prefs.getString("language", "hi-IN") ?: "hi-IN"; set(v) = prefs.edit().putString("language", v).apply()
    var model: String get() = prefs.getString("model", "gemini-2.5-flash") ?: "gemini-2.5-flash"; set(v) = prefs.edit().putString("model", v).apply()
}
