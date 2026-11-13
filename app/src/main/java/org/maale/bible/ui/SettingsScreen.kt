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
import org.maale.bible.data.security.PortableStudyBackup

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repository = remember(context) { StudyDatabase(context.applicationContext) }
    var recoveryKey by rememberSaveable { mutableStateOf("") }
    var backupEnvelope by rememberSaveable { mutableStateOf("") }
    var restoreKey by rememberSaveable { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }

    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        Text("Accessibility", style = MaterialTheme.typography.titleMedium)
        Text("The interface respects Android system font size, display size, contrast and screen-reader settings. Adjust these under your device Settings → Accessibility.")
        Text("Language", style = MaterialTheme.typography.titleMedium)
        Text("The shell currently provides English navigation and the Maale-script product name. Maale and Amharic interface translations are not yet packaged. Scripture language selection is not enabled.")
        HorizontalDivider()
        Text("Portable encrypted study backup", style = MaterialTheme.typography.titleMedium)
        Text("A recovery key encrypts the backup with Argon2id and AES-GCM. Keep the recovery key separately from the backup. Anyone with both can restore your private notes.")
        Button(onClick = {
            runCatching {
                recoveryKey = PortableStudyBackup.generateRecoveryKey()
                backupEnvelope = PortableStudyBackup.export(context, repository, recoveryKey)
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_SUBJECT, "Maale Bible encrypted study backup")
                    putExtra(Intent.EXTRA_TEXT, backupEnvelope)
                }
                context.startActivity(Intent.createChooser(send, "Save encrypted study backup"))
                status = "Encrypted backup prepared. Save the recovery key below separately."
            }.onFailure { status = "Backup failed: ${it.message ?: "unknown error"}" }
        }, modifier = Modifier.semantics { contentDescription = "Create and share portable encrypted study backup" }) {
            Text("Create encrypted backup")
        }
        if (recoveryKey.isNotBlank()) {
            Text("Recovery key — store this securely and separately", style = MaterialTheme.typography.titleSmall)
            SelectionContainer { Text(recoveryKey, style = MaterialTheme.typography.bodySmall) }
        }
        HorizontalDivider()
        Text("Restore portable backup", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = restoreKey, onValueChange = { restoreKey = it }, label = { Text("Recovery key") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = backupEnvelope, onValueChange = { backupEnvelope = it }, label = { Text("Backup envelope JSON") }, modifier = Modifier.fillMaxWidth(), minLines = 4)
        Button(onClick = {
            runCatching {
                PortableStudyBackup.restore(context, backupEnvelope, restoreKey)
                backupEnvelope = ""; restoreKey = ""
                status = "Backup restored. Existing database data was not overwritten."
            }.onFailure { status = "Restore rejected: ${it.message ?: "invalid backup"}" }
        }, enabled = backupEnvelope.isNotBlank() && restoreKey.isNotBlank(),
            modifier = Modifier.semantics { contentDescription = "Restore encrypted study backup using recovery key" }) {
            Text("Restore backup")
        }
        status?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        HorizontalDivider()
        Text("Content availability", style = MaterialTheme.typography.titleMedium)
        Text("The catalogue is available offline. Scripture text, full-text scripture search, audio and translations remain disabled until their source and rights gates are satisfied.")
        Text("Maale Bible · 0.1.0", style = MaterialTheme.typography.labelSmall)
    }
}
