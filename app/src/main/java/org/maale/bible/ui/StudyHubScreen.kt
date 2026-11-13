package org.maale.bible.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import org.maale.bible.data.catalog.CanonicalBookCatalog
import org.maale.bible.domain.model.completionPercent
import org.maale.bible.domain.repository.StudyRepository

@Composable
fun StudyHubScreen(repository: StudyRepository, onOpenBook: (String) -> Unit, modifier: Modifier = Modifier) {
    var refresh by remember { mutableIntStateOf(0) }
    val bookmarks = remember(refresh) { repository.getBookmarks() }
    val annotations = remember(refresh) { repository.getAnnotations() }
    val plans = remember(refresh) { repository.getPlans() }
    LazyColumn(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text(stringResource(R.string.study_title), style = MaterialTheme.typography.headlineSmall) }
        item { Text("Your private reading activity, saved locally.", style = MaterialTheme.typography.bodyMedium) }
        item { Text(stringResource(R.string.study_plans, plans.size), style = MaterialTheme.typography.titleMedium) }
        items(plans, key = { "plan-${it.id}" }) { plan ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(plan.title, style = MaterialTheme.typography.titleMedium)
                    Text("${plan.completedDays.size} of ${plan.durationDays} days · ${plan.completionPercent()}%")
                    LinearProgressIndicator(progress = { plan.completionPercent() / 100f }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
        item { Text(stringResource(R.string.study_bookmarks, bookmarks.size), style = MaterialTheme.typography.titleMedium) }
        items(bookmarks, key = { "bookmark-${it.id}" }) { bookmark ->
            val name = CanonicalBookCatalog.find(bookmark.bookId)?.englishName ?: bookmark.bookId
            ListItem(
                headlineContent = { Text(name) },
                supportingContent = { Text("Chapter ${bookmark.chapter}, verse ${bookmark.verse}") },
                trailingContent = { TextButton(onClick = { repository.removeBookmark(bookmark.id); refresh++ }) { Text(stringResource(R.string.reader_remove)) } },
                modifier = Modifier.clickable { onOpenBook(bookmark.bookId) }.semantics { contentDescription = "Open bookmark in $name" }
            )
            HorizontalDivider()
        }
        item { Text(stringResource(R.string.study_annotations, annotations.size), style = MaterialTheme.typography.titleMedium) }
        items(annotations, key = { "annotation-${it.id}" }) { annotation ->
            val name = CanonicalBookCatalog.find(annotation.bookId)?.englishName ?: annotation.bookId
            ListItem(
                headlineContent = { Text("$name · ${annotation.kind}") },
                supportingContent = { Text("Chapter ${annotation.chapter}, verse ${annotation.verse}: ${annotation.body}") },
                modifier = Modifier.clickable { onOpenBook(annotation.bookId) }
            )
            HorizontalDivider()
        }
        if (bookmarks.isEmpty() && annotations.isEmpty() && plans.isEmpty()) {
            item { Text(stringResource(R.string.study_empty)) }
        }
    }
}
