package com.offx.sayra

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.offx.sayra.data.SayraPreferences
import com.offx.sayra.service.SayraVoiceService

class MainActivity : ComponentActivity() {
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState)
        requestNeededPermissions()
        setContent { SayraApp() }
    }
    private fun requestNeededPermissions() {
        val permissions = buildList { add(Manifest.permission.RECORD_AUDIO); if (android.os.Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS) }
        val missing = permissions.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) permissionLauncher.launch(missing.toTypedArray())
    }
}

@Composable private fun SayraApp() {
    val prefs = remember { SayraPreferences(LocalContext.current) }
    var key by remember { mutableStateOf(prefs.apiKey) }
    var configured by remember { mutableStateOf(prefs.apiKey.isNotBlank()) }
    var wake by remember { mutableStateOf(prefs.wakeWordEnabled) }
    val context = LocalContext.current
    MaterialTheme(colorScheme = darkColorScheme()) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black, Color(0xFF090A12), Color.Black)))) {
            Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(42.dp)); Text("SAYRA", style = MaterialTheme.typography.headlineLarge); Text("Your AI. Your Phone. Your Control.", color = Color.LightGray)
                Spacer(Modifier.height(36.dp)); LiquidOrb()
                Spacer(Modifier.height(28.dp))
                if (!configured) {
                    Text("Enter your own Google AI Studio API key", color = Color.White)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(value = key, onValueChange = { key = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Gemini API key") })
                    Spacer(Modifier.height(14.dp))
                    Button(onClick = { if (key.isNotBlank()) { prefs.apiKey = key.trim(); configured = true } }, modifier = Modifier.fillMaxWidth()) { Text("Securely save & continue") }
                } else {
                    GlassButton("Start SAYRA", onClick = { context.startForegroundService(Intent(context, SayraVoiceService::class.java)) })
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) { Text("Hey Sayra", modifier = Modifier.weight(1f)); Switch(checked = wake, onCheckedChange = { wake = it; prefs.wakeWordEnabled = it }) }
                    Text("Wake word is opt-in. Android may require a visible foreground-service notification for continuous microphone use.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.weight(1f)); Text("Privacy: your API key is stored locally. SAYRA does not ship a shared key.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable private fun LiquidOrb() { val t = rememberInfiniteTransition(label = "orb"); val s by t.animateFloat(0.94f, 1.06f, infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "scale"); Box(Modifier.size(190.dp).scale(s).background(Brush.radialGradient(listOf(Color(0xFFE9F5FF), Color(0xFF7C8CFF), Color(0xFF181B42), Color.Transparent)), CircleShape)) }
@Composable private fun GlassButton(text: String, onClick: () -> Unit) { Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = .12f))) { Text(text) } }
