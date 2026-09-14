package com.example.sayra.character

import com.example.sayra.data.model.OrbState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CharacterStateController(
    private val scope: CoroutineScope
) {
    private val _characterState = MutableStateFlow(CharacterState.IDLE)
    val characterState: StateFlow<CharacterState> = _characterState.asStateFlow()

    private val _expression = MutableStateFlow(CharacterExpression.CALM)
    val expression: StateFlow<CharacterExpression> = _expression.asStateFlow()

    private val _attentionTarget = MutableStateFlow(CharacterAttentionTarget.USER)
    val attentionTarget: StateFlow<CharacterAttentionTarget> = _attentionTarget.asStateFlow()

    private val _viewMode = MutableStateFlow(CharacterViewMode.CHARACTER)
    val viewMode: StateFlow<CharacterViewMode> = _viewMode.asStateFlow()

    private val _performanceMode = MutableStateFlow(CharacterPerformanceMode.HIGH)
    val performanceMode: StateFlow<CharacterPerformanceMode> = _performanceMode.asStateFlow()

    fun updateFromOrbState(orbState: OrbState, promptContext: String = "") {
        when (orbState) {
            OrbState.IDLE -> {
                _characterState.value = CharacterState.IDLE
                _expression.value = determineIdleExpression(promptContext)
                _attentionTarget.value = CharacterAttentionTarget.USER
            }
            OrbState.LISTENING -> {
                _characterState.value = CharacterState.LISTENING
                _expression.value = CharacterExpression.LISTENING
                _attentionTarget.value = CharacterAttentionTarget.USER
            }
            OrbState.THINKING -> {
                _characterState.value = CharacterState.THINKING
                _expression.value = CharacterExpression.THINKING
                _attentionTarget.value = CharacterAttentionTarget.THINKING_UP
            }
            OrbState.SPEAKING -> {
                _characterState.value = CharacterState.SPEAKING
                // Context-aware expression refinement based on response & user prompt
                _expression.value = determineExpressionFromText(promptContext)
                _attentionTarget.value = CharacterAttentionTarget.USER
            }
            OrbState.ACTION_SUCCESS -> {
                _characterState.value = CharacterState.ACTION_SUCCESS
                _expression.value = CharacterExpression.CONFIDENT
                _attentionTarget.value = CharacterAttentionTarget.USER
            }
            OrbState.ERROR -> {
                _characterState.value = CharacterState.ERROR
                _expression.value = CharacterExpression.CONCERNED
                _attentionTarget.value = CharacterAttentionTarget.USER
            }
            OrbState.VISION -> {
                _characterState.value = CharacterState.VISION
                _expression.value = CharacterExpression.FOCUSED
                _attentionTarget.value = CharacterAttentionTarget.SCANNING
            }
            OrbState.AGENT -> {
                _characterState.value = CharacterState.AGENT
                _expression.value = CharacterExpression.FOCUSED
                _attentionTarget.value = CharacterAttentionTarget.USER
            }
            OrbState.IMAGE_GENERATION -> {
                _characterState.value = CharacterState.IMAGE_GENERATION
                _expression.value = CharacterExpression.EXCITED
                _attentionTarget.value = CharacterAttentionTarget.TRANSCRIPT
            }
        }
    }

    private fun determineIdleExpression(context: String): CharacterExpression {
        if (context.isBlank()) return CharacterExpression.CALM
        val lower = context.lowercase()
        return when {
            lower.contains("hi") || lower.contains("hello") || lower.contains("नमस्ते") || lower.contains("hey") -> CharacterExpression.CUTE_SMILE
            lower.contains("thanks") || lower.contains("धन्यवाद") || lower.contains("shukriya") -> CharacterExpression.HAPPY
            else -> CharacterExpression.CALM
        }
    }

    /**
     * Context-aware classification supporting English, Hindi, and Hinglish.
     */
    fun determineExpressionFromText(text: String): CharacterExpression {
        val lower = text.lowercase()
        return when {
            // Greetings / Warmth
            lower.contains("hi sayra") || lower.contains("hello sayra") || lower.contains("नमस्ते") ||
                    lower.contains("namaste") || lower.contains("hey sayra") || lower.contains("good morning") ||
                    lower.contains("good evening") -> CharacterExpression.CUTE_SMILE

            // Humor / Jokes / Playful
            lower.contains("joke") || lower.contains("chutkula") || lower.contains("चुटकुला") ||
                    lower.contains("हँसी") || lower.contains("funny") || lower.contains("laugh") ||
                    lower.contains("मज़ाक") || lower.contains("haha") -> CharacterExpression.HAPPY

            // Inquisitive / Capabilities
            lower.contains("क्या कर सकती हो") || lower.contains("what can you do") ||
                    lower.contains("who are you") || lower.contains("तुम कौन हो") ||
                    lower.contains("help me") || lower.contains("tell me about") -> CharacterExpression.CURIOUS

            // Creativity / Excitement / Image Generation
            lower.contains("create") || lower.contains("imagine") || lower.contains("idea") ||
                    lower.contains("generate") || lower.contains("picture") || lower.contains("photo") ||
                    lower.contains("art") || lower.contains("draw") || lower.contains("amazing") -> CharacterExpression.EXCITED

            // Astonishment / Surprise
            lower.contains("wow") || lower.contains("unbelievable") || lower.contains("really") ||
                    lower.contains("are you sure") || lower.contains("सच में") -> CharacterExpression.SURPRISED

            // Problem / Failure / Concern
            lower.contains("sorry") || lower.contains("error") || lower.contains("failed") ||
                    lower.contains("unable") || lower.contains("trouble") || lower.contains("गलती") ||
                    lower.contains("माफ़") -> CharacterExpression.CONCERNED

            // Analytical / Technical / Deep Calculation
            lower.contains("analyzing") || lower.contains("calculating") || lower.contains("detailed") ||
                    lower.contains("code") || lower.contains("system") || lower.contains("device") ||
                    lower.contains("battery") -> CharacterExpression.FOCUSED

            // Warnings / Security / Alerts
            lower.contains("warning") || lower.contains("alert") || lower.contains("important") ||
                    lower.contains("careful") || lower.contains("ध्यान दें") || lower.contains("critical") -> CharacterExpression.SERIOUS

            // Success / Accomplishment
            lower.contains("success") || lower.contains("done") || lower.contains("completed") ||
                    lower.contains("great") || lower.contains("हो गया") || lower.contains("perfect") -> CharacterExpression.HAPPY

            // Default confident adult composure
            else -> CharacterExpression.CONFIDENT
        }
    }

    fun setViewMode(mode: CharacterViewMode) {
        _viewMode.value = mode
    }

    fun toggleViewMode() {
        _viewMode.value = if (_viewMode.value == CharacterViewMode.CHARACTER) {
            CharacterViewMode.ORB
        } else {
            CharacterViewMode.CHARACTER
        }
    }

    fun setPerformanceMode(mode: CharacterPerformanceMode) {
        _performanceMode.value = mode
    }

    fun setExpression(exp: CharacterExpression) {
        _expression.value = exp
    }

    fun setAttentionTarget(target: CharacterAttentionTarget) {
        _attentionTarget.value = target
    }
}
