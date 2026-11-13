package org.maale.bible.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import org.maale.bible.data.catalog.CanonicalBookCatalog
import org.maale.bible.domain.model.*
import org.maale.bible.domain.repository.StudyRepository
import java.util.UUID
import java.time.LocalDate

@Composable
fun ReaderPlaceholderScreen(bookId: String, repository: StudyRepository, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val book = CanonicalBookCatalog.find(bookId)
    var navigation by rememberSaveable(bookId) {
        mutableStateOf(repository.getReadingPosition()?.takeIf { it.bookId == bookId }
            ?.let { ReaderNavigation(it.bookId, it.chapter, it.verse) } ?: ReaderNavigation(bookId))
    }
    var noteText by rememberSaveable(bookId) { mutableStateOf("") }
    var editingAnnotationId by rememberSaveable(bookId) { mutableStateOf<String?>(null) }
    var planTitle by rememberSaveable { mutableStateOf("") }
    var refresh by remember { mutableIntStateOf(0) }
    LaunchedEffect(navigation) {
        repository.saveReadingPosition(ReadingPosition(navigation.bookId, navigation.chapter, navigation.verse, System.currentTimeMillis()))
    }
    val bookmarks = remember(refresh, bookId) { repository.getBookmarks(bookId) }
    val annotations = remember(refresh, bookId) { repository.getAnnotations(bookId) }
    val plans = remember(refresh) { repository.getPlans() }

    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(book?.englishName ?: bookId, style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onBack) { Text("Books") }
        }
        Text("Reading position", style = MaterialTheme.typography.titleMedium)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Chapter ${navigation.chapter} · Verse ${navigation.verse}", style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.reader_coordinates_notice))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(onClick = { navigation = navigation.previousChapter() }) { Text("Previous chapter") }
                    Button(onClick = { navigation = navigation.nextChapter() }) { Text("Next chapter") }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(onClick = { navigation = navigation.previousVerse() }) { Text("Previous verse") }
                    Button(onClick = { navigation = navigation.nextVerse() }) { Text("Next verse") }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                repository.addBookmark(Bookmark(UUID.randomUUID().toString(), bookId, navigation.chapter, navigation.verse, System.currentTimeMillis()))
                refresh++
            }) { Text("Add bookmark") }
            OutlinedButton(onClick = {
                val now = System.currentTimeMillis()
                repository.saveAnnotation(StudyAnnotation(UUID.randomUUID().toString(), bookId, navigation.chapter, navigation.verse, AnnotationKind.HIGHLIGHT, "", "yellow", now, now))
                refresh++
            }) { Text("Highlight position") }
        }
        HorizontalDivider()
        Text("Personal notes", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = noteText, onValueChange = { noteText = it }, label = { Text("Write a private note") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        Button(onClick = {
            if (noteText.isNotBlank()) {
                val now = System.currentTimeMillis()
                val existing = editingAnnotationId?.let { id -> repository.getAnnotations(bookId).firstOrNull { it.id == id } }
                repository.saveAnnotation(StudyAnnotation(existing?.id ?: UUID.randomUUID().toString(), bookId, navigation.chapter, navigation.verse, AnnotationKind.NOTE, noteText.trim(), existing?.color, existing?.createdAtEpochMs ?: now, now))
                noteText = ""; editingAnnotationId = null; refresh++
            }
        }, enabled = noteText.isNotBlank()) { Text(if (editingAnnotationId == null) "Save note" else "Update note") }
        Text("Bookmarks (${bookmarks.size})", style = MaterialTheme.typography.titleMedium)
        bookmarks.forEach { item ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Chapter ${item.chapter}, verse ${item.verse}")
                TextButton(onClick = { navigation = ReaderNavigation(bookId, item.chapter, item.verse) }) { Text("Go to") }
                TextButton(onClick = { repository.removeBookmark(item.id); refresh++ }) { Text("Remove") }
            }
        }
        Text("Highlights and notes (${annotations.size})", style = MaterialTheme.typography.titleMedium)
        annotations.forEach { item ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("${item.kind}: ${item.chapter}:${item.verse} ${item.body}")
                    if (item.kind == AnnotationKind.NOTE) TextButton(onClick = { noteText = item.body; editingAnnotationId = item.id; navigation = ReaderNavigation(bookId, item.chapter, item.verse) }) { Text("Edit") }
                }
                TextButton(onClick = { repository.deleteAnnotation(item.id); refresh++ }) { Text("Delete") }
            }
        }
        HorizontalDivider()
        Text("Reading plans", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = planTitle, onValueChange = { planTitle = it }, label = { Text("New reading plan") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            val now = System.currentTimeMillis()
            repository.savePlan(ReadingPlan(UUID.randomUUID().toString(), planTitle.trim(), LocalDate.now().toEpochDay(), 7, emptySet(), now))
            planTitle = ""; refresh++
        }, enabled = planTitle.isNotBlank()) { Text("Create 7-day plan") }
        plans.forEach { plan ->
            Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text("${plan.title}: ${plan.completedDays.size}/${plan.durationDays} days · ${plan.completionPercent()}%", style = MaterialTheme.typography.titleSmall)
                LinearProgressIndicator(progress = { plan.completionPercent() / 100f }, modifier = Modifier.fillMaxWidth())
                val dueDay = plan.schedule(LocalDate.now().toEpochDay()).firstOrNull { it.status == PlanDayStatus.DUE || it.status == PlanDayStatus.OVERDUE }
                TextButton(onClick = {
                    if (dueDay != null) repository.savePlan(plan.withDayCompleted(dueDay.dayNumber, System.currentTimeMillis()))
                    refresh++
                }, enabled = dueDay != null) { Text(if (dueDay == null) "No day due today" else "Complete day ${dueDay.dayNumber}") }
            }
        }
        Text("Study data is stored locally and protected by the app's encrypted database.", style = MaterialTheme.typography.bodySmall)
    }
}
