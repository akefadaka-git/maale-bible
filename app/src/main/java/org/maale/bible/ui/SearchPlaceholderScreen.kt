package org.maale.bible.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.maale.bible.data.catalog.CanonicalBookCatalog

@Composable
fun SearchPlaceholderScreen(
    onSelectBook: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }
    val results = CanonicalBookCatalog.search(query)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Search the catalogue", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Book name or code") },
            singleLine = true,
            supportingText = { Text("Searches the 66-book catalogue stored on this device.") }
        )
        Text(
            text = if (results.isEmpty()) "No books match “$query”" else "${results.size} books found",
            style = MaterialTheme.typography.labelLarge
        )
        if (results.isEmpty()) {
            Text(
                "Try an English book name or a three-character USFM code.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn {
                items(results, key = { it.usfmId }) { book ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectBook(book.usfmId) }
                            .semantics {
                                contentDescription = "Open ${book.englishName}, catalogue entry ${book.order}"
                            }
                            .padding(horizontal = 8.dp, vertical = 14.dp)
                    ) {
                        Column {
                            Text(
                                "${book.order}. ${book.englishName}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(book.usfmId, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
        Text(
            "This search covers book names and catalogue codes only. Scripture search remains unavailable until a separate content-intake approval.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
