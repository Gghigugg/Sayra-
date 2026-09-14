package com.example.sayra.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import com.example.sayra.character.CharacterPerformanceMode
import com.example.sayra.character.CharacterViewMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.sayra.data.model.LanguageMode
import com.example.sayra.data.storage.PreferencesManager
import com.example.sayra.ui.components.GlassCard
import com.example.sayra.ui.viewmodel.SayraViewModel
import com.example.ui.theme.CoralWarning
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GlassBorderDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    viewModel: SayraViewModel,
    onBack: () -> Unit,
    onNavigateToMemory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val speechRate by viewModel.speechRate.collectAsState()
    val speechPitch by viewModel.speechPitch.collectAsState()
    val languageMode by viewModel.languageMode.collectAsState()
    val aiModel by viewModel.aiModel.collectAsState()
    val orbIntensity by viewModel.orbIntensity.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val isPrivacyMode by viewModel.isPrivacyMode.collectAsState()
    val isWakeWordEnabled by viewModel.isWakeWordEnabled.collectAsState()
    val memories by viewModel.memories.collectAsState()
    val isCharacterEnabled by viewModel.isCharacterEnabled.collectAsState()
    val characterPerformance by viewModel.characterPerformanceMode.collectAsState()
    val characterViewMode by viewModel.characterViewMode.collectAsState()

    var inputKey by remember { mutableStateOf(viewModel.secureStorage.getApiKey()) }
    var isKeyVisible by remember { mutableStateOf(false) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var testFeedback by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var isConfirmSensitive by remember { mutableStateOf(viewModel.prefsManager.isConfirmSensitiveEnabled()) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(ObsidianBlack, Color(0xFF110C22), ObsidianBlack)
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Settings & AI Setup",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // SECTION 1: AI & BYOK
            SettingsSectionHeader(icon = Icons.Default.Key, title = "Gemini AI (BYOK)")

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Status row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Connection Status",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )

                        val hasKey = viewModel.secureStorage.hasValidApiKey()
                        Row(
                            modifier = Modifier
                                .background(
                                    if (hasKey) EmeraldSuccess.copy(alpha = 0.15f) else CoralWarning.copy(alpha = 0.15f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (hasKey) EmeraldSuccess else CoralWarning)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (hasKey) "Connected" else "No Key",
                                color = if (hasKey) EmeraldSuccess else CoralWarning,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Active Key: ${viewModel.secureStorage.getMaskedApiKey()}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    TextField(
                        value = inputKey,
                        onValueChange = {
                            inputKey = it
                            testFeedback = null
                        },
                        placeholder = { Text("Enter your Gemini API key...", color = TextSecondary, fontSize = 13.sp) },
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
                            .testTag("settings_key_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

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
                                .testTag("settings_test_key_button")
                        ) {
                            if (isTestingConnection) {
                                CircularProgressIndicator(
                                    color = ElectricViolet,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Test Key", color = SoftLavender, fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = {
                                if (inputKey.isNotBlank()) {
                                    viewModel.saveApiKey(inputKey)
                                    testFeedback = true to "Key Saved Successfully ✓"
                                }
                            },
                            enabled = inputKey.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_save_key_button")
                        ) {
                            Text("Save Key", color = Color.White, fontSize = 12.sp)
                        }

                        if (viewModel.secureStorage.isCustomKeySaved()) {
                            IconButton(
                                onClick = {
                                    viewModel.removeApiKey()
                                    inputKey = ""
                                    testFeedback = false to "Key Removed"
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove Key",
                                    tint = CoralWarning
                                )
                            }
                        }
                    }

                    // Test feedback
                    AnimatedVisibility(visible = testFeedback != null) {
                        val feedback = testFeedback
                        if (feedback != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = feedback.second,
                                color = if (feedback.first) EmeraldSuccess else CoralWarning,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Shortcut to get key
                    OutlinedButton(
                        onClick = {
                            val browserIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://aistudio.google.com/app/apikey")
                            )
                            context.startActivity(browserIntent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Get Free Gemini Key (Google AI Studio)", color = NeonCyan, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Centralized Model Engine selector
                    Text(
                        text = "Gemini Model Architecture",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    PreferencesManager.AVAILABLE_MODELS.forEach { modelOption ->
                        val isSelected = aiModel == modelOption.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) ElectricViolet.copy(alpha = 0.25f) else Color(0x22000000))
                                .border(
                                    1.dp,
                                    if (isSelected) ElectricViolet else GlassBorderDark,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.updateAiModel(modelOption.id) }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = modelOption.displayName,
                                        color = if (isSelected) NeonCyan else TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Text(
                                        text = modelOption.description,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 2: Controlled Memory
            SettingsSectionHeader(icon = Icons.Default.Bookmark, title = "Controlled Memory")

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Saved Memories (${memories.size})",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "SAYRA stores facts only when you ask. You can view, edit, or delete them anytime.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onNavigateToMemory,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manage_memory_button")
                    ) {
                        Text("Manage Memory (View, Edit, Delete)", color = Color.White, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 3: Privacy Mode
            SettingsSectionHeader(icon = Icons.Default.Security, title = "Privacy Mode")

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Privacy Mode",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isPrivacyMode)
                                    "ACTIVE: Conversation persistence is minimized, and new memories are not saved."
                                else
                                    "When enabled, SAYRA minimizes persistent storage and pauses memory learning.",
                                color = if (isPrivacyMode) NeonCyan else TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                        Switch(
                            checked = isPrivacyMode,
                            onCheckedChange = { viewModel.updatePrivacyMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ElectricViolet
                            ),
                            modifier = Modifier.testTag("privacy_mode_switch")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 4: Voice & Speech Settings
            SettingsSectionHeader(icon = Icons.Default.Language, title = "Voice & Language")

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Voice Input & Speech Language",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Language modes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LanguageMode.values().forEach { mode ->
                            val isSelected = languageMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) ElectricViolet.copy(alpha = 0.3f) else Color(0x22000000))
                                    .border(
                                        1.dp,
                                        if (isSelected) ElectricViolet else GlassBorderDark,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { viewModel.updateLanguageMode(mode) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.displayName.substringBefore(" ("),
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Speech Speed Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Voice Speed", color = TextPrimary, fontSize = 13.sp)
                        Text("${(speechRate * 100).toInt()}%", color = SoftLavender, fontSize = 13.sp)
                    }
                    Slider(
                        value = speechRate,
                        onValueChange = { viewModel.updateSpeechSettings(it, speechPitch) },
                        valueRange = 0.5f..1.8f,
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricViolet,
                            activeTrackColor = ElectricViolet,
                            inactiveTrackColor = Color(0x33A78BFA)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Speech Pitch Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Voice Pitch", color = TextPrimary, fontSize = 13.sp)
                        Text("${(speechPitch * 100).toInt()}%", color = SoftLavender, fontSize = 13.sp)
                    }
                    Slider(
                        value = speechPitch,
                        onValueChange = { viewModel.updateSpeechSettings(speechRate, it) },
                        valueRange = 0.7f..1.4f,
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricViolet,
                            activeTrackColor = ElectricViolet,
                            inactiveTrackColor = Color(0x33A78BFA)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 5: Assistant & Actions
            SettingsSectionHeader(icon = Icons.Default.Tune, title = "Assistant & Actions")

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Confirm Sensitive Actions",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Show confirmation dialog before dialing calls or preparing messages",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                        Switch(
                            checked = isConfirmSensitive,
                            onCheckedChange = {
                                isConfirmSensitive = it
                                viewModel.prefsManager.setConfirmSensitiveEnabled(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ElectricViolet
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Wake Word Architecture preview
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hands-free Wake Word (Preview)",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Prepares background engine for hands-free 'Hey SAYRA' activation",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                        Switch(
                            checked = isWakeWordEnabled,
                            onCheckedChange = { viewModel.updateWakeWord(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ElectricViolet
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 6: Android Capabilities & Permissions
            SettingsSectionHeader(icon = Icons.Default.CheckCircle, title = "Android Capabilities & Permissions")

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    val micGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                    val phoneGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
                    val contactsGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                    val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                    } else true

                    PermissionStatusRow(icon = Icons.Default.Mic, name = "Microphone", isGranted = micGranted)
                    PermissionStatusRow(icon = Icons.Default.Phone, name = "Phone Calls", isGranted = phoneGranted)
                    PermissionStatusRow(icon = Icons.Default.Person, name = "Contacts Access", isGranted = contactsGranted)
                    PermissionStatusRow(icon = Icons.Default.Notifications, name = "Notifications", isGranted = notifGranted)

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Manage App Permissions in Android Settings", color = SoftLavender, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 7: SAYRA Digital Character System
            SettingsSectionHeader(icon = Icons.Default.AutoAwesome, title = "SAYRA Digital Character")

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Digital Character Presence",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Original holographic AI companion with live lip-sync, blinking, and liquid energy element",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                        Switch(
                            checked = isCharacterEnabled,
                            onCheckedChange = { viewModel.updateCharacterEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ElectricViolet
                            )
                        )
                    }

                    if (isCharacterEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Default Home Presence",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                CharacterViewMode.CHARACTER to "Digital Character",
                                CharacterViewMode.ORB to "Liquid Orb"
                            ).forEach { (mode, label) ->
                                val isSelected = characterViewMode == mode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) ElectricViolet.copy(alpha = 0.3f) else Color(0x22000000))
                                        .border(
                                            1.dp,
                                            if (isSelected) ElectricViolet else GlassBorderDark,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            if (!isSelected) viewModel.toggleCharacterViewMode()
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) TextPrimary else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Animation & Particle Performance",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        listOf(
                            CharacterPerformanceMode.HIGH to "Cinematic (32 Particles, Deep Shaders)",
                            CharacterPerformanceMode.MEDIUM to "Balanced (18 Particles, Smooth 60fps)",
                            CharacterPerformanceMode.LOW to "Battery Saver (8 Particles, Essential)"
                        ).forEach { (mode, label) ->
                            val isSelected = characterPerformance == mode
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) ElectricViolet.copy(alpha = 0.25f) else Color(0x18000000))
                                    .border(
                                        1.dp,
                                        if (isSelected) ElectricViolet else GlassBorderDark,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { viewModel.updateCharacterPerformanceMode(mode) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) NeonCyan else Color.Transparent)
                                            .border(1.dp, if (isSelected) NeonCyan else TextSecondary, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = label,
                                        color = if (isSelected) TextPrimary else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 8: Appearance & Orb
            SettingsSectionHeader(icon = Icons.Default.Palette, title = "Appearance & Orb")

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Orb Animation Intensity", color = TextPrimary, fontSize = 13.sp)
                        Text("${(orbIntensity * 100).toInt()}%", color = SoftLavender, fontSize = 13.sp)
                    }
                    Slider(
                        value = orbIntensity,
                        onValueChange = { viewModel.updateOrbIntensity(it) },
                        valueRange = 0.6f..1.4f,
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricViolet,
                            activeTrackColor = ElectricViolet,
                            inactiveTrackColor = Color(0x33A78BFA)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dark Mode Theme", color = TextPrimary, fontSize = 14.sp)
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { viewModel.updateDarkTheme(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ElectricViolet
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 8: About SAYRA AI
            SettingsSectionHeader(icon = Icons.Default.Info, title = "About SAYRA AI")

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "SAYRA AI",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Version 1.0 (Production Foundation)",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tagline: \"Your AI. Your Phone. Your Control.\"",
                        color = SoftLavender,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Architecture: BYOK Gemini AI Brain + Liquid Glass Visual Interface + Controlled Native Android Engine.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun PermissionStatusRow(
    icon: ImageVector,
    name: String,
    isGranted: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SoftLavender,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = name, color = TextPrimary, fontSize = 13.sp)
        }

        Row(
            modifier = Modifier
                .background(
                    if (isGranted) EmeraldSuccess.copy(alpha = 0.15f) else CoralWarning.copy(alpha = 0.15f),
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isGranted) "Granted ✓" else "Not Granted",
                color = if (isGranted) EmeraldSuccess else CoralWarning,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(icon: ImageVector, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = SoftLavender,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
    }
}
