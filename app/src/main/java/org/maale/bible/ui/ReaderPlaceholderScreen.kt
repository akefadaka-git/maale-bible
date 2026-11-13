package org.maale.bible.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.maale.bible.data.catalog.CanonicalBookCatalog
import org.maale.bible.domain.model.*
import org.maale.bible.domain.repository.StudyRepository
import java.util.UUID
import java.time.LocalDate

@Composable
fun ReaderScreen(bookId: String, repository: StudyRepository, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val book = CanonicalBookCatalog.find(bookId)
    var navigation by remember(bookId) {
        mutableStateOf(repository.getReadingPosition()?.takeIf { it.bookId == bookId }
            ?.let { ReaderNavigation(it.bookId,it.chapter,it.verse) } ?: ReaderNavigation(bookId))
    }
    var noteText by remember { mutableStateOf("") }
    var editingAnnotationId by remember { mutableStateOf<String?>(null) }
    var planTitle by remember { mutableStateOf("") }
    var refresh by remember { mutableIntStateOf(0) }
    LaunchedEffect(navigation) {
        repository.saveReadingPosition(ReadingPosition(navigation.bookId,navigation.chapter,navigation.verse,System.currentTimeMillis()))
    }
    val bookmarks = remember(refresh,bookId) { repository.getBookmarks(bookId) }
    val annotations = remember(refresh,bookId) { repository.getAnnotations(bookId) }
    val plans = remember(refresh) { repository.getPlans() }

    Column(modifier.fillMaxSize().padding(16.dp)) {
        Text(book?.englishName ?: bookId, style = MaterialTheme.typography.headlineSmall)
        Text("Reader coordinates — scripture content is not included", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Chapter ${navigation.chapter} · Verse ${navigation.verse}", style = MaterialTheme.typography.titleLarge)
                Text("Coordinate navigation only. Actual chapter and verse boundaries will be supplied by an approved scripture package.")
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
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                repository.addBookmark(Bookmark(UUID.randomUUID().toString(),bookId,navigation.chapter,navigation.verse,System.currentTimeMillis()))
                refresh++
            }) { Text("Bookmark") }
            OutlinedButton(onClick = {
                repository.saveAnnotation(StudyAnnotation(UUID.randomUUID().toString(),bookId,navigation.chapter,navigation.verse,AnnotationKind.HIGHLIGHT,"", "yellow",System.currentTimeMillis(),System.currentTimeMillis()))
                refresh++
            }) { Text("Highlight") }
        }
        OutlinedTextField(value=noteText,onValueChange={noteText=it},label={Text("Personal note")},modifier=Modifier.fillMaxWidth())
        Button(onClick = {
            if(noteText.isNotBlank()) {
                val now=System.currentTimeMillis()
                val existing = editingAnnotationId?.let { id -> repository.getAnnotations(bookId).firstOrNull { it.id == id } }
                repository.saveAnnotation(StudyAnnotation(existing?.id ?: UUID.randomUUID().toString(),bookId,navigation.chapter,navigation.verse,AnnotationKind.NOTE,noteText,existing?.color,existing?.createdAtEpochMs ?: now,now))
                noteText=""; editingAnnotationId=null; refresh++
            }
        },enabled=noteText.isNotBlank()) { Text("Save note") }
        Divider(Modifier.padding(vertical=6.dp))
        Text("Bookmarks (${bookmarks.size})",style=MaterialTheme.typography.titleMedium)
        LazyColumn(Modifier.heightIn(max=100.dp)) {
            items(bookmarks,key={it.id}) { item ->
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                    Text("Chapter ${item.chapter}, verse ${item.verse}")
                    TextButton(onClick={repository.removeBookmark(item.id);refresh++}) { Text("Remove") }
                }
            }
        }
        Text("Highlights and notes (${annotations.size})",style=MaterialTheme.typography.titleMedium)
        LazyColumn(Modifier.heightIn(max=110.dp)) {
            items(annotations,key={it.id}) { item ->
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("${item.kind}: chapter ${item.chapter}, verse ${item.verse} ${item.body}")
                        if (item.kind == AnnotationKind.NOTE) TextButton(onClick={noteText=item.body;editingAnnotationId=item.id}) { Text("Edit") }
                    }
                    TextButton(onClick={repository.deleteAnnotation(item.id);if(editingAnnotationId==item.id){editingAnnotationId=null;noteText=""};refresh++}) { Text("Delete") }
                }
            }
        }
        Divider(Modifier.padding(vertical=6.dp))
        Text("Reading plans",style=MaterialTheme.typography.titleMedium)
        OutlinedTextField(value=planTitle,onValueChange={planTitle=it},label={Text("New 7-day plan")},modifier=Modifier.fillMaxWidth())
        Button(onClick={
            val now=System.currentTimeMillis()
            repository.savePlan(ReadingPlan(UUID.randomUUID().toString(),planTitle.trim(),LocalDate.now().toEpochDay(),7,emptySet(),now))
            planTitle="";refresh++
        },enabled=planTitle.isNotBlank()) { Text("Create plan") }
        plans.forEach { plan ->
            Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text("${plan.title}: ${plan.completedDays.size}/${plan.durationDays} days · ${plan.completionPercent()}%", style = MaterialTheme.typography.titleSmall)
                val today = LocalDate.now().toEpochDay()
                val schedule = plan.schedule(today)
                schedule.take(7).forEach { day ->
                    Text("Day ${day.dayNumber} · ${LocalDate.ofEpochDay(day.dueEpochDay)} · ${day.status.name.lowercase()}", style = MaterialTheme.typography.bodySmall)
                }
                val dueDay = schedule.firstOrNull { it.status == PlanDayStatus.DUE || it.status == PlanDayStatus.OVERDUE }
                TextButton(onClick={
                    if (dueDay != null) repository.savePlan(plan.withDayCompleted(dueDay.dayNumber, System.currentTimeMillis()))
                    refresh++
                },enabled=dueDay != null) { Text(if (dueDay == null) "No day due today" else "Complete day ${dueDay.dayNumber}") }
            }
        }
        Spacer(Modifier.weight(1f))
        Text("Personal study data is stored locally on this device.",style=MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick=onBack) { Text("Back to books") }
    }
}
