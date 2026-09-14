package com.example.sayra.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sayra.data.model.OrbState
import com.example.sayra.ui.components.GlassCard
import com.example.sayra.ui.orb.LiquidAiOrb
import com.example.sayra.ui.viewmodel.SayraViewModel
import com.example.ui.theme.CoralWarning
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GlassBorderDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RadiantMagenta
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun OnboardingScreen(
    viewModel: SayraViewModel,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(1) }
    var inputKey by remember { mutableStateOf(viewModel.secureStorage.getApiKey()) }
    var isKeyVisible by remember { mutableStateOf(false) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var testFeedback by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var isPermissionsGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val micGranted = permissions[Manifest.permission.RECORD_AUDIO] == true
        isPermissionsGranted = micGranted
        if (micGranted) {
            viewModel.haptic.actionSuccess()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(ObsidianBlack, Color(0xFF130E26), ObsidianBlack)
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            when (currentStep) {
                1 -> {
                    // STEP 1: Welcome & Identity
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))

                        // Mini Animated Liquid Orb
                        LiquidAiOrb(
                            orbState = OrbState.IDLE,
                            audioAmplitude = 0f,
                            size = 170.dp,
                            modifier = Modifier.padding(16.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Meet SAYRA AI",
                            color = TextPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Your AI. Your Phone. Your Control.",
                            color = ElectricViolet,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 20.dp
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "«Gemini is the brain.\nSAYRA is the assistant.\nAndroid APIs are the hands.»",
                                    color = SoftLavender,
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "• Voice-first native interaction in English, Hindi & Hinglish\n• Real Android actions (Apps, Wi-Fi, Torch, Battery, Volume)\n• Bring Your Own Gemini Key for total privacy & ownership",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.haptic.tap()
                            currentStep = 2
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .testTag("onboarding_next_step1"),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            "Connect Gemini AI →",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                2 -> {
                    // STEP 2: BYOK Gemini Key Setup
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(ElectricViolet.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Connect Gemini",
                                    color = TextPrimary,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Use your own Gemini API key (BYOK)",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 20.dp
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "Gemini API Key",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                TextField(
                                    value = inputKey,
                                    onValueChange = {
                                        inputKey = it
                                        testFeedback = null
                                    },
                                    placeholder = { Text("AIzaSy...", color = TextSecondary, fontSize = 14.sp) },
                                    singleLine = true,
                                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                            Icon(
                                                imageVector = if (isKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = "Toggle Visibility",
                                                tint = SoftLavender
                                            )
                                        }
                                    },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color(0x33000000),
                                        unfocusedContainerColor = Color(0x33000000),
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        cursorColor = ElectricViolet,
                                        focusedIndicatorColor = ElectricViolet,
                                        unfocusedIndicatorColor = GlassBorderDark
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("api_key_input")
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            if (inputKey.isNotBlank()) {
                                                isTestingConnection = true
                                                testFeedback = null
                                                viewModel.testApiKey(inputKey) { success, msg ->
                                                    isTestingConnection = false
                                                    testFeedback = success to msg
                                                }
                                            }
                                        },
                                        enabled = inputKey.isNotBlank() && !isTestingConnection,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("test_key_button")
                                    ) {
                                        if (isTestingConnection) {
                                            CircularProgressIndicator(
                                                color = ElectricViolet,
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Text("Test Key", color = SoftLavender, fontSize = 13.sp)
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            if (inputKey.isNotBlank()) {
                                                viewModel.saveApiKey(inputKey)
                                                testFeedback = true to "Gemini Connected ✓"
                                            }
                                        },
                                        enabled = inputKey.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("save_key_button")
                                    ) {
                                        Text("Save Key", color = Color.White, fontSize = 13.sp)
                                    }
                                }

                                // Test Feedback Badge
                                AnimatedVisibility(visible = testFeedback != null) {
                                    val feedback = testFeedback
                                    if (feedback != null) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    if (feedback.first) EmeraldSuccess.copy(alpha = 0.15f) else CoralWarning.copy(alpha = 0.15f),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (feedback.first) Icons.Default.CheckCircle else Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = if (feedback.first) EmeraldSuccess else CoralWarning,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = feedback.second,
                                                color = if (feedback.first) EmeraldSuccess else CoralWarning,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Button to open Google AI Studio to get key
                        OutlinedButton(
                            onClick = {
                                val browserIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://aistudio.google.com/app/apikey")
                                )
                                context.startActivity(browserIntent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("get_gemini_key_button"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Get Gemini API Key (Free on Google AI Studio)",
                                color = NeonCyan,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Your key is stored securely only on your phone and never shared with third parties.",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { currentStep = 1 },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Back", color = TextSecondary)
                        }

                        Button(
                            onClick = {
                                if (inputKey.isNotBlank()) {
                                    viewModel.saveApiKey(inputKey)
                                }
                                viewModel.haptic.tap()
                                currentStep = 3
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(2f)
                                .testTag("onboarding_next_step2")
                        ) {
                            Text("Next: Permissions →", color = Color.White)
                        }
                    }
                }

                3 -> {
                    // STEP 3: Permissions Setup
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Device Permissions",
                            color = TextPrimary,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "SAYRA needs access to permitted Android capabilities to be your hands.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Permission item: Microphone
                        PermissionItem(
                            icon = Icons.Default.Mic,
                            title = "Microphone Access",
                            description = "Required for natural real-time voice interaction in English, Hindi & Hinglish."
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Permission item: Phone
                        PermissionItem(
                            icon = Icons.Default.Phone,
                            title = "Phone & Dialer",
                            description = "Allows SAYRA to initiate voice calls when you request (always with confirmation)."
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.RECORD_AUDIO,
                                        Manifest.permission.CALL_PHONE
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPermissionsGranted) EmeraldSuccess else ElectricViolet
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("grant_permissions_button")
                        ) {
                            Text(
                                if (isPermissionsGranted) "Permissions Granted ✓" else "Grant Permissions",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { currentStep = 2 },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Back", color = TextSecondary)
                        }

                        Button(
                            onClick = {
                                viewModel.completeOnboarding()
                                onComplete()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RadiantMagenta),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(2f)
                                .testTag("start_sayra_button")
                        ) {
                            Text("Start SAYRA AI ✦", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(ElectricViolet.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SoftLavender,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
