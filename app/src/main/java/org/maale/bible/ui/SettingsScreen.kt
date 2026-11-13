package org.maale.bible.ui

import org.maale.bible.R

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource

import org.maale.bible.data.security.PortableStudyBackup

@Composable
fun SettingsScreen(repository: org.maale.bible.data.repository.StudyDatabase, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var recoveryKey by rememberSaveable { mutableStateOf("") }
    var backupEnvelope by rememberSaveable { mutableStateOf("") }
    var restoreKey by rememberSaveable { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }

    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.nav_settings), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.settings_accessibility), modifier = Modifier.testTag("settings_accessibility_section"), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.settings_accessibility_help))
        Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.settings_language_help))
        HorizontalDivider()
        Text(stringResource(R.string.settings_backup), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.settings_backup_help))
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
            Text(stringResource(R.string.settings_create_backup))
        }
        if (recoveryKey.isNotBlank()) {
            Text(stringResource(R.string.settings_recovery_key), style = MaterialTheme.typography.titleSmall)
            SelectionContainer { Text(recoveryKey, style = MaterialTheme.typography.bodySmall) }
        }
        HorizontalDivider()
        Text(stringResource(R.string.settings_restore), style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = restoreKey, onValueChange = { restoreKey = it }, label = { Text(stringResource(R.string.settings_recovery_key_hint)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = backupEnvelope, onValueChange = { backupEnvelope = it }, label = { Text(stringResource(R.string.settings_envelope_hint)) }, modifier = Modifier.fillMaxWidth(), minLines = 4)
        Button(onClick = {
            runCatching {
                PortableStudyBackup.restore(context, backupEnvelope, restoreKey)
                backupEnvelope = ""; restoreKey = ""
                status = "Backup restored. Existing database data was not overwritten."
            }.onFailure { status = "Restore rejected: ${it.message ?: "invalid backup"}" }
        }, enabled = backupEnvelope.isNotBlank() && restoreKey.isNotBlank(),
            modifier = Modifier.semantics { contentDescription = "Restore encrypted study backup using recovery key" }) {
            Text(stringResource(R.string.settings_restore_button))
        }
        status?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        HorizontalDivider()
        Text(stringResource(R.string.settings_content), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.settings_content_help))
        Text("Maale Bible · 0.1.0", style = MaterialTheme.typography.labelSmall)
    }
}
