package com.offx.sayra.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class GeminiClient(private val http: OkHttpClient = OkHttpClient()) {
    suspend fun generate(apiKey: String, model: String, prompt: String): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext Result.failure(IllegalStateException("Gemini API key is missing"))
        try {
            val body = JSONObject().put("contents", JSONArray().put(JSONObject().put("parts", JSONArray().put(JSONObject().put("text", prompt))))).toString()
            val request = Request.Builder().url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey").post(body.toRequestBody()).build()
            http.newCall(request).execute().use { r ->
                val raw = r.body?.string().orEmpty()
                if (!r.isSuccessful) return@withContext Result.failure(IllegalStateException("Gemini HTTP ${r.code}"))
                val text = JSONObject(raw).optJSONArray("candidates")?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text").orEmpty()
                if (text.isBlank()) Result.failure(IllegalStateException("Gemini returned no text")) else Result.success(text)
            }
        } catch (e: Exception) { Result.failure(e) }
    }
}
