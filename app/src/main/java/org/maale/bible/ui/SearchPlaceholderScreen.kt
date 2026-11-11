package org.maale.bible.ui

// Offline FTS search is edition-scoped; all database work runs off the main thread.

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.maale.bible.R
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.maale.bible.data.catalog.CanonicalBookCatalog
import org.maale.bible.data.content.OfflineScriptureStore
import org.maale.bible.data.content.OfflineContentDescriptor
import org.maale.bible.data.content.ScriptureSearchResult

@Composable
fun SearchPlaceholderScreen(
    onSelectBook: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current.applicationContext
    val loadingEditionsText = stringResource(R.string.search_loading_editions)
    val noEditionText = stringResource(R.string.search_no_edition)
    val listFailedText = stringResource(R.string.search_list_failed)
    val indexedPassagesFormat = stringResource(R.string.search_indexed_passages)
    val selectedNotInstalledText = stringResource(R.string.search_selected_not_installed)
    val integrityFailedText = stringResource(R.string.search_integrity_failed)
    val noResultsText = stringResource(R.string.search_no_results)
    val resultCountFormat = stringResource(R.string.search_result_count)
    val searchErrorText = stringResource(R.string.search_error)
    val searchingText = stringResource(R.string.search_searching)
    var query by remember { mutableStateOf("") }
    var store by remember { mutableStateOf<OfflineScriptureStore?>(null) }
    var editions by remember { mutableStateOf<List<OfflineContentDescriptor>>(emptyList()) }
    var selectedEditionId by remember { mutableStateOf<String?>(null) }
    var storeState by remember { mutableStateOf(loadingEditionsText) }
    var searchResults by remember { mutableStateOf<List<ScriptureSearchResult>>(emptyList()) }
    var searchState by remember { mutableStateOf("") }
    val currentStore by rememberUpdatedState(store)

    LaunchedEffect(context) {
        val available = withContext(Dispatchers.IO) {
            runCatching { OfflineScriptureStore.installedEditions(context) }
        }
        available.fold(
            onSuccess = { installed ->
                editions = installed
                selectedEditionId = installed.firstOrNull()?.editionId
                if (installed.isEmpty()) storeState = noEditionText
            },
            onFailure = {
                storeState = listFailedText
            }
        )
    }

    LaunchedEffect(context, selectedEditionId) {
        val editionId = selectedEditionId ?: return@LaunchedEffect
        storeState = verifyingEditionText
        val opened = withContext(Dispatchers.IO) {
            runCatching { OfflineScriptureStore.openInstalled(context, editionId) }
        }
        opened.fold(
            onSuccess = { installed ->
                val previous = store
                store = installed
                withContext(Dispatchers.IO) { previous?.close() }
                storeState = if (installed == null) {
                    stringResource(R.string.search_selected_not_installed)
                } else {
                    String.format(indexedPassagesFormat, installed.descriptor.editionId, installed.descriptor.verseCount)
                }
            },
            onFailure = {
                storeState = integrityFailedText
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose { currentStore?.close() }
    }

    val normalizedQuery = query.trim()
    val matchingBooks = remember(normalizedQuery) {
        if (normalizedQuery.isBlank()) emptyList()
        else CanonicalBookCatalog.books.filter {
            it.englishName.contains(normalizedQuery, ignoreCase = true) ||
                it.usfmId.contains(normalizedQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(normalizedQuery, store) {
        searchResults = emptyList()
        searchState = ""
        if (normalizedQuery.isBlank()) return@LaunchedEffect
        val activeStore = store ?: return@LaunchedEffect
        delay(300)
        val result = withContext(Dispatchers.IO) {
            runCatching { activeStore.search(normalizedQuery, limit = 50) }
        }
        result.fold(
            onSuccess = { rows ->
                searchResults = rows
                searchState = if (rows.isEmpty()) stringResource(R.string.search_no_results)
                else String.format(resultCountFormat, rows.size)
            },
            onFailure = {
                searchState = stringResource(R.string.search_error)
            }
        )
    }

    Column(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(
            modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth().align(Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(stringResource(R.string.search_eyebrow), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.search_title), style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.search_description),
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().semantics {
                    contentDescription = stringResource(R.string.search_accessibility)
                },
                singleLine = true,
                label = { Text(stringResource(R.string.search_field_label)) },
                placeholder = { Text(stringResource(R.string.search_field_hint)) },
                trailingIcon = { if (query.isNotEmpty()) TextButton(onClick = { query = "" }) { Text(stringResource(R.string.action_clear)) } }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.size(8.dp).background(
                        if (store == null) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
                        CircleShape
                    )
                )
                Text(storeState, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
            }
        }

        if (editions.size > 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                editions.forEach { edition ->
                    TextButton(onClick = { selectedEditionId = edition.editionId }) {
                        Text(
                            if (selectedEditionId == edition.editionId) "✓ " + edition.editionId else edition.editionId
                        )
                    }
                }
            }
        }

        if (normalizedQuery.isBlank()) {
            SearchCapabilityCard(installed = store != null)
        } else {
            LazyColumn(
                modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth()
                    .align(Alignment.CenterHorizontally).weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (matchingBooks.isNotEmpty()) {
                    item(key = "book-heading") {
                        Text(stringResource(R.string.search_books_heading, matchingBooks.size), style = MaterialTheme.typography.titleSmall)
                    }
                    items(matchingBooks, key = { "book-" + it.usfmId }) { book ->
                        Card(onClick = { onSelectBook(book.usfmId) }, modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(book.englishName, style = MaterialTheme.typography.titleMedium)
                                    Text(stringResource(R.string.reader_canonical_book_id) + " · " + book.usfmId, style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(stringResource(R.string.action_open) + " →", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                if (store != null) {
                    item(key = "scripture-heading") {
                        Text(searchState.ifBlank { stringResource(R.string.search_searching) },
                            modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.titleSmall)
                    }
                    items(searchResults, key = { "verse-" + it.bookId + ":" + it.chapter + ":" + it.verseRef }) { result ->
                        val bookName = CanonicalBookCatalog.find(result.bookId)?.englishName ?: result.bookId
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(bookName + " " + result.chapter + ":" + result.verseRef,
                                    style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold)
                                Text(result.text, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                } else {
                    item(key = "content-unavailable") {
                        Card(modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Text(storeState, modifier = Modifier.padding(18.dp), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchCapabilityCard(installed: Boolean) {
    Card(modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.search_capabilities), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.search_capability_description), style = MaterialTheme.typography.titleMedium)
            CapabilityLine(stringResource(R.string.search_available), stringResource(R.string.search_book_lookup))
            CapabilityLine(stringResource(if (installed) R.string.search_available else R.string.search_waiting_download), stringResource(R.string.search_offline_full_text))
            CapabilityLine(stringResource(R.string.search_in_development), stringResource(R.string.search_edition_filters))
        }
    }
}

@Composable
private fun CapabilityLine(status: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(status.uppercase(), style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
        Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
