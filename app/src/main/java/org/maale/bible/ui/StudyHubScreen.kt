package org.maale.bible.ui

import org.maale.bible.R

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.maale.bible.data.catalog.CanonicalBookCatalog
import org.maale.bible.domain.model.AnnotationKind
import org.maale.bible.domain.model.completionPercent
import org.maale.bible.domain.repository.StudyRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun StudyHubScreen(repository: StudyRepository, onOpenBook: (String) -> Unit, modifier: Modifier = Modifier) {
    var refresh by remember { mutableIntStateOf(0) }
    val bookmarks = remember(refresh) { repository.getBookmarks() }
    val annotations = remember(refresh) { repository.getAnnotations() }
    val plans = remember(refresh) { repository.getPlans() }
    val notesCount = annotations.count { it.kind == AnnotationKind.NOTE }
    val highlightsCount = annotations.count { it.kind == AnnotationKind.HIGHLIGHT }

    LazyColumn(
        modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(stringResource(R.string.study_title), style = MaterialTheme.typography.headlineSmall)
                    Text(stringResource(R.string.study_private_body), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text(
                        LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault())),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
        item { StudyWeekContextCard() }
        item { StudyInsightsChart(bookmarks.size, notesCount, highlightsCount, plans.size) }
        item { BiblePlacesPreviewCard() }
        item { Text(stringResource(R.string.study_plans, plans.size), style = MaterialTheme.typography.titleMedium) }
        items(plans, key = { "plan-${it.id}" }) { plan ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(plan.title, style = MaterialTheme.typography.titleMedium)
                    Text("${plan.completedDays.size} of ${plan.durationDays} days · ${plan.completionPercent()}%")
                    LinearProgressIndicator(progress = { plan.completionPercent() / 100f }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
        item {
            Text(
                stringResource(R.string.study_bookmarks, bookmarks.size),
                modifier = Modifier.testTag("study_bookmarks_count"),
                style = MaterialTheme.typography.titleMedium
            )
        }
        items(bookmarks, key = { "bookmark-${it.id}" }) { bookmark ->
            val name = CanonicalBookCatalog.find(bookmark.bookId)?.englishName ?: bookmark.bookId
            ListItem(
                headlineContent = { Text(name) },
                supportingContent = { Text("Chapter ${bookmark.chapter}, verse ${bookmark.verse}") },
                trailingContent = { TextButton(onClick = { repository.removeBookmark(bookmark.id); refresh++ }) { Text(stringResource(R.string.reader_remove)) } },
                modifier = Modifier.semantics { contentDescription = "Open bookmark in $name" }
            )
            HorizontalDivider()
        }
        item { Text(stringResource(R.string.study_annotations, annotations.size), style = MaterialTheme.typography.titleMedium) }
        items(annotations, key = { "annotation-${it.id}" }) { annotation ->
            val name = CanonicalBookCatalog.find(annotation.bookId)?.englishName ?: annotation.bookId
            ListItem(
                headlineContent = { Text("$name · ${annotation.kind}") },
                supportingContent = { Text("Chapter ${annotation.chapter}, verse ${annotation.verse}: ${annotation.body}") },
                modifier = Modifier.semantics { contentDescription = "Open annotation in $name" }
            )
            HorizontalDivider()
        }
        if (bookmarks.isEmpty() && annotations.isEmpty() && plans.isEmpty()) {
            item { Text(stringResource(R.string.study_empty), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun StudyWeekContextCard() {
    val today = LocalDate.now()
    val days = (6 downTo 0).map { today.minusDays(it.toLong()) }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.study_calendar_title), style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                days.forEach { day ->
                    val isToday = day == today
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(day.dayOfWeek.name.take(1), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Surface(
                            shape = CircleShape,
                            color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent
                        ) {
                            Text(
                                day.dayOfMonth.toString(),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            Text(stringResource(R.string.study_calendar_today), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun StudyInsightsChart(bookmarks: Int, notes: Int, highlights: Int, plans: Int) {
    val metrics = listOf(
        Triple(stringResource(R.string.study_metric_bookmarks), bookmarks, MaterialTheme.colorScheme.primary),
        Triple(stringResource(R.string.study_metric_notes), notes, MaterialTheme.colorScheme.secondary),
        Triple(stringResource(R.string.study_metric_highlights), highlights, MaterialTheme.colorScheme.tertiary),
        Triple(stringResource(R.string.study_metric_plans), plans, MaterialTheme.colorScheme.error)
    )
    val maxValue = (metrics.maxOfOrNull { it.second } ?: 0).coerceAtLeast(1)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.study_insights_title), style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                metrics.forEach { (label, value, color) ->
                    Column(
                        Modifier.weight(1f).semantics {
                            contentDescription = "$label: $value"
                        },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(value.toString(), style = MaterialTheme.typography.titleMedium)
                        Box(
                            Modifier.width(30.dp).height((72f * value / maxValue).coerceAtLeast(5f).dp)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(color)
                        )
                        Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun BiblePlacesPreviewCard() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            MaaleBibleDecorativeMapSurface(Modifier.size(width = 112.dp, height = 92.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(stringResource(R.string.study_places_title), style = MaterialTheme.typography.titleSmall)
                Text(stringResource(R.string.study_places_body), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.study_places_status), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
