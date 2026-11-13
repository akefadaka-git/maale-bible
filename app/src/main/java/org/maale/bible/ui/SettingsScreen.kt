package org.maale.bible.ui

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.maale.bible.data.repository.StudyDatabase

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repository = remember(context) { StudyDatabase(context.applicationContext) }
    var backupText by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        Text("Reading and accessibility", style = MaterialTheme.typography.titleMedium)
        Text("Maale Bible follows the Android system font-size, display-size, contrast and screen-reader settings. Use your device Settings → Accessibility to adjust them.")
        Text("Language", style = MaterialTheme.typography.titleMedium)
        Text("The app shell currently provides English navigation and the Maale-script product name. Approved Maale and Amharic interface translations are not yet packaged; scripture language selection is not enabled.")
        HorizontalDivider()
        Text("Encrypted study data", style = MaterialTheme.typography.titleMedium)
        Text("Bookmarks, highlights, notes, reading position and plans are stored in the app-private SQLCipher database. Portable backup requires the recovery key created for that backup.")
        Button(
            onClick = {
                runCatching {
                    val json = repository.exportBackup()
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(Intent.EXTRA_SUBJECT, "Maale Bible study backup")
                        putExtra(Intent.EXTRA_TEXT, json)
                    }
                    context.startActivity(Intent.createChooser(send, "Share encrypted study backup"))
                    status = "Backup prepared for sharing. Keep the recovery key separate and secure."
                }.onFailure { status = "Backup failed: ${it.message ?: "unknown error"}" }
            },
            modifier = Modifier.semantics { contentDescription = "Export encrypted study backup" }
        ) { Text("Export study backup") }
        OutlinedTextField(
            value = backupText,
            onValueChange = { backupText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Paste a backup envelope to restore") },
            minLines = 3
        )
        Button(
            onClick = {
                runCatching {
                    repository.restoreBackup(backupText)
                    backupText = ""
                    status = "Backup restored successfully."
                }.onFailure { status = "Restore rejected: ${it.message ?: "invalid backup"}" }
            },
            enabled = backupText.isNotBlank(),
            modifier = Modifier.semantics { contentDescription = "Restore study backup from pasted envelope" }
        ) { Text("Restore backup") }
        status?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        HorizontalDivider()
        Text("Content availability", style = MaterialTheme.typography.titleMedium)
        Text("The catalogue is available offline. Scripture text, full-text scripture search, audio and translations remain disabled until their source and rights gates are satisfied.")
        Text("Maale Bible · 0.1.0", style = MaterialTheme.typography.labelSmall)
    }
}
