package com.example.sayra

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.sayra.character.CharacterAttentionTarget
import com.example.sayra.character.CharacterExpression
import com.example.sayra.character.CharacterPerformanceMode
import com.example.sayra.character.CharacterState
import com.example.sayra.character.CharacterStateController
import com.example.sayra.character.CharacterViewMode
import com.example.sayra.data.model.OrbState
import com.example.sayra.data.storage.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SayraCharacterSystemTest {

    private lateinit var context: Context
    private lateinit var prefsManager: PreferencesManager
    private lateinit var characterController: CharacterStateController
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        prefsManager = PreferencesManager(context)
        characterController = CharacterStateController(scope)
    }

    @Test
    fun testCharacterPreferencesDefaultAndToggle() {
        assertTrue(prefsManager.isCharacterEnabled())
        assertEquals("HIGH", prefsManager.getCharacterPerformance())
        assertEquals("CHARACTER", prefsManager.getCharacterViewMode())

        prefsManager.setCharacterEnabled(false)
        assertEquals(false, prefsManager.isCharacterEnabled())

        prefsManager.setCharacterViewMode(CharacterViewMode.ORB.name)
        assertEquals(CharacterViewMode.ORB.name, prefsManager.getCharacterViewMode())

        prefsManager.setCharacterPerformance(CharacterPerformanceMode.LOW.name)
        assertEquals(CharacterPerformanceMode.LOW.name, prefsManager.getCharacterPerformance())
    }

    @Test
    fun testCharacterStateTransitions() {
        // IDLE
        characterController.updateFromOrbState(OrbState.IDLE)
        assertEquals(CharacterState.IDLE, characterController.characterState.value)
        assertEquals(CharacterExpression.CALM, characterController.expression.value)
        assertEquals(CharacterAttentionTarget.USER, characterController.attentionTarget.value)

        // LISTENING
        characterController.updateFromOrbState(OrbState.LISTENING)
        assertEquals(CharacterState.LISTENING, characterController.characterState.value)
        assertEquals(CharacterExpression.LISTENING, characterController.expression.value)

        // THINKING
        characterController.updateFromOrbState(OrbState.THINKING)
        assertEquals(CharacterState.THINKING, characterController.characterState.value)
        assertEquals(CharacterExpression.THINKING, characterController.expression.value)
        assertEquals(CharacterAttentionTarget.THINKING_UP, characterController.attentionTarget.value)

        // SPEAKING with positive prompt
        characterController.updateFromOrbState(OrbState.SPEAKING, "Great job! It was a success.")
        assertEquals(CharacterState.SPEAKING, characterController.characterState.value)
        assertEquals(CharacterExpression.HAPPY, characterController.expression.value)

        // SPEAKING with greeting in Hindi
        characterController.updateFromOrbState(OrbState.SPEAKING, "नमस्ते! मैं SAYRA हूँ।")
        assertEquals(CharacterExpression.CUTE_SMILE, characterController.expression.value)

        // SPEAKING with joke query in Hinglish
        characterController.updateFromOrbState(OrbState.SPEAKING, "Here is a funny joke for you!")
        assertEquals(CharacterExpression.HAPPY, characterController.expression.value)

        // VISION
        characterController.updateFromOrbState(OrbState.VISION)
        assertEquals(CharacterState.VISION, characterController.characterState.value)
        assertEquals(CharacterExpression.FOCUSED, characterController.expression.value)
        assertEquals(CharacterAttentionTarget.SCANNING, characterController.attentionTarget.value)

        // AGENT
        characterController.updateFromOrbState(OrbState.AGENT)
        assertEquals(CharacterState.AGENT, characterController.characterState.value)
        assertEquals(CharacterExpression.FOCUSED, characterController.expression.value)

        // IMAGE_GENERATION
        characterController.updateFromOrbState(OrbState.IMAGE_GENERATION)
        assertEquals(CharacterState.IMAGE_GENERATION, characterController.characterState.value)
        assertEquals(CharacterExpression.EXCITED, characterController.expression.value)

        // ACTION_SUCCESS
        characterController.updateFromOrbState(OrbState.ACTION_SUCCESS)
        assertEquals(CharacterState.ACTION_SUCCESS, characterController.characterState.value)
        assertEquals(CharacterExpression.CONFIDENT, characterController.expression.value)

        // ERROR
        characterController.updateFromOrbState(OrbState.ERROR)
        assertEquals(CharacterState.ERROR, characterController.characterState.value)
        assertEquals(CharacterExpression.CONCERNED, characterController.expression.value)
    }

    @Test
    fun testViewModeToggle() {
        assertEquals(CharacterViewMode.CHARACTER, characterController.viewMode.value)
        characterController.toggleViewMode()
        assertEquals(CharacterViewMode.ORB, characterController.viewMode.value)
        characterController.toggleViewMode()
        assertEquals(CharacterViewMode.CHARACTER, characterController.viewMode.value)
    }
}
