package com.example.sayra.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sayra.data.model.ActionConfirmation
import com.example.sayra.data.model.OrbState
import com.example.sayra.data.model.ToolExecutionInfo
import com.example.ui.theme.CoralWarning
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GlassBorderDark
import com.example.ui.theme.GlassSurfaceDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RadiantMagenta
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.SoftPink
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    backgroundColor: Color = GlassSurfaceDark,
    borderColor: Color = GlassBorderDark,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .border(
                BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        listOf(borderColor, borderColor.copy(alpha = 0.15f))
                    )
                ),
                RoundedCornerShape(cornerRadius)
            )
            .clip(RoundedCornerShape(cornerRadius)),
        color = backgroundColor,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        content()
    }
}

@Composable
fun DynamicTranscriptCard(
    userPrompt: String,
    assistantResponse: String,
    toolExecution: ToolExecutionInfo?,
    orbState: OrbState,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status indicator text
        val statusText = when (orbState) {
            OrbState.IDLE -> "Ready"
            OrbState.LISTENING -> "Listening..."
            OrbState.THINKING -> "Thinking..."
            OrbState.SPEAKING -> "Speaking..."
            OrbState.ACTION_SUCCESS -> "Action Completed"
            OrbState.ERROR -> "Action Alert"
            OrbState.VISION -> "Vision Scanning..."
            OrbState.AGENT -> "Autonomous Agent..."
            OrbState.IMAGE_GENERATION -> "Creating Image..."
        }

        Text(
            text = statusText,
            color = when (orbState) {
                OrbState.LISTENING -> NeonCyan
                OrbState.THINKING -> SoftPink
                OrbState.SPEAKING -> RadiantMagenta
                OrbState.ACTION_SUCCESS -> EmeraldSuccess
                OrbState.ERROR -> CoralWarning
                else -> TextSecondary
            },
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Active conversation glass bubble
        AnimatedVisibility(
            visible = userPrompt.isNotEmpty() || assistantResponse.isNotEmpty() || toolExecution != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                cornerRadius = 20.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (userPrompt.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = "You: ",
                                color = NeonCyan,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = userPrompt,
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (assistantResponse.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = "SAYRA: ",
                                color = ElectricViolet,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = assistantResponse,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                lineHeight = 21.sp
                            )
                        }
                    }

                    // Tool action badge if executed
                    if (toolExecution != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (toolExecution.isSuccess) EmeraldSuccess.copy(alpha = 0.15f)
                                    else CoralWarning.copy(alpha = 0.15f),
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    1.dp,
                                    if (toolExecution.isSuccess) EmeraldSuccess.copy(alpha = 0.4f)
                                    else CoralWarning.copy(alpha = 0.4f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val icon: ImageVector = when (toolExecution.toolName) {
                                "openApp" -> Icons.AutoMirrored.Filled.OpenInNew
                                "controlFlashlight" -> Icons.Default.FlashlightOn
                                "controlVolume" -> Icons.AutoMirrored.Filled.VolumeUp
                                "makePhoneCall" -> Icons.Default.Phone
                                "getBatteryStatus" -> Icons.Default.CheckCircle
                                else -> if (toolExecution.isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (toolExecution.isSuccess) EmeraldSuccess else CoralWarning,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${toolExecution.displayName}: ${toolExecution.details}",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Suggestions pills if idle and no recent speech
        if (userPrompt.isEmpty() && assistantResponse.isEmpty() && orbState == OrbState.IDLE) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                val suggestions = listOf("YouTube खोलो", "Check Battery", "Flashlight On", "Wi-Fi Settings")
                suggestions.take(3).forEach { prompt ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .background(GlassSurfaceDark, RoundedCornerShape(16.dp))
                            .border(1.dp, GlassBorderDark, RoundedCornerShape(16.dp))
                            .clickable { onSuggestionClick(prompt) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = prompt,
                            color = SoftLavender,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingGlassDock(
    isListening: Boolean,
    isTextInputOpen: Boolean,
    textInputValue: String,
    onTextInputChange: (String) -> Unit,
    onSendText: (String) -> Unit,
    onToggleListening: () -> Unit,
    onToggleTextInput: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenMemory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Expandable text input bar
        AnimatedVisibility(
            visible = isTextInputOpen,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                cornerRadius = 24.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = textInputValue,
                        onValueChange = onTextInputChange,
                        placeholder = {
                            Text(
                                "Ask SAYRA anything...",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = ElectricViolet,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (textInputValue.isNotBlank()) {
                                onSendText(textInputValue)
                            }
                        }),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("text_input_field")
                    )

                    IconButton(
                        onClick = {
                            if (textInputValue.isNotBlank()) {
                                onSendText(textInputValue)
                            }
                        },
                        modifier = Modifier.testTag("send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send text",
                            tint = if (textInputValue.isNotBlank()) ElectricViolet else TextSecondary
                        )
                    }
                }
            }
        }

        // Floating Minimal Glass Dock
        GlassCard(
            modifier = Modifier.fillMaxWidth(0.92f),
            cornerRadius = 32.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Keyboard / Text input toggle
                IconButton(
                    onClick = onToggleTextInput,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("keyboard_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isTextInputOpen) Icons.Default.Close else Icons.Default.Keyboard,
                        contentDescription = "Keyboard Input",
                        tint = if (isTextInputOpen) RadiantMagenta else SoftLavender
                    )
                }

                // Center glowing Voice Mic Action Button
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = if (isListening) listOf(NeonCyan, ElectricViolet)
                                else listOf(ElectricViolet, ElectricPurple)
                            )
                        )
                        .border(
                            2.dp,
                            if (isListening) NeonCyan else Color.White.copy(alpha = 0.5f),
                            CircleShape
                        )
                        .clickable { onToggleListening() }
                        .testTag("main_mic_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (isListening) "Stop Listening" else "Start Listening",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // History Button
                IconButton(
                    onClick = onOpenHistory,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Conversation History",
                        tint = SoftLavender
                    )
                }

                // Memory Button
                IconButton(
                    onClick = onOpenMemory,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("dock_memory_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Controlled Memory",
                        tint = SoftLavender
                    )
                }

                // Settings Button
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = SoftLavender
                    )
                }
            }
        }
    }
}

@Composable
fun ActionConfirmationModal(
    confirmation: ActionConfirmation?
) {
    if (confirmation == null) return

    AlertDialog(
        onDismissRequest = confirmation.onCancel,
        title = {
            Text(
                text = confirmation.title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Text(
                text = confirmation.description,
                color = TextSecondary,
                fontSize = 14.sp
            )
        },
        confirmButton = {
            Button(
                onClick = confirmation.onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(confirmation.confirmLabel, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = confirmation.onCancel,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = GlassSurfaceDark,
        shape = RoundedCornerShape(24.dp)
    )
}
