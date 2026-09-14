package com.example.sayra.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sayra.character.SayraPresenceContainer
import com.example.sayra.data.model.OrbState
import com.example.sayra.ui.components.ActionConfirmationModal
import com.example.sayra.ui.components.DynamicTranscriptCard
import com.example.sayra.ui.components.FloatingGlassDock
import com.example.sayra.ui.orb.LiquidAiOrb
import com.example.sayra.ui.viewmodel.SayraViewModel
import com.example.ui.theme.CoralWarning
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GlassBorderDark
import com.example.ui.theme.GlassSurfaceDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RadiantMagenta
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.SoftPink
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SayraHomeScreen(
    viewModel: SayraViewModel,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenMemory: () -> Unit,
    onOpenFullScreen: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val orbState by viewModel.orbState.collectAsState()
    val audioAmplitude by viewModel.audioAmplitude.collectAsState()
    val userPrompt by viewModel.userPrompt.collectAsState()
    val assistantResponse by viewModel.assistantResponse.collectAsState()
    val toolExecution by viewModel.toolExecution.collectAsState()
    val isTextInputOpen by viewModel.isTextInputOpen.collectAsState()
    val textInputValue by viewModel.textInputValue.collectAsState()
    val actionConfirmation by viewModel.actionConfirmation.collectAsState()
    val orbIntensity by viewModel.orbIntensity.collectAsState()
    val isPrivacyMode by viewModel.isPrivacyMode.collectAsState()

    val characterState by viewModel.characterState.collectAsState()
    val characterExpression by viewModel.characterExpression.collectAsState()
    val characterAttentionTarget by viewModel.characterAttentionTarget.collectAsState()
    val isCharacterEnabled by viewModel.isCharacterEnabled.collectAsState()
    val characterPerformance by viewModel.characterPerformanceMode.collectAsState()
    val characterViewMode by viewModel.characterViewMode.collectAsState()

    val isListening = orbState == OrbState.LISTENING
    val hasApiKey = viewModel.secureStorage.hasValidApiKey()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ObsidianBlack,
                        Color(0xFF0F0B1E),
                        ObsidianBlack
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Header: SAYRA AI Brand & Live Status Pill & Privacy Mode Badge
            TopHeaderSection(orbState = orbState, isPrivacyMode = isPrivacyMode)

            // Optional notice if API key is not yet set
            if (!hasApiKey) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CoralWarning.copy(alpha = 0.15f))
                        .border(1.dp, CoralWarning.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .clickable { onOpenSettings() }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = CoralWarning,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Gemini API Key required. Tap to connect in Settings.",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 2. Center Stage: Digital Presence / Liquid AI Orb Fusion
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                SayraPresenceContainer(
                    orbState = orbState,
                    characterState = characterState,
                    expression = characterExpression,
                    audioAmplitude = audioAmplitude,
                    intensityMultiplier = orbIntensity,
                    performanceMode = characterPerformance,
                    attentionTarget = characterAttentionTarget,
                    isCharacterEnabled = isCharacterEnabled,
                    viewMode = characterViewMode,
                    onToggleViewMode = { viewModel.toggleCharacterViewMode() },
                    onOpenFullScreen = onOpenFullScreen,
                    onToggleListening = { viewModel.toggleListening() },
                    size = 280.dp
                )
            }

            // 3. Dynamic Subtitles / Transcript & Action Feedback
            DynamicTranscriptCard(
                userPrompt = userPrompt,
                assistantResponse = assistantResponse,
                toolExecution = toolExecution,
                orbState = orbState,
                onSuggestionClick = { prompt -> viewModel.sendTextMessage(prompt) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 4. Floating Minimal Glass Control Area at Bottom
        FloatingGlassDock(
            isListening = isListening,
            isTextInputOpen = isTextInputOpen,
            textInputValue = textInputValue,
            onTextInputChange = { viewModel.onTextInputChange(it) },
            onSendText = { viewModel.sendTextMessage(it) },
            onToggleListening = { viewModel.toggleListening() },
            onToggleTextInput = { viewModel.toggleTextInput() },
            onOpenHistory = onOpenHistory,
            onOpenSettings = onOpenSettings,
            onOpenMemory = onOpenMemory,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
        )

        // Action Confirmation Modal (for sensitive actions like calls)
        ActionConfirmationModal(confirmation = actionConfirmation)
    }
}

@Composable
private fun TopHeaderSection(
    orbState: OrbState,
    isPrivacyMode: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "SAYRA AI",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = "Your AI. Your Phone. Your Control.",
                color = SoftLavender.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Privacy Mode badge if active
            if (isPrivacyMode) {
                Row(
                    modifier = Modifier
                        .background(ElectricPurple.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Privacy Mode Active",
                        tint = NeonCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Private",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Status Pill
            Row(
                modifier = Modifier
                    .background(GlassSurfaceDark, RoundedCornerShape(20.dp))
                    .border(1.dp, GlassBorderDark, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val (statusDotColor, statusLabel) = when (orbState) {
                    OrbState.IDLE -> EmeraldSuccess to "Ready"
                    OrbState.LISTENING -> NeonCyan to "Listening"
                    OrbState.THINKING -> SoftPink to "Thinking"
                    OrbState.SPEAKING -> RadiantMagenta to "Speaking"
                    OrbState.ACTION_SUCCESS -> EmeraldSuccess to "Done"
                    OrbState.ERROR -> Color(0xFFF43F5E) to "Alert"
                    OrbState.VISION -> NeonCyan to "Vision"
                    OrbState.AGENT -> ElectricViolet to "Agent"
                    OrbState.IMAGE_GENERATION -> RadiantMagenta to "Creating"
                }

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusDotColor)
                )
                Spacer(modifier = Modifier.padding(start = 6.dp))
                Text(
                    text = statusLabel,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
