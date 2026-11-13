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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.maale.bible.data.catalog.CanonicalBookCatalog

/**
 * Smart search foundation. Book metadata lookup is functional; full-text
 * scripture search remains unavailable until a verified corpus is indexed.
 */
@Composable
fun CatalogueSearchScreen(
    onSelectBook: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    val normalizedQuery = query.trim()
    val matchingBooks = remember(normalizedQuery) {
        if (normalizedQuery.isBlank()) emptyList()
        else CanonicalBookCatalog.books.filter {
            it.englishName.contains(normalizedQuery, ignoreCase = true) ||
                it.usfmId.contains(normalizedQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "DISCOVER THE SCRIPTURES",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Search with purpose.",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Find a book by its name or canonical code. Scripture phrase search will activate when verified text is indexed.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Search Bible book names or canonical codes" },
                singleLine = true,
                label = { Text("Search books") },
                placeholder = { Text("e.g. Genesis or GEN") },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        TextButton(onClick = { query = "" }) { Text("Clear") }
                    }
                }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
                Text(
                    text = "BOOK CATALOGUE  •  66 METADATA ENTRIES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (normalizedQuery.isBlank()) {
            SearchCapabilityCard()
        } else if (matchingBooks.isEmpty()) {
            Card(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("No book matches", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Try a book name such as Psalms, or a canonical code such as PSA.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Text(
                text = "${matchingBooks.size} book match${if (matchingBooks.size == 1) "" else "es"}",
                modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth(),
                style = MaterialTheme.typography.titleSmall
            )
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(matchingBooks, key = { it.usfmId }) { book ->
                    Card(
                        onClick = { onSelectBook(book.usfmId) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(book.englishName, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "CANONICAL ID  •  ${book.usfmId}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text("Open  →", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchCapabilityCard() {
    Card(
        modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth().align(Alignment.CenterHorizontally),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("SEARCH CAPABILITIES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
            Text("A clear path from book discovery to scripture search.", style = MaterialTheme.typography.titleMedium)
            CapabilityLine("Available now", "Book-name and USFM-code lookup")
            CapabilityLine("Preparing", "Keyword and exact-phrase search across verified scripture text")
            CapabilityLine("Preparing", "Language-aware search and offline index")
            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.medium) {
                Text(
                    "SCRIPTURE INDEX NOT YET AVAILABLE",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CapabilityLine(status: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(status.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
        Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
