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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sayra.character.CharacterAttentionTarget
import com.example.sayra.character.CharacterExpression
import com.example.sayra.character.CharacterPerformanceMode
import com.example.sayra.character.CharacterState
import com.example.sayra.character.SayraCharacterRenderer
import com.example.sayra.data.model.OrbState
import com.example.sayra.ui.viewmodel.SayraViewModel
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
fun SayraFullScreenCharacterScreen(
    viewModel: SayraViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val orbState by viewModel.orbState.collectAsState()
    val audioAmplitude by viewModel.audioAmplitude.collectAsState()
    val userPrompt by viewModel.userPrompt.collectAsState()
    val assistantResponse by viewModel.assistantResponse.collectAsState()
    val characterPerformance by viewModel.characterPerformanceMode.collectAsState()
    val characterState by viewModel.characterState.collectAsState()
    val expression by viewModel.characterExpression.collectAsState()
    val attentionTarget by viewModel.characterAttentionTarget.collectAsState()

    val isListening = orbState == OrbState.LISTENING
    val isSpeaking = orbState == OrbState.SPEAKING

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF160E2A),
                        Color(0xFF090611),
                        ObsidianBlack
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Minimal Controls & Presence Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Presence Brand
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(GlassSurfaceDark)
                        .border(1.dp, GlassBorderDark, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isListening) NeonCyan else EmeraldSuccess)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SAYRA DIGITAL PRESENCE",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // Close / Minimize Button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GlassSurfaceDark)
                        .border(1.dp, GlassBorderDark, CircleShape)
                        .testTag("fullscreen_exit_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FullscreenExit,
                        contentDescription = "Minimize Fullscreen",
                        tint = SoftLavender,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Central Hero Digital Human Character
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                SayraCharacterRenderer(
                    characterState = characterState,
                    expression = expression,
                    audioAmplitude = audioAmplitude,
                    performanceMode = characterPerformance,
                    attentionTarget = attentionTarget,
                    onClick = { viewModel.toggleListening() },
                    size = 360.dp,
                    showSignatureEnergy = true
                )
            }

            // Bottom Minimal Response Caption & Microphone Control
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Minimal Response Text Card
                val activeText = when {
                    isSpeaking && assistantResponse.isNotEmpty() -> assistantResponse
                    isListening && userPrompt.isNotEmpty() -> userPrompt
                    orbState == OrbState.THINKING -> "Thinking..."
                    else -> "Tap microphone or speak naturally to SAYRA"
                }

                AnimatedVisibility(
                    visible = activeText.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(GlassSurfaceDark.copy(alpha = 0.85f))
                            .border(1.dp, GlassBorderDark, RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = activeText,
                            color = if (isListening) NeonCyan else TextPrimary,
                            fontSize = 13.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Microphone Interactive Floating Button
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = if (isListening) listOf(NeonCyan, ElectricViolet)
                                else listOf(ElectricPurple, ObsidianBlack)
                            )
                        )
                        .border(
                            2.dp,
                            if (isListening) NeonCyan else GlassBorderDark,
                            CircleShape
                        )
                        .clickable { viewModel.toggleListening() }
                        .testTag("fullscreen_mic_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                        contentDescription = "Voice Control",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}
