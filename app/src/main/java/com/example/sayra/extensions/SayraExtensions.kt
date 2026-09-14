package com.example.sayra.extensions

import android.graphics.Bitmap

/**
 * Architectural extension contracts for future V2/V3 features.
 * Designed to cleanly plug into SayraViewModel and GeminiClient without structural refactoring.
 */

// 1. Gemini Vision & Visual Intelligence
interface VisionCapability {
    suspend fun analyzeImage(bitmap: Bitmap, prompt: String): String
    suspend fun analyzeScreen(screenshot: Bitmap, userQuery: String): String
}

// 2. AI Image Generation & Editing
interface ImageGenerationCapability {
    suspend fun generateImage(prompt: String, aspectRatio: String = "1:1"): Result<Bitmap>
    suspend fun editImage(baseImage: Bitmap, editPrompt: String): Result<Bitmap>
}

// 3. Agent Mode & Smart Routines
data class ScheduledRoutine(
    val id: String,
    val name: String,
    val cronTime: String,
    val actionSteps: List<String>,
    val isEnabled: Boolean
)

interface AgentRoutinesCapability {
    suspend fun executeRoutine(routine: ScheduledRoutine)
    suspend fun listRoutines(): List<ScheduledRoutine>
}

// 4. Notification Intelligence
data class SayraNotificationSummary(
    val packageName: String,
    val sender: String,
    val summary: String,
    val priority: Int
)

interface NotificationIntelligenceCapability {
    suspend fun summarizeIncomingNotifications(): List<SayraNotificationSummary>
}

// 5. Wake Word Engine Placeholder
interface WakeWordEngine {
    fun startListening(onWakeWordDetected: () -> Unit)
    fun stopListening()
    val isAvailable: Boolean
}

// 6. Cloud Sync Capability
interface CloudSyncCapability {
    suspend fun syncConversations(): Result<Unit>
    suspend fun syncMemories(): Result<Unit>
}
