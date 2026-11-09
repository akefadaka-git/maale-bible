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

private enum class StudySection(val title: String) {
    NOTES("Notes"), BOOKMARKS("Bookmarks"), HIGHLIGHTS("Highlights"), COLLECTIONS("Collections")
}

/** Persistent local journal. Database operations are serialized off the Compose main thread. */
@Composable
fun SmartStudyWorkspaceScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current.applicationContext
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
                            ?: "Your study notes could not be saved. Please try again."
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
                Text("YOUR STUDY SPACE", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                Text("Study deeper. Keep what matters.", style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
                Text("A private workspace for reflections and discoveries.",
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
                    SectionIntro("Reflection notes", "Capture a thought while it is fresh.")
                    Card(
                        modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(if (editingNoteId == null) "NEW REFLECTION" else "EDIT REFLECTION",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = draft, onValueChange = { draft = it }, modifier = Modifier.fillMaxWidth(),
                                minLines = 4, enabled = !busy,
                                placeholder = { Text("Write your thought, question or prayer…") },
                                supportingText = { Text("Saved privately on this device • no passage reference attached") }
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
                                                    "This note no longer exists. Refresh the list and try again."
                                                }
                                            }
                                            database.journalNotes()
                                        }) { draft = ""; editingNoteId = null }
                                    }
                                },
                                enabled = draft.isNotBlank() && !busy, modifier = Modifier.fillMaxWidth()
                            ) { Text(if (busy) "Saving…" else if (editingNoteId == null) "Save reflection" else "Update reflection") }
                            if (editingNoteId != null) {
                                OutlinedButton(
                                    onClick = { draft = ""; editingNoteId = null },
                                    enabled = !busy, modifier = Modifier.fillMaxWidth()
                                ) { Text("Cancel edit") }
                            }
                        }
                    }

                    SectionIntro("Saved reflections", when {
                        loading -> "Loading your notes from this device…"
                        notes.isEmpty() -> "Your saved reflections will appear here."
                        else -> "${notes.size} saved reflection(s)"
                    })

                    if (loading) {
                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (notes.isEmpty()) {
                        EmptyStudyCard("No reflections yet",
                            "Start with a question, a personal insight or something you want to revisit.")
                    } else {
                        notes.forEach { note ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("PRIVATE REFLECTION", style = MaterialTheme.typography.labelSmall,
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
                                        ) { Text("Edit") }
                                        Spacer(Modifier.size(8.dp))
                                        OutlinedButton(
                                            onClick = {
                                                runDatabase({ database ->
                                                    check(database.deleteJournalNote(note.id)) {
                                                        "This note no longer exists. Refresh the list and try again."
                                                    }
                                                    database.journalNotes()
                                                }) {
                                                    if (editingNoteId == note.id) {
                                                        editingNoteId = null
                                                        draft = ""
                                                    }
                                                }
                                            }, enabled = !busy
                                        ) { Text("Delete") }
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
                    ) { Text("Refresh saved notes") }
                }
                StudySection.BOOKMARKS -> LockedStudyFeature(
                    "Saved passages",
                    "Bookmarks will connect to canonical book, chapter and verse identifiers after verified scripture structure is integrated.",
                    "PASSAGE REFERENCES NOT AVAILABLE"
                )
                StudySection.HIGHLIGHTS -> LockedStudyFeature(
                    "Scripture highlights",
                    "Highlight colours and annotations will be attached to verified passage ranges. No highlight can be created against an unverified reference.",
                    "VERIFIED PASSAGE DATA REQUIRED"
                )
                StudySection.COLLECTIONS -> LockedStudyFeature(
                    "Study collections",
                    "Organize bookmarks, notes and highlights into themes once durable passage-linked records are available.",
                    "COLLECTION STORAGE NOT CONNECTED"
                )
            }

            Text("LOCAL STUDY • PRIVATE ON-DEVICE STORAGE",
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
                Text("PERSISTENT STUDY NOTES", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                Text(
                    when {
                        loading -> "Loading notes from your device."
                        busy -> "Saving your changes securely on this device."
                        else -> "Your journal notes are stored locally and remain available after you reopen the app."
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
    if (selected) Button(onClick = onClick, modifier = modifier) { Text(item.title, maxLines = 1) }
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
        EmptyStudyCard("Not connected yet",
            "This area is intentionally inactive until verified passage identities and durable study operations are available.")
        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) {
            Text(status, modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold)
        }
    }
}
