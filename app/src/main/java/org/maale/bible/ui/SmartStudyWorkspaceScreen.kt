package org.maale.bible.ui

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.maale.bible.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.maale.bible.data.LocalStudyDatabase
import org.maale.bible.data.StudyNote
import java.text.DateFormat
import java.util.Date
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

private enum class StudySection(val titleRes: Int) {
    NOTES(R.string.study_notes), BOOKMARKS(R.string.study_bookmarks),
    HIGHLIGHTS(R.string.study_highlights), COLLECTIONS(R.string.study_collections)
}

/** Persistent local journal. Database operations are serialized off the Compose main thread. */
@Composable
fun SmartStudyWorkspaceScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current.applicationContext
    val saveErrorText = stringResource(R.string.study_save_error)
    val noteMissingText = stringResource(R.string.study_note_missing)
    val executor = remember(context) { Executors.newSingleThreadExecutor() }
    val databaseRef = remember(context) { AtomicReference<LocalStudyDatabase?>(null) }
    val active = remember(executor) { AtomicBoolean(true) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    var section by remember { mutableStateOf(StudySection.NOTES) }
    var draft by remember { mutableStateOf("") }
    var editingNoteId by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf<List<StudyNote>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var busy by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    DisposableEffect(executor) {
        onDispose {
            active.set(false)
            executor.execute { databaseRef.getAndSet(null)?.close() }
            executor.shutdown()
        }
    }

    fun runDatabase(
        operation: (LocalStudyDatabase) -> List<StudyNote>,
        onSuccess: () -> Unit = {}
    ) {
        if (busy) return
        busy = true
        errorMessage = null
        executor.execute {
            try {
                val database = databaseRef.updateAndGet { it ?: LocalStudyDatabase.open(context) }!!
                val result = operation(database)
                mainHandler.post {
                    if (active.get()) {
                        notes = result
                        loading = false
                        busy = false
                        errorMessage = null
                        onSuccess()
                    }
                }
            } catch (failure: Exception) {
                mainHandler.post {
                    if (active.get()) {
                        loading = false
                        busy = false
                        errorMessage = failure.message?.takeIf { it.isNotBlank() }
                            ?: stringResource(R.string.study_save_error)
                    }
                }
            }
        }
    }

    LaunchedEffect(executor) { runDatabase({ it.journalNotes() }) }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        val wide = maxWidth >= 640.dp
        Column(
            modifier = Modifier.widthIn(max = 900.dp).fillMaxWidth()
                .align(Alignment.TopCenter).verticalScroll(rememberScrollState())
                .padding(horizontal = if (wide) 32.dp else 18.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.study_your_space).uppercase(), style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.study_deeper) + ". Keep what matters.", style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.study_private_workspace),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            StudyStatusCard(loading, busy)

            if (wide) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StudySection.entries.forEach { item ->
                        StudyTab(item, section == item, Modifier.weight(1f)) { section = item }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StudyTab(StudySection.NOTES, section == StudySection.NOTES, Modifier.weight(1f)) { section = StudySection.NOTES }
                        StudyTab(StudySection.BOOKMARKS, section == StudySection.BOOKMARKS, Modifier.weight(1f)) { section = StudySection.BOOKMARKS }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StudyTab(StudySection.HIGHLIGHTS, section == StudySection.HIGHLIGHTS, Modifier.weight(1f)) { section = StudySection.HIGHLIGHTS }
                        StudyTab(StudySection.COLLECTIONS, section == StudySection.COLLECTIONS, Modifier.weight(1f)) { section = StudySection.COLLECTIONS }
                    }
                }
            }

            when (section) {
                StudySection.NOTES -> {
                    SectionIntro(stringResource(R.string.study_reflection_notes), stringResource(R.string.study_capture_thought))
                    Card(
                        modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(if (editingNoteId == null) stringResource(R.string.study_new_note).uppercase() else stringResource(R.string.study_edit_note).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = draft, onValueChange = { draft = it }, modifier = Modifier.fillMaxWidth(),
                                minLines = 4, enabled = !busy,
                                placeholder = { Text(stringResource(R.string.study_write_thought)) },
                                supportingText = { Text(stringResource(R.string.study_saved_privately) + " • no passage reference attached") }
                            )
                            Button(
                                onClick = {
                                    val body = draft.trim()
                                    if (body.isNotEmpty()) {
                                        val now = System.currentTimeMillis()
                                        val targetId = editingNoteId
                                        runDatabase({ database ->
                                            if (targetId == null) {
                                                database.addJournalNote(UUID.randomUUID().toString(), body, now)
                                            } else {
                                                check(database.updateJournalNote(targetId, body, now)) {
                                                    stringResource(R.string.study_note_missing)
                                                }
                                            }
                                            database.journalNotes()
                                        }) { draft = ""; editingNoteId = null }
                                    }
                                },
                                enabled = draft.isNotBlank() && !busy, modifier = Modifier.fillMaxWidth()
                            ) { Text(if (busy) stringResource(R.string.study_saving) else if (editingNoteId == null) stringResource(R.string.study_save_reflection) else stringResource(R.string.study_update_reflection)) }
                            if (editingNoteId != null) {
                                OutlinedButton(
                                    onClick = { draft = ""; editingNoteId = null },
                                    enabled = !busy, modifier = Modifier.fillMaxWidth()
                                ) { Text(stringResource(R.string.study_cancel_edit)) }
                            }
                        }
                    }

                    SectionIntro(stringResource(R.string.study_saved_reflections), when {
                        loading -> stringResource(R.string.study_loading_notes)
                        notes.isEmpty() -> stringResource(R.string.study_empty_notes)
                        else -> stringResource(R.string.study_saved_reflections_count, notes.size)
                    })

                    if (loading) {
                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (notes.isEmpty()) {
                        EmptyStudyCard(stringResource(R.string.study_no_reflections),
                            stringResource(R.string.study_start_reflection))
                    } else {
                        notes.forEach { note ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(stringResource(R.string.study_private_reflection).uppercase(), style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                                    Text(note.body, style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface)
                                    Text(
                                        "Updated ${DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(note.updatedAt))}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        OutlinedButton(
                                            onClick = { editingNoteId = note.id; draft = note.body }, enabled = !busy
                                        ) { Text(stringResource(R.string.study_edit_note)) }
                                        Spacer(Modifier.size(8.dp))
                                        OutlinedButton(
                                            onClick = {
                                                runDatabase({ database ->
                                                    check(database.deleteJournalNote(note.id)) {
                                                        stringResource(R.string.study_note_missing)
                                                    }
                                                    database.journalNotes()
                                                }) {
                                                    if (editingNoteId == note.id) {
                                                        editingNoteId = null
                                                        draft = ""
                                                    }
                                                }
                                            }, enabled = !busy
                                        ) { Text(stringResource(R.string.study_delete)) }
                                    }
                                }
                            }
                        }
                    }

                    errorMessage?.let { message ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(message, modifier = Modifier.padding(14.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    OutlinedButton(
                        onClick = { runDatabase({ it.journalNotes() }) },
                        enabled = !busy, modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.study_refresh)) }
                }
                StudySection.BOOKMARKS -> LockedStudyFeature(
                    stringResource(R.string.study_saved_passages),
                    stringResource(R.string.study_bookmarks_description),
                    stringResource(R.string.study_passage_references_unavailable).uppercase()
                )
                StudySection.HIGHLIGHTS -> LockedStudyFeature(
                    stringResource(R.string.study_highlights),
                    stringResource(R.string.study_highlights_description),
                    stringResource(R.string.study_verified_passage_required).uppercase()
                )
                StudySection.COLLECTIONS -> LockedStudyFeature(
                    stringResource(R.string.study_collections),
                    stringResource(R.string.study_collections_description),
                    stringResource(R.string.study_collection_storage_unavailable).uppercase()
                )
            }

            Text(stringResource(R.string.study_local_private).uppercase(),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp),
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StudyStatusCard(loading: Boolean, busy: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(10.dp).background(MaterialTheme.colorScheme.secondary, CircleShape))
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(stringResource(R.string.study_persistent_notes).uppercase(), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                Text(
                    when {
                        loading -> stringResource(R.string.study_loading_device)
                        busy -> stringResource(R.string.study_saving_device)
                        else -> stringResource(R.string.study_local_persistence)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun StudyTab(item: StudySection, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    if (selected) Button(onClick = onClick, modifier = modifier) { Text(stringResource(item.titleRes), maxLines = 1) }
    else OutlinedButton(onClick = onClick, modifier = modifier) { Text(item.title, maxLines = 1) }
}

@Composable
private fun SectionIntro(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyStudyCard(title: String, message: String) {
    Card(modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text("✧", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LockedStudyFeature(title: String, description: String, status: String) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionIntro(title, description)
        EmptyStudyCard(stringResource(R.string.study_not_connected),
            stringResource(R.string.study_inactive_description))
        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) {
            Text(status, modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold)
        }
    }
}
