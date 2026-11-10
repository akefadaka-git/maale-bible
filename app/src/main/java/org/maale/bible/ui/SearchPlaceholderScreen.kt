package org.maale.bible.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.weight
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.maale.bible.data.catalog.CanonicalBookCatalog
import org.maale.bible.data.content.OfflineScriptureStore
import org.maale.bible.data.content.ScriptureSearchResult

@Composable
fun SearchPlaceholderScreen(
    onSelectBook: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current.applicationContext
    var query by remember { mutableStateOf("") }
    var store by remember { mutableStateOf<OfflineScriptureStore?>(null) }
    var storeState by remember { mutableStateOf("Loading offline scripture index…") }
    var searchResults by remember { mutableStateOf<List<ScriptureSearchResult>>(emptyList()) }
    var searchState by remember { mutableStateOf("") }
    val currentStore by rememberUpdatedState(store)

    LaunchedEffect(context) {
        val opened = withContext(Dispatchers.IO) {
            runCatching { OfflineScriptureStore.openInstalled(context) }
        }
        opened.fold(
            onSuccess = { installed ->
                store = installed
                storeState = if (installed == null) {
                    "No offline scripture edition is installed on this device."
                } else {
                    installed.descriptor.editionId + " · " + installed.descriptor.verseCount + " indexed passages"
                }
            },
            onFailure = {
                storeState = "The installed scripture package could not be verified. Reinstall it before searching."
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
                searchState = if (rows.isEmpty()) "No scripture passages match this search."
                else rows.size.toString() + " scripture result" + if (rows.size == 1) "" else "s"
            },
            onFailure = {
                searchState = "Search could not be completed. Check the installed content package."
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
            Text("DISCOVER THE SCRIPTURES", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
            Text("Search with purpose.", style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
            Text("Search installed Bible text offline, or find a book by name and canonical code.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().semantics {
                    contentDescription = "Search installed Bible text, book names or canonical codes"
                },
                singleLine = true,
                label = { Text("Search scripture") },
                placeholder = { Text("Enter a word or phrase") },
                trailingIcon = { if (query.isNotEmpty()) TextButton(onClick = { query = "" }) { Text("Clear") } }
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
                        Text("BOOKS · " + matchingBooks.size, style = MaterialTheme.typography.titleSmall)
                    }
                    items(matchingBooks, key = { "book-" + it.usfmId }) { book ->
                        Card(onClick = { onSelectBook(book.usfmId) }, modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(book.englishName, style = MaterialTheme.typography.titleMedium)
                                    Text("CANONICAL ID · " + book.usfmId, style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("Open →", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                if (store != null) {
                    item(key = "scripture-heading") {
                        Text(searchState.ifBlank { "Searching installed scripture…" },
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
            Text("SEARCH CAPABILITIES", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
            Text("Find words and phrases across installed Bible editions.", style = MaterialTheme.typography.titleMedium)
            CapabilityLine("Available", "Book-name and USFM-code lookup")
            CapabilityLine(if (installed) "Available" else "Waiting for download",
                "Offline full-text search in installed scripture")
            CapabilityLine("In development", "Edition filters and language-aware search")
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
