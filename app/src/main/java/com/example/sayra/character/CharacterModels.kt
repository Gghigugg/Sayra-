package com.example.sayra.character

enum class CharacterState {
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

enum class CharacterExpression {
    CALM,
    HAPPY,
    CUTE_SMILE,
    CURIOUS,
    FOCUSED,
    THINKING,
    EXCITED,
    SURPRISED,
    CONCERNED,
    SERIOUS,
    CONFIDENT,
    LISTENING
}

enum class CharacterPerformanceMode {
    HIGH,    // Full particle field (36 particles), advanced liquid shaders, rich light refraction
    MEDIUM,  // Balanced particle field (18 particles), smooth animation
    LOW      // Minimal particles (6 particles), essential animation, optimized battery
}

enum class CharacterAttentionTarget {
    USER,       // Direct forward gaze at the user
    ORB,        // Looking down toward the liquid orb
    TRANSCRIPT, // Looking slightly downward toward user subtitles
    THINKING_UP,// Looking slightly upward in reflection
    SCANNING    // Scanning gaze for vision/analysis
}

enum class CharacterViewMode {
    CHARACTER, // Digital human character presence
    ORB        // Pure liquid AI orb mode
}

data class CharacterParticle(
    val id: Int,
    var x: Float, // 0.0 to 1.0 normalized
    var y: Float, // 0.0 to 1.0 normalized
    val size: Float,
    val speed: Float,
    val baseAlpha: Float,
    val colorIndex: Int, // 0: Violet, 1: Neon Cyan, 2: Magenta, 3: Lavender
    var phase: Float
)
