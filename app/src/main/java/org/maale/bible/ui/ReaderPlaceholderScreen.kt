package org.maale.bible.ui

import org.maale.bible.R

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import org.maale.bible.data.catalog.CanonicalBookCatalog
import org.maale.bible.data.scripture.ScriptureChapter
import org.maale.bible.data.scripture.ScripturePackageManager
import org.maale.bible.data.scripture.ScripturePackageReader
import org.maale.bible.domain.model.*
import org.maale.bible.domain.repository.StudyRepository
import java.util.UUID
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ReaderPlaceholderScreen(bookId: String, repository: StudyRepository, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val book = CanonicalBookCatalog.find(bookId)
    val context = LocalContext.current
    val packageManager = remember(context) { ScripturePackageManager(context) }
    val scope = rememberCoroutineScope()
    var packageReader by remember { mutableStateOf<ScripturePackageReader?>(null) }
    var packageStatus by rememberSaveable(bookId) { mutableStateOf<String?>(null) }
    var packageImporting by remember { mutableStateOf(false) }
    val packagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                packageImporting = true
                try {
                    val installedReader = withContext(Dispatchers.IO) {
                        packageReader?.close()
                        packageManager.installFromUri(uri)
                        packageManager.openInstalled()
                    }
                    packageReader = installedReader
                    packageStatus = context.getString(R.string.reader_package_imported)
                } catch (_: Exception) {
                    packageStatus = context.getString(R.string.reader_package_import_failed)
                } finally {
                    packageImporting = false
                }
            }
        }
    }
    LaunchedEffect(packageManager) {
        val result = withContext(Dispatchers.IO) {
            runCatching { packageManager.openInstalled() }
        }
        packageReader = result.getOrNull()
        if (result.isFailure) packageStatus = context.getString(R.string.reader_package_import_failed)
    }
    DisposableEffect(packageReader) {
        onDispose { packageReader?.close() }
    }
    var navigation by remember(bookId) {
        mutableStateOf(repository.getReadingPosition()?.takeIf { it.bookId == bookId }
            ?.let { ReaderNavigation(it.bookId, it.chapter, it.verse) } ?: ReaderNavigation(bookId))
    }
    var chapterContent by remember(bookId) { mutableStateOf<ScriptureChapter?>(null) }
    LaunchedEffect(packageReader, bookId, navigation.chapter) {
        chapterContent = withContext(Dispatchers.IO) {
            runCatching { packageReader?.loadChapter(bookId, navigation.chapter) }.getOrNull()
        }
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

    Column(modifier.fillMaxSize().testTag("reader_screen").verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(book?.englishName ?: bookId, style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onBack) { Text(stringResource(R.string.nav_books)) }
        }
        Text(stringResource(R.string.reader_position), style = MaterialTheme.typography.titleMedium)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Chapter ${navigation.chapter} · Verse ${navigation.verse}", modifier = Modifier.testTag("reader_current_position"), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.reader_coordinates_notice))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(onClick = { navigation = navigation.previousChapter() }) { Text(stringResource(R.string.reader_previous_chapter)) }
                    Button(onClick = { navigation = navigation.nextChapter() }) { Text(stringResource(R.string.reader_next_chapter)) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(onClick = { navigation = navigation.previousVerse() }) { Text(stringResource(R.string.reader_previous_verse)) }
                    Button(onClick = { navigation = navigation.nextVerse() }) { Text(stringResource(R.string.reader_next_verse)) }
                }
            }
        }
        Card(Modifier.fillMaxWidth().testTag("scripture_content_card")) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.reader_chapter_title, navigation.chapter), style = MaterialTheme.typography.titleLarge)
                if (chapterContent == null) {
                    Text(stringResource(R.string.reader_content_not_loaded))
                    if (packageManager.isDebuggableBuild()) {
                        Button(
                            onClick = { packagePicker.launch(arrayOf("*/*")) },
                            enabled = !packageImporting,
                            modifier = Modifier.fillMaxWidth().testTag("reader_import_scripture_package")
                        ) {
                            Text(stringResource(if (packageImporting) R.string.reader_package_importing else R.string.reader_import_package))
                        }
                        Text(stringResource(R.string.reader_package_preview_warning), style = MaterialTheme.typography.bodySmall)
                    }
                    packageStatus?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                } else if (chapterContent?.verses.isNullOrEmpty()) {
                    Text(stringResource(R.string.reader_chapter_content_missing))
                    if (packageManager.isDebuggableBuild()) {
                        OutlinedButton(onClick = { packagePicker.launch(arrayOf("*/*")) }) {
                            Text(stringResource(R.string.reader_import_package))
                        }
                    }
                } else {
                    Text(stringResource(R.string.reader_package_preview_warning), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    chapterContent?.headings?.forEach { heading ->
                        Text(heading.text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    }
                    chapterContent?.verses?.forEach { verse ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(verse.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            Text(verse.text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    HorizontalDivider()
                    Text(stringResource(R.string.reader_package_attribution), style = MaterialTheme.typography.bodySmall)
                    Text(stringResource(R.string.reader_package_license), style = MaterialTheme.typography.labelSmall)
                    packageStatus?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(modifier = Modifier.testTag("reader_add_bookmark_button"), onClick = {
                val bookmark = Bookmark(UUID.randomUUID().toString(), bookId, navigation.chapter, navigation.verse, System.currentTimeMillis())
                repository.addBookmark(bookmark)
                refresh++
            }) { Text(stringResource(R.string.reader_add_bookmark)) }
            OutlinedButton(onClick = {
                val now = System.currentTimeMillis()
                repository.saveAnnotation(StudyAnnotation(UUID.randomUUID().toString(), bookId, navigation.chapter, navigation.verse, AnnotationKind.HIGHLIGHT, "", "yellow", now, now))
                refresh++
            }) { Text(stringResource(R.string.reader_highlight)) }
        }
        HorizontalDivider()
        Text(stringResource(R.string.reader_notes), style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = noteText, onValueChange = { noteText = it }, label = { Text(stringResource(R.string.reader_note_hint)) }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        Button(onClick = {
            if (noteText.isNotBlank()) {
                val now = System.currentTimeMillis()
                val existing = editingAnnotationId?.let { id -> repository.getAnnotations(bookId).firstOrNull { it.id == id } }
                repository.saveAnnotation(StudyAnnotation(existing?.id ?: UUID.randomUUID().toString(), bookId, navigation.chapter, navigation.verse, AnnotationKind.NOTE, noteText.trim(), existing?.color, existing?.createdAtEpochMs ?: now, now))
                noteText = ""; editingAnnotationId = null; refresh++
            }
        }, enabled = noteText.isNotBlank()) { Text(stringResource(if (editingAnnotationId == null) R.string.reader_save_note else R.string.reader_update_note)) }
        Text(stringResource(R.string.reader_bookmarks, bookmarks.size), style = MaterialTheme.typography.titleMedium)
        bookmarks.forEach { item ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Chapter ${item.chapter}, verse ${item.verse}")
                TextButton(onClick = { navigation = ReaderNavigation(bookId, item.chapter, item.verse) }) { Text(stringResource(R.string.reader_go_to)) }
                TextButton(onClick = { repository.removeBookmark(item.id); refresh++ }) { Text(stringResource(R.string.reader_remove)) }
            }
        }
        Text(stringResource(R.string.reader_annotations, annotations.size), style = MaterialTheme.typography.titleMedium)
        annotations.forEach { item ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("${item.kind}: ${item.chapter}:${item.verse} ${item.body}")
                    if (item.kind == AnnotationKind.NOTE) TextButton(onClick = { noteText = item.body; editingAnnotationId = item.id; navigation = ReaderNavigation(bookId, item.chapter, item.verse) }) { Text(stringResource(R.string.reader_edit)) }
                }
                TextButton(onClick = { repository.deleteAnnotation(item.id); refresh++ }) { Text(stringResource(R.string.reader_delete)) }
            }
        }
        HorizontalDivider()
        Text(stringResource(R.string.reader_plans), style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = planTitle, onValueChange = { planTitle = it }, label = { Text(stringResource(R.string.reader_new_plan)) }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            val now = System.currentTimeMillis()
            repository.savePlan(ReadingPlan(UUID.randomUUID().toString(), planTitle.trim(), LocalDate.now().toEpochDay(), 7, emptySet(), now))
            planTitle = ""; refresh++
        }, enabled = planTitle.isNotBlank()) { Text(stringResource(R.string.reader_create_plan)) }
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
