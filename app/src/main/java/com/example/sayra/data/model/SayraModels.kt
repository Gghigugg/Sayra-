package com.example.sayra.data.model

enum class OrbState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    ACTION_SUCCESS,
    ERROR,
    VISION,
    AGENT,
    IMAGE_GENERATION
}

enum class LanguageMode(val displayName: String, val localeCode: String) {
    AUTO("Auto (Auto-Detect)", "auto"),
    ENGLISH("English", "en-US"),
    HINDI("Hindi (हिन्दी)", "hi-IN"),
    HINGLISH("Hinglish", "hi-IN")
}

data class ActionConfirmation(
    val title: String,
    val description: String,
    val confirmLabel: String,
    val onConfirm: () -> Unit,
    val onCancel: () -> Unit
)

data class ToolExecutionInfo(
    val toolName: String,
    val displayName: String,
    val details: String,
    val isSuccess: Boolean = true,
    val iconName: String = "check"
)
