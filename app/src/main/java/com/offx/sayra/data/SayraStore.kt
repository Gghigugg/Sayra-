package com.offx.sayra.data

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class SayraStore(context: Context) {
    private val prefs = context.getSharedPreferences("sayra", Context.MODE_PRIVATE)
    private val key: SecretKeySpec by lazy {
        val digest = MessageDigest.getInstance("SHA-256").digest((context.packageName + ":SAYRA").toByteArray())
        SecretKeySpec(digest, "AES")
    }
    fun saveApiKey(value: String) {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding").apply { init(Cipher.ENCRYPT_MODE, key) }
        prefs.edit().putString("api_key", Base64.encodeToString(cipher.doFinal(value.toByteArray()), Base64.NO_WRAP)).apply()
    }
    fun apiKey(): String = runCatching {
        val raw = Base64.decode(prefs.getString("api_key", ""), Base64.NO_WRAP)
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding").apply { init(Cipher.DECRYPT_MODE, key) }
        String(cipher.doFinal(raw))
    }.getOrDefault("")
    fun wakeWordEnabled() = prefs.getBoolean("wake_word", true)
    fun setWakeWordEnabled(v: Boolean) = prefs.edit().putBoolean("wake_word", v).apply()
}
