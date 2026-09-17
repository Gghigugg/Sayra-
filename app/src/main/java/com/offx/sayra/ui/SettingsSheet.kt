package com.offx.sayra.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsSheet(vm: SayraViewModel, onClose: () -> Unit) {
    var key by remember { mutableStateOf(vm.apiKey()) }
    var wake by remember { mutableStateOf(vm.wakeWordEnabled()) }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background.copy(alpha = .98f)) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("SAYRA Settings", style = MaterialTheme.typography.headlineSmall); TextButton(onClick = onClose) { Text("Done") } }
            Text("Gemini API key", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(key, { key = it }, Modifier.fillMaxWidth(), singleLine = true)
            Button(onClick = { vm.saveApiKey(key) }, Modifier.fillMaxWidth()) { Text("Save API key") }
            HorizontalDivider()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Column(Modifier.weight(1f)) { Text("Hey Sayra", style = MaterialTheme.typography.titleMedium); Text("Wake-word foundation", style = MaterialTheme.typography.bodySmall) }; Switch(wake, { wake = it; vm.setWakeWordEnabled(it) }) }
            Text("Privacy: your API key is stored locally and is not included in source code or a shared project key.", style = MaterialTheme.typography.bodySmall)
        }
    }
}
