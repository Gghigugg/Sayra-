package com.example.sayra.character

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Lens
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sayra.data.model.OrbState
import com.example.sayra.ui.orb.LiquidAiOrb
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.GlassBorderDark
import com.example.ui.theme.GlassSurfaceDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SayraPresenceContainer(
    orbState: OrbState,
    characterState: CharacterState,
    expression: CharacterExpression,
    audioAmplitude: Float,
    intensityMultiplier: Float,
    performanceMode: CharacterPerformanceMode,
    attentionTarget: CharacterAttentionTarget,
    isCharacterEnabled: Boolean,
    viewMode: CharacterViewMode,
    onToggleViewMode: () -> Unit,
    onOpenFullScreen: () -> Unit,
    onToggleListening: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 280.dp
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Switcher Bar: [ Character | Orb ] + Fullscreen Mode
        if (isCharacterEnabled) {
            Row(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(GlassSurfaceDark)
                    .border(1.dp, GlassBorderDark, RoundedCornerShape(20.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Character Mode Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (viewMode == CharacterViewMode.CHARACTER) ElectricPurple.copy(alpha = 0.4f)
                            else Color.Transparent
                        )
                        .clickable { if (viewMode != CharacterViewMode.CHARACTER) onToggleViewMode() }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (viewMode == CharacterViewMode.CHARACTER) NeonCyan else TextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Character",
                        color = if (viewMode == CharacterViewMode.CHARACTER) TextPrimary else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (viewMode == CharacterViewMode.CHARACTER) FontWeight.Bold else FontWeight.Normal
                    )
                }

                // Orb Mode Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (viewMode == CharacterViewMode.ORB) ElectricPurple.copy(alpha = 0.4f)
                            else Color.Transparent
                        )
                        .clickable { if (viewMode != CharacterViewMode.ORB) onToggleViewMode() }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lens,
                        contentDescription = null,
                        tint = if (viewMode == CharacterViewMode.ORB) NeonCyan else TextSecondary,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Orb",
                        color = if (viewMode == CharacterViewMode.ORB) TextPrimary else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (viewMode == CharacterViewMode.ORB) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Fullscreen Expansion Button
                IconButton(
                    onClick = onOpenFullScreen,
                    modifier = Modifier
                        .size(26.dp)
                        .testTag("presence_fullscreen_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Full-Screen Presence",
                        tint = SoftLavender,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Central Stage: Seamless Transformation Between Character & Liquid Orb
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            val effectiveView = if (!isCharacterEnabled) CharacterViewMode.ORB else viewMode

            AnimatedContent(
                targetState = effectiveView,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(400, easing = FastOutSlowInEasing)) +
                            scaleIn(initialScale = 0.88f, animationSpec = tween(400)))
                        .togetherWith(
                            fadeOut(animationSpec = tween(300)) +
                                    scaleOut(targetScale = 0.88f, animationSpec = tween(300))
                        )
                },
                label = "PresenceTransformation"
            ) { target ->
                when (target) {
                    CharacterViewMode.CHARACTER -> {
                        SayraCharacterRenderer(
                            characterState = characterState,
                            expression = expression,
                            audioAmplitude = audioAmplitude,
                            performanceMode = performanceMode,
                            attentionTarget = attentionTarget,
                            onClick = onToggleListening,
                            size = size
                        )
                    }
                    CharacterViewMode.ORB -> {
                        LiquidAiOrb(
                            orbState = orbState,
                            audioAmplitude = audioAmplitude,
                            intensityMultiplier = intensityMultiplier,
                            performanceMode = performanceMode,
                            size = size,
                            onClick = onToggleListening
                        )
                    }
                }
            }
        }
    }
}
