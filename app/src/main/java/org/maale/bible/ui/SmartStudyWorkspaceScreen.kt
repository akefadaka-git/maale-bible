package org.maale.bible.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private enum class StudySection(val title: String) {
    NOTES("Notes"), BOOKMARKS("Bookmarks"), HIGHLIGHTS("Highlights"), COLLECTIONS("Collections")
}

/**
 * Smart study workspace UI foundation.
 *
 * Draft notes are deliberately session-only until the existing offline study
 * database gains a verified passage-reference workflow and complete CRUD APIs.
 * This screen never fabricates verse references or claims durable storage.
 */
@Composable
fun SmartStudyWorkspaceScreen(modifier: Modifier = Modifier) {
    var section by remember { mutableStateOf(StudySection.NOTES) }
    var draft by remember { mutableStateOf("") }
    val sessionNotes = remember { mutableStateListOf<String>() }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        val wide = maxWidth >= 640.dp
        Column(
            modifier = Modifier
                .widthIn(max = 900.dp)
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (wide) 32.dp else 18.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "YOUR STUDY SPACE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Study deeper. Keep what matters.",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "A personal workspace for reflections, saved passages and discoveries.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            StudyStatusCard()

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
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("NEW REFLECTION", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = draft,
                                onValueChange = { draft = it },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 4,
                                placeholder = { Text("Write your thought, question or prayer…") },
                                supportingText = { Text("Session draft only • no verified passage attached") }
                            )
                            Button(
                                onClick = {
                                    val value = draft.trim()
                                    if (value.isNotEmpty()) {
                                        sessionNotes.add(0, value)
                                        draft = ""
                                    }
                                },
                                enabled = draft.isNotBlank(),
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Keep in this session") }
                        }
                    }
                    SectionIntro("This session", if (sessionNotes.isEmpty()) "Your reflections will appear here." else "${sessionNotes.size} reflection(s) in this session")
                    if (sessionNotes.isEmpty()) {
                        EmptyStudyCard("No reflections yet", "Start with a question, a personal insight or something you want to revisit.")
                    } else {
                        sessionNotes.forEachIndexed { index, note ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("REFLECTION ${sessionNotes.size - index}", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                                    Text(note, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        OutlinedButton(onClick = { sessionNotes.removeAt(index) }) { Text("Remove") }
                                    }
                                }
                            }
                        }
                    }
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
                    "Organize bookmarks, notes and highlights into themes such as family, wisdom, prayer and personal study once durable study records are available.",
                    "COLLECTION STORAGE NOT CONNECTED"
                )
            }

            Text(
                "LOCAL STUDY • PRIVATE WORKSPACE FOUNDATION",
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StudyStatusCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(Modifier.size(10.dp).background(MaterialTheme.colorScheme.secondary, CircleShape))
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("STUDY WORKSPACE FOUNDATION", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                Text("Notes are session-only in this UI stage. Durable storage and passage linking remain gated.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
    }
}

@Composable
private fun StudyTab(item: StudySection, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    if (selected) {
        Button(onClick = onClick, modifier = modifier) { Text(item.title, maxLines = 1) }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) { Text(item.title, maxLines = 1) }
    }
}

@Composable
private fun SectionIntro(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyStudyCard(title: String, message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text("✧", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LockedStudyFeature(title: String, description: String, status: String) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionIntro(title, description)
        EmptyStudyCard("Not connected yet", "This area is intentionally inactive until verified passage identities and durable study operations are available.")
        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) {
            Text(
                status,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
