package org.maale.bible.ui

import org.maale.bible.R

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import org.maale.bible.data.scripture.ScriptureChapter
import org.maale.bible.data.scripture.ScriptureEditionCatalog
import org.maale.bible.data.scripture.ScriptureChapterResult
import org.maale.bible.data.scripture.ScripturePackageRepository
import org.maale.bible.domain.model.*
import org.maale.bible.domain.repository.StudyRepository
import java.util.UUID
import java.time.LocalDate

@Composable
fun ReaderPlaceholderScreen(bookId: String, repository: StudyRepository, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var navigation by remember(bookId) {
        mutableStateOf(repository.getReadingPosition()?.takeIf { it.bookId == bookId }
            ?.let { ReaderNavigation(it.bookId, it.chapter, it.verse) } ?: ReaderNavigation(bookId))
    }
    var selectedEditionId by rememberSaveable(bookId) { mutableStateOf(ScriptureEditionCatalog.PRIMARY_MAALE_ID) }
    var editionMenuExpanded by remember { mutableStateOf(false) }
    val selectedEdition = ScriptureEditionCatalog.find(selectedEditionId)
        ?: ScriptureEditionCatalog.editions.first()
    val scriptureResult = remember(context, bookId, navigation.chapter, selectedEditionId) {
        ScripturePackageRepository.loadChapter(context, bookId, navigation.chapter, selectedEditionId)
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
            Text(stringResource(bookNameResource(bookId)), style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onBack) { Text(stringResource(R.string.reader_back_to_books)) }
        }
        Text(stringResource(R.string.reader_position), style = MaterialTheme.typography.titleMedium)
        Box {
            OutlinedButton(onClick = { editionMenuExpanded = true }, modifier = Modifier.fillMaxWidth().testTag("reader_edition_selector")) {
                Text("${selectedEdition.languageName}: ${selectedEdition.title}")
            }
            DropdownMenu(expanded = editionMenuExpanded, onDismissRequest = { editionMenuExpanded = false }) {
                ScriptureEditionCatalog.editions.forEach { edition ->
                    DropdownMenuItem(
                        text = { Column { Text(edition.title); Text("${edition.languageName} · ${edition.status.name}", style = MaterialTheme.typography.labelSmall) } },
                        modifier = Modifier.testTag("reader_edition_${edition.id}"),
                        onClick = { selectedEditionId = edition.id; editionMenuExpanded = false }
                    )
                }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.reader_position_coordinates, navigation.chapter, navigation.verse), modifier = Modifier.testTag("reader_current_position"), style = MaterialTheme.typography.titleLarge)
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
        when (val result = scriptureResult) {
            is ScriptureChapterResult.Available -> ScriptureChapterCard(result.chapter)
            ScriptureChapterResult.PackageNotInstalled -> ReaderScriptureNotice(
                stringResource(R.string.reader_scripture_not_installed_title),
                stringResource(R.string.reader_scripture_not_installed_body)
            )
            ScriptureChapterResult.PreviewOnly -> ReaderScriptureNotice(
                stringResource(R.string.reader_scripture_preview_title),
                stringResource(R.string.reader_scripture_preview_body)
            )
            ScriptureChapterResult.ChapterUnavailable -> ReaderScriptureNotice(
                stringResource(R.string.reader_scripture_chapter_missing_title),
                stringResource(R.string.reader_scripture_chapter_missing_body)
            )
            ScriptureChapterResult.InvalidPackage -> ReaderScriptureNotice(
                stringResource(R.string.reader_scripture_invalid_title),
                stringResource(R.string.reader_scripture_invalid_body)
            )
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
                Text(stringResource(R.string.reader_bookmark_coordinates, item.chapter, item.verse))
                TextButton(onClick = { navigation = ReaderNavigation(bookId, item.chapter, item.verse) }) { Text(stringResource(R.string.reader_go_to)) }
                TextButton(onClick = { repository.removeBookmark(item.id); refresh++ }) { Text(stringResource(R.string.reader_remove)) }
            }
        }
        Text(stringResource(R.string.reader_annotations, annotations.size), style = MaterialTheme.typography.titleMedium)
        annotations.forEach { item ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.reader_annotation_coordinates, stringResource(if (item.kind == AnnotationKind.NOTE) R.string.reader_annotation_note_kind else R.string.reader_annotation_highlight_kind), item.chapter, item.verse, item.body))
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
                Text(stringResource(R.string.reader_plan_progress, plan.title, plan.completedDays.size, plan.durationDays, plan.completionPercent()), style = MaterialTheme.typography.titleSmall)
                LinearProgressIndicator(progress = { plan.completionPercent() / 100f }, modifier = Modifier.fillMaxWidth())
                val dueDay = plan.schedule(LocalDate.now().toEpochDay()).firstOrNull { it.status == PlanDayStatus.DUE || it.status == PlanDayStatus.OVERDUE }
                TextButton(onClick = {
                    if (dueDay != null) repository.savePlan(plan.withDayCompleted(dueDay.dayNumber, System.currentTimeMillis()))
                    refresh++
                }, enabled = dueDay != null) { Text(if (dueDay == null) stringResource(R.string.reader_no_day_due) else stringResource(R.string.reader_complete_day, dueDay.dayNumber)) }
            }
        }
        Text(stringResource(R.string.reader_private_notice), style = MaterialTheme.typography.bodySmall)
    }
}


@Composable
private fun ScriptureChapterCard(chapter: ScriptureChapter) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("scripture_chapter_content"),
        shape = MaterialTheme.shapes.large
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                stringResource(R.string.reader_scripture_chapter_title, chapter.chapterNumber),
                style = MaterialTheme.typography.titleLarge
            )
            chapter.headings.forEach { heading ->
                Text(heading.text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 560.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(chapter.verses, key = { it.label }) { verse ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(verse.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text(verse.text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderScriptureNotice(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("scripture_package_status"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
