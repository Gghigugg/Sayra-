package com.example.sayra.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sayra.data.db.ConversationEntity
import com.example.sayra.ui.components.GlassCard
import com.example.sayra.ui.viewmodel.SayraViewModel
import com.example.ui.theme.CoralWarning
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GlassSurfaceDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: SayraViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val conversations by viewModel.conversations.collectAsState()
    var showClearConfirm by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

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
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("history_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "History",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (conversations.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearConfirm = true },
                        modifier = Modifier.testTag("clear_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear All History",
                            tint = CoralWarning
                        )
                    }
                }
            }

            if (conversations.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = SoftLavender.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Conversations Yet",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap the glowing Liquid Orb or mic button on the home screen to speak with SAYRA.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(conversations, key = { it.id }) { item ->
                        HistoryCard(
                            conversation = item,
                            dateFormat = dateFormat,
                            onDelete = { viewModel.deleteConversation(item.id) }
                        )
                    }
                }
            }
        }

        // Confirmation Modal
        if (showClearConfirm) {
            AlertDialog(
                onDismissRequest = { showClearConfirm = false },
                title = {
                    Text("Clear All History", color = TextPrimary, fontWeight = FontWeight.Bold)
                },
                text = {
                    Text("Are you sure you want to permanently delete all conversation history from local storage?", color = TextSecondary)
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearAllConversations()
                            showClearConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralWarning),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Clear All", color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showClearConfirm = false },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = GlassSurfaceDark,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
private fun HistoryCard(
    conversation: ConversationEntity,
    dateFormat: SimpleDateFormat,
    onDelete: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(Date(conversation.timestamp)),
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete entry",
                        tint = TextSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // User Prompt
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = "You: ",
                    color = NeonCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = conversation.userQuery,
                    color = TextPrimary,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Assistant Response
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = "SAYRA: ",
                    color = ElectricViolet,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = conversation.assistantResponse,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 19.sp
                )
            }

            // Action Badge
            if (!conversation.actionExecuted.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .background(
                            if (conversation.isSuccess) EmeraldSuccess.copy(alpha = 0.15f) else CoralWarning.copy(alpha = 0.15f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = when {
                        conversation.actionExecuted.contains("YouTube", true) ||
                                conversation.actionExecuted.contains("Instagram", true) ||
                                conversation.actionExecuted.contains("App", true) -> Icons.AutoMirrored.Filled.OpenInNew
                        conversation.actionExecuted.contains("Flashlight", true) -> Icons.Default.FlashlightOn
                        conversation.actionExecuted.contains("Volume", true) -> Icons.AutoMirrored.Filled.VolumeUp
                        conversation.actionExecuted.contains("Call", true) -> Icons.Default.Phone
                        else -> Icons.Default.History
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (conversation.isSuccess) EmeraldSuccess else CoralWarning,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Executed: ${conversation.actionExecuted}",
                        color = if (conversation.isSuccess) EmeraldSuccess else CoralWarning,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
