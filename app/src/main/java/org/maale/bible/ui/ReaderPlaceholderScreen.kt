package org.maale.bible.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maale.bible.R
import org.maale.bible.data.catalog.CanonicalBookCatalog
import org.maale.bible.data.content.OfflineScriptureStore
import org.maale.bible.data.content.ScriptureSearchResult

@Composable
fun ReaderPlaceholderScreen(
    bookId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    initialChapter: Int = 1,
    initialVerseRef: String? = null
) {
    val context = LocalContext.current.applicationContext
    val book = CanonicalBookCatalog.find(bookId)
    var readingFontSize by remember { mutableFloatStateOf(20f) }
    var focusMode by remember { mutableStateOf(false) }
    var chapter by remember(bookId) { mutableIntStateOf(initialChapter.coerceAtLeast(1)) }
    var chapterCount by remember(bookId) { mutableIntStateOf(0) }
    var store by remember(bookId) { mutableStateOf<OfflineScriptureStore?>(null) }
    var verses by remember(bookId) { mutableStateOf<List<ScriptureSearchResult>>(emptyList()) }
    var loading by remember(bookId) { mutableStateOf(true) }

    LaunchedEffect(context, bookId) {
        val opened = withContext(Dispatchers.IO) {
            runCatching {
                OfflineScriptureStore.openInstalled(context, "mdyeth")?.let { it to it.chapterCount(bookId) }
            }
        }
        opened.onSuccess { result ->
            store = result?.first
            chapterCount = result?.second ?: 0
            loading = false
            if (chapterCount > 0 && chapter > chapterCount) chapter = chapterCount
        }.onFailure {
            store = null
            chapterCount = 0
            loading = false
        }
    }

    DisposableEffect(store) {
        onDispose { store?.close() }
    }

    LaunchedEffect(store, chapter) {
        val active = store
        verses = withContext(Dispatchers.IO) {
            runCatching { active?.chapter(bookId, chapter).orEmpty() }.getOrDefault(emptyList())
        }
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (!focusMode) ReaderTopBar(book?.englishName ?: bookId, onBack)
        Column(
            modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth()
                .align(Alignment.CenterHorizontally).weight(1f)
                .padding(horizontal = 20.dp, vertical = if (focusMode) 28.dp else 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!focusMode) {
                ReaderBookIdentity(book?.englishName ?: bookId, book?.usfmId ?: bookId)
                ReaderControlPanel(
                    fontSize = readingFontSize,
                    onDecreaseFont = { readingFontSize = (readingFontSize - 1f).coerceAtLeast(16f) },
                    onIncreaseFont = { readingFontSize = (readingFontSize + 1f).coerceAtMost(32f) },
                    onToggleFocus = { focusMode = true }
                )
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = { focusMode = false }) { Text(stringResource(R.string.reader_exit_focus)) }
                }
            }
            when {
                loading -> Text(stringResource(R.string.reader_loading_content), color = MaterialTheme.colorScheme.onSurfaceVariant)
                store == null -> ScriptureUnavailablePanel(fontSize = readingFontSize)
                chapterCount == 0 -> Text(stringResource(R.string.reader_book_not_in_edition), color = MaterialTheme.colorScheme.onSurfaceVariant)
                else -> {
                    ReaderChapterNavigation(chapter, chapterCount,
                        onPrevious = { chapter = (chapter - 1).coerceAtLeast(1) },
                        onNext = { chapter = (chapter + 1).coerceAtMost(chapterCount) })
                    if (verses.isEmpty()) {
                        Text(stringResource(R.string.reader_chapter_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        LazyColumn(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(verses, key = { it.bookId + ":" + it.chapter + ":" + it.verseRef }) { verse ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().testTag("reader_verse_${verse.verseRef}"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (verse.verseRef == initialVerseRef)
                                            MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text(verse.verseRef, style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        Text(verse.text, style = MaterialTheme.typography.bodyLarge.copy(fontSize = readingFontSize.sp),
                                            color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderChapterNavigation(chapter: Int, chapterCount: Int, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(onClick = onPrevious, enabled = chapter > 1) { Text(stringResource(R.string.reader_previous_chapter)) }
        Text(stringResource(R.string.reader_chapter_label, chapter, chapterCount), style = MaterialTheme.typography.labelMedium)
        OutlinedButton(onClick = onNext, enabled = chapter < chapterCount) { Text(stringResource(R.string.reader_next_chapter)) }
    }
}

@Composable
private fun ReaderTopBar(title: String, onBack: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Text("←", style = MaterialTheme.typography.headlineSmall) }
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.reader_title), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold)
            }
            Box(Modifier.size(9.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
private fun ReaderBookIdentity(bookName: String, usfmId: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(stringResource(R.string.reader_collection), style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
        Text(bookName, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold)
        Text(stringResource(R.string.reader_canonical_book_id) + " • " + usfmId,
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ReaderControlPanel(fontSize: Float, onDecreaseFont: () -> Unit, onIncreaseFont: () -> Unit, onToggleFocus: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = MaterialTheme.shapes.large) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Aa", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold)
            IconButton(onClick = onDecreaseFont, enabled = fontSize > 16f) { Text("A−") }
            Text("${fontSize.toInt()} sp", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            IconButton(onClick = onIncreaseFont, enabled = fontSize < 32f) { Text("A+") }
            Spacer(Modifier.weight(1f))
            OutlinedButton(onClick = onToggleFocus) { Text(stringResource(R.string.reader_focus)) }
        }
    }
}

@Composable
private fun ScriptureUnavailablePanel(fontSize: Float) {
    Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(
                MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)
            ))).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.Start
        ) {
            Text("✦", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.reader_quiet_space), style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.reader_content_unavailable),
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.secondaryContainer) {
                Text(stringResource(R.string.reader_content_not_loaded),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold)
            }
            Text(stringResource(R.string.reader_no_sample_notice), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
