package com.offx.sayra.data

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class GeminiClient(private val apiKey: String) {
    private val client = OkHttpClient()
    suspend fun generate(prompt: String): String = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val body = JSONObject().apply {
            put("contents", org.json.JSONArray().put(JSONObject().apply {
                put("parts", org.json.JSONArray().put(JSONObject().put("text", "You are SAYRA, a concise helpful Android assistant. Reply in the user's language. User: $prompt")))
            }))
        }.toString()
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
        runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext "Gemini error ${response.code}. Check your API key and model access."
                val json = JSONObject(response.body?.string().orEmpty())
                json.optJSONArray("candidates")?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text")
                    ?: "I couldn't generate a response."
            }
        }.getOrElse { "Network error: ${it.message ?: "unknown error"}" }
    }
}
