package org.maale.bible.ui

import org.maale.bible.R

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource

import org.maale.bible.data.security.PortableStudyBackup
import org.maale.bible.data.repository.StudyDatabase

@Composable
fun SettingsScreen(repository: StudyDatabase, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Sensitive values are transient and never serialized into saved-instance state.
    var recoveryKey by remember { mutableStateOf("") }
    var pendingExportKey by remember { mutableStateOf<String?>(null) }
    var restoreKey by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }

    val createBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val key = pendingExportKey
        pendingExportKey = null
        if (uri == null || key == null) {
            status = "Backup export cancelled."
        } else {
            scope.launch {
                status = "Preparing encrypted backup…"
                runCatching {
                    withContext(Dispatchers.IO) {
                        PortableStudyBackup.exportToUri(context, repository, key, uri)
                    }
                }.onSuccess {
                    recoveryKey = key
                    status = "Encrypted backup saved. Store the recovery key separately; it cannot be recovered if this screen is closed."
                }.onFailure {
                    recoveryKey = ""
                    status = "Backup failed: ${it.message ?: "unknown error"}"
                }
            }
        }
    }

    val openBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            status = "Backup selection cancelled."
        } else {
            val key = restoreKey
            scope.launch {
                status = "Validating and restoring backup…"
                runCatching {
                    require(key.isNotBlank()) { "Enter the recovery key before selecting a backup" }
                    withContext(Dispatchers.IO) {
                        PortableStudyBackup.restoreFromUri(context, uri, key)
                    }
                }.onSuccess {
                    restoreKey = ""
                    status = "Backup restored. Existing database data was not overwritten."
                }.onFailure {
                    status = "Restore rejected: ${it.message ?: "invalid backup"}"
                }
            }
        }
    }

    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.settings_accessibility), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.settings_accessibility_help))
        Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.settings_language_help))
        HorizontalDivider()
        Text(stringResource(R.string.settings_backup), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.settings_backup_help))
        Button(onClick = {
            pendingExportKey = PortableStudyBackup.generateRecoveryKey()
            createBackup.launch("maale-bible-study-backup.json")
        }, modifier = Modifier.semantics { contentDescription = "Create encrypted study backup in a user-selected file" }) {
            Text(stringResource(R.string.settings_create_backup))
        }
        if (recoveryKey.isNotBlank()) {
            Text(stringResource(R.string.settings_recovery_key), style = MaterialTheme.typography.titleSmall)
            SelectionContainer { Text(recoveryKey, style = MaterialTheme.typography.bodySmall) }
            Text("Copy this key to a separate secure location. It is held only while this Settings screen remains active.")
        }
        HorizontalDivider()
        Text(stringResource(R.string.settings_restore), style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = restoreKey,
            onValueChange = { restoreKey = it },
            label = { Text(stringResource(R.string.settings_recovery_key_hint)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Button(
            onClick = { openBackup.launch(arrayOf("application/json", "application/octet-stream")) },
            enabled = restoreKey.isNotBlank(),
            modifier = Modifier.semantics { contentDescription = "Select encrypted backup file and restore using recovery key" }
        ) {
            Text(stringResource(R.string.settings_restore_button))
        }
        status?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        HorizontalDivider()
        Text(stringResource(R.string.settings_content), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.settings_content_help))
        Text("Maale Bible · 0.1.0", style = MaterialTheme.typography.labelSmall)
    }
}
