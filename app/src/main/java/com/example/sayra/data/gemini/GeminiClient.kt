package com.example.sayra.data.gemini

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class GeminiResult {
    data class Success(
        val text: String,
        val toolCall: GeminiToolCall? = null
    ) : GeminiResult()

    data class Error(
        val message: String,
        val isAuthError: Boolean = false
    ) : GeminiResult()
}

data class GeminiToolCall(
    val name: String,
    val args: Map<String, Any?>
)

data class ChatMessage(
    val role: String, // "user" or "model"
    val text: String
)

class GeminiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Sends a ping to verify if the API key is active and functional
     */
    suspend fun testConnection(apiKey: String, model: String = "gemini-3.5-flash"): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(Exception("API key is empty"))
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=${apiKey.trim()}"

        val testPayload = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", "Respond with just the word: Connected"))
                    })
                })
            })
        }

        try {
            val request = Request.Builder()
                .url(url)
                .post(testPayload.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                Result.success("Connection successful! Gemini is ready.")
            } else {
                val errorMsg = try {
                    val errorJson = JSONObject(responseBody).getJSONObject("error")
                    errorJson.getString("message")
                } catch (_: Exception) {
                    "HTTP ${response.code}: $responseBody"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    /**
     * Sends prompt to Gemini with full Android tool declarations
     */
    suspend fun generateAssistantResponse(
        apiKey: String,
        userPrompt: String,
        conversationHistory: List<ChatMessage> = emptyList(),
        model: String = "gemini-2.5-flash",
        knownMemories: List<String> = emptyList()
    ): GeminiResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext GeminiResult.Error(
                message = "Connect your Gemini API key to start using SAYRA.",
                isAuthError = true
            )
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=${apiKey.trim()}"

        try {
            val root = JSONObject()

            // System Instruction
            root.put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", buildSystemInstruction(knownMemories)))
                })
            })

            // Conversation Contents
            val contentsArray = JSONArray()
            val recentHistory = conversationHistory.takeLast(6)
            for (msg in recentHistory) {
                contentsArray.put(JSONObject().apply {
                    put("role", msg.role)
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", msg.text))
                    })
                })
            }
            // Add current user prompt
            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", userPrompt))
                })
            })
            root.put("contents", contentsArray)

            // Function Calling Tools
            root.put("tools", JSONArray().apply {
                put(buildToolsDeclaration())
            })

            // Generation Config
            root.put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("maxOutputTokens", 600)
            })

            val request = Request.Builder()
                .url(url)
                .post(root.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                val isAuth = response.code == 400 || response.code == 403 || response.code == 401
                val isModelNotFound = response.code == 404
                val rawErrMsg = try {
                    val err = JSONObject(responseBody).getJSONObject("error")
                    err.getString("message")
                } catch (_: Exception) {
                    "Request failed with code ${response.code}"
                }

                val friendlyMsg = when {
                    isAuth -> "Invalid or expired Gemini API key. Please check or re-enter your key in Settings."
                    isModelNotFound -> "Model '$model' is unavailable. Please select Gemini 2.5 Flash in Settings."
                    else -> rawErrMsg
                }
                return@withContext GeminiResult.Error(friendlyMsg, isAuth)
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            if (parts == null || parts.length() == 0) {
                return@withContext GeminiResult.Success("I didn't quite catch that. Could you repeat?")
            }

            var textResponse = ""
            var toolCall: GeminiToolCall? = null

            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                if (part.has("text")) {
                    textResponse += part.getString("text")
                }
                if (part.has("functionCall")) {
                    val funcObj = part.getJSONObject("functionCall")
                    val name = funcObj.getString("name")
                    val argsObj = funcObj.optJSONObject("args")
                    val argsMap = mutableMapOf<String, Any?>()
                    if (argsObj != null) {
                        val keys = argsObj.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            argsMap[key] = argsObj.get(key)
                        }
                    }
                    toolCall = GeminiToolCall(name, argsMap)
                }
            }

            GeminiResult.Success(
                text = textResponse.trim(),
                toolCall = toolCall
            )

        } catch (e: java.net.UnknownHostException) {
            GeminiResult.Error("You're offline. Gemini features are unavailable right now. Please check your internet connection.")
        } catch (e: java.io.IOException) {
            GeminiResult.Error("Connection timeout. Please check your internet connection.")
        } catch (e: Exception) {
            GeminiResult.Error("Network error: ${e.message ?: "Unable to contact Gemini."}")
        }
    }

    private fun buildSystemInstruction(knownMemories: List<String> = emptyList()): String {
        val memoryContext = if (knownMemories.isNotEmpty()) {
            "\n\nRemembered facts from the user:\n" + knownMemories.joinToString("\n") { "- $it" }
        } else ""

        return """
            You are SAYRA AI, a futuristic, elegant, and ultra-responsive personal AI voice assistant on Android.
            Your tagline is: 'Your AI. Your Phone. Your Control.'
            Core concept: 'Gemini is the brain. SAYRA is the assistant. Android APIs are the hands.'
            
            Key Rules:
            1. Multilingual: Seamlessly understand and respond to English, Hindi (हिन्दी), and Hinglish (e.g. 'YouTube खोलो', 'Flashlight on kar do', 'Battery check karo', 'Open Wi-Fi settings', 'Screen brightness 50% kar do').
            2. Android Actions: When the user asks to open an app, adjust volume, change brightness, open a URL, toggle flashlight, check battery, make a call, compose SMS, open settings, or set a reminder, ALWAYS invoke the corresponding tool.
            3. Controlled Memory: When the user explicitly asks you to remember or store something (e.g. 'Remember that I prefer Hindi', 'Save note that my dog's name is Bruno'), invoke 'saveMemory'. When asked what you remember, invoke 'recallMemory'. Never silently record personal facts without user request.
            4. Voice-first replies: When you perform an action or answer a question, keep your spoken text concise, friendly, and natural. NEVER use markdown asterisks (*), markdown bullet points, or complex formatting. Speak like a real human assistant.
            5. If user speaks in Hindi or Hinglish, answer politely in natural Hindi or Hinglish.$memoryContext
        """.trimIndent()
    }

    private fun buildToolsDeclaration(): JSONObject {
        val functionDeclarations = JSONArray()

        // 1. openApp
        functionDeclarations.put(JSONObject().apply {
            put("name", "openApp")
            put("description", "Opens an installed Android app like YouTube, Instagram, WhatsApp, Spotify, Chrome, Camera, Settings, etc.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("appName", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Name of the app to launch, e.g. YouTube, Instagram, WhatsApp, Spotify, Camera, Settings")
                    })
                })
                put("required", JSONArray().put("appName"))
            })
        })

        // 2. openSettings
        functionDeclarations.put(JSONObject().apply {
            put("name", "openSettings")
            put("description", "Opens Android system settings pages like Wi-Fi, Bluetooth, Display, Sound, Battery, Apps, Notifications")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("settingType", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "The settings page to open: 'wifi', 'bluetooth', 'display', 'sound', 'battery', 'apps'")
                    })
                })
                put("required", JSONArray().put("settingType"))
            })
        })

        // 3. getBatteryStatus
        functionDeclarations.put(JSONObject().apply {
            put("name", "getBatteryStatus")
            put("description", "Reads the current Android battery percentage and charging state")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject())
            })
        })

        // 4. controlFlashlight
        functionDeclarations.put(JSONObject().apply {
            put("name", "controlFlashlight")
            put("description", "Turns the phone flashlight / torch on or off")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("turnOn", JSONObject().apply {
                        put("type", "BOOLEAN")
                        put("description", "True to turn flashlight on, false to turn it off")
                    })
                })
                put("required", JSONArray().put("turnOn"))
            })
        })

        // 5. controlVolume
        functionDeclarations.put(JSONObject().apply {
            put("name", "controlVolume")
            put("description", "Adjusts the media volume up, down, mute, or sets percentage")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("direction", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "'up', 'down', 'mute', or 'set'")
                    })
                    put("level", JSONObject().apply {
                        put("type", "INTEGER")
                        put("description", "Optional percentage level between 0 and 100")
                    })
                })
                put("required", JSONArray().put("direction"))
            })
        })

        // 6. makePhoneCall
        functionDeclarations.put(JSONObject().apply {
            put("name", "makePhoneCall")
            put("description", "Opens the phone dialer or initiates a call to a contact or phone number")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("phoneNumber", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Phone number to call")
                    })
                    put("contactName", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Contact name if known")
                    })
                })
                put("required", JSONArray().put("phoneNumber"))
            })
        })

        // 7. composeSms
        functionDeclarations.put(JSONObject().apply {
            put("name", "composeSms")
            put("description", "Prepares an SMS message to a phone number")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("phoneNumber", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Optional recipient phone number")
                    })
                    put("message", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Message text content to send")
                    })
                })
                put("required", JSONArray().put("message"))
            })
        })

        // 8. createReminder
        functionDeclarations.put(JSONObject().apply {
            put("name", "createReminder")
            put("description", "Creates an alarm or reminder for a given title and time")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("title", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Title of the reminder or alarm")
                    })
                    put("time", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Time description e.g. 8:00 AM")
                    })
                })
                put("required", JSONArray().put("title").put("time"))
            })
        })

        // 9. searchWeb
        functionDeclarations.put(JSONObject().apply {
            put("name", "searchWeb")
            put("description", "Performs a web search for information")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("query", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "The search query")
                    })
                })
                put("required", JSONArray().put("query"))
            })
        })

        // 10. getDeviceInfo
        functionDeclarations.put(JSONObject().apply {
            put("name", "getDeviceInfo")
            put("description", "Returns details about the device hardware, Android version, and network status")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject())
            })
        })

        // 11. controlBrightness
        functionDeclarations.put(JSONObject().apply {
            put("name", "controlBrightness")
            put("description", "Adjusts device screen brightness level from 0 to 100 percent")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("levelPercent", JSONObject().apply {
                        put("type", "INTEGER")
                        put("description", "Target screen brightness percentage between 0 and 100")
                    })
                })
                put("required", JSONArray().put("levelPercent"))
            })
        })

        // 12. openUrl
        functionDeclarations.put(JSONObject().apply {
            put("name", "openUrl")
            put("description", "Safely opens a web link / URL in the device browser")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("url", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "The website URL to open, e.g. https://google.com or wikipedia.org")
                    })
                })
                put("required", JSONArray().put("url"))
            })
        })

        // 13. saveMemory
        functionDeclarations.put(JSONObject().apply {
            put("name", "saveMemory")
            put("description", "Stores a user-requested fact or preference into local controlled memory")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("fact", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "The exact fact or preference the user asked to remember")
                    })
                    put("category", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Category: 'preference', 'personal', 'work', or 'general'")
                    })
                })
                put("required", JSONArray().put("fact"))
            })
        })

        // 14. recallMemory
        functionDeclarations.put(JSONObject().apply {
            put("name", "recallMemory")
            put("description", "Recalls user's saved memories and preferences stored in SAYRA")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject())
            })
        })

        return JSONObject().put("functionDeclarations", functionDeclarations)
    }
}
