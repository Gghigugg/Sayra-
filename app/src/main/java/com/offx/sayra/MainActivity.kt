package com.offx.sayra

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.offx.sayra.ui.SayraOrb
import com.offx.sayra.ui.SayraViewModel
import com.offx.sayra.ui.SettingsSheet

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: SayraViewModel = viewModel()
            var showSettings by remember { mutableStateOf(false) }
            val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
            LaunchedEffect(Unit) {
                if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    micPermission.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
            MaterialTheme(colorScheme = darkColorScheme()) {
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black, Color(0xFF08080C), Color.Black)))) {
                    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column { Text("SAYRA", style = MaterialTheme.typography.headlineSmall); Text("Your AI. Your Phone. Your Control.", color = Color.LightGray, style = MaterialTheme.typography.labelMedium) }
                            IconButton(onClick = { showSettings = true }) { Icon(Icons.Default.Settings, "Settings") }
                        }
                        Spacer(Modifier.weight(1f))
                        SayraOrb(state = vm.state, onTap = { vm.toggleListening(this@MainActivity) })
                        Spacer(Modifier.height(24.dp))
                        Text(vm.status, color = Color.White, style = MaterialTheme.typography.titleMedium)
                        if (vm.lastText.isNotBlank()) {
                            Spacer(Modifier.height(12.dp))
                            Surface(shape = RoundedCornerShape(24.dp), color = Color.White.copy(alpha = .08f), modifier = Modifier.fillMaxWidth()) {
                                Text(vm.lastText, Modifier.padding(18.dp), color = Color.White)
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        Text("Say “Hey Sayra” or tap the orb", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                    if (showSettings) SettingsSheet(vm, onClose = { showSettings = false })
                }
            }
        }
    }

    fun launchUrl(url: String) { startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))) }
}
