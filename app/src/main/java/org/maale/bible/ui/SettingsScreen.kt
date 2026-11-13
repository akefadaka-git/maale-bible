package org.maale.bible.ui

import org.maale.bible.R

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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

    // Recovery material is deliberately transient: never use rememberSaveable or SavedStateHandle.
    var recoveryKey by remember { mutableStateOf("") }
    var restoreKey by remember { mutableStateOf("") }
    // A SAF document URI is not secret and may survive configuration recreation.
    var selectedRestoreUri by rememberSaveable { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf<String?>(null) }

    val createBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) {
            status = "Backup export cancelled."
        } else {
            // Generate the key only after the picker returns. Nothing secret is held while
            // the external document picker is in the foreground.
            val key = PortableStudyBackup.generateRecoveryKey()
            scope.launch {
                status = "Preparing encrypted backup…"
                runCatching {
                    withContext(Dispatchers.IO) {
                        PortableStudyBackup.exportToUri(context, repository, key, uri)
                    }
                }.onSuccess {
                    recoveryKey = key
                    status = "Encrypted backup saved. Store the recovery key separately; it is held only while this screen remains active."
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
            selectedRestoreUri = null
            status = "Backup selection cancelled."
        } else {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            selectedRestoreUri = uri.toString()
            status = "Backup selected. Enter its recovery key, then choose Restore."
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
            recoveryKey = ""
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
        Button(
            onClick = { openBackup.launch(arrayOf("application/json", "application/octet-stream")) },
            modifier = Modifier.semantics { contentDescription = "Select encrypted backup file" }
        ) {
            Text(if (selectedRestoreUri == null) "Choose backup file" else "Choose a different backup")
        }
        selectedRestoreUri?.let {
            Text("Backup file selected. The recovery key is requested after file selection.")
            OutlinedTextField(
                value = restoreKey,
                onValueChange = { restoreKey = it },
                label = { Text(stringResource(R.string.settings_recovery_key_hint)) },
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Transient backup recovery key" },
                singleLine = true
            )
            Button(
                onClick = {
                    val uri = Uri.parse(selectedRestoreUri)
                    val key = restoreKey
                    scope.launch {
                        status = "Validating and restoring backup…"
                        runCatching {
                            require(key.isNotBlank()) { "Enter the recovery key" }
                            withContext(Dispatchers.IO) {
                                PortableStudyBackup.restoreFromUri(context, uri, key)
                            }
                        }.onSuccess {
                            restoreKey = ""
                            selectedRestoreUri = null
                            status = "Backup restored. Existing database data was not overwritten."
                        }.onFailure {
                            status = "Restore rejected: ${it.message ?: "invalid backup"}"
                        }
                    }
                },
                enabled = restoreKey.isNotBlank(),
                modifier = Modifier.semantics { contentDescription = "Restore selected encrypted backup" }
            ) {
                Text(stringResource(R.string.settings_restore_button))
            }
        }
        status?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        HorizontalDivider()
        Text(stringResource(R.string.settings_content), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.settings_content_help))
        Text("Maale Bible · 0.1.0", style = MaterialTheme.typography.labelSmall)
    }
}
