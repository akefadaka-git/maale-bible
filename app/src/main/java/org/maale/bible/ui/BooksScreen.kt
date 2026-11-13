package org.maale.bible.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import org.maale.bible.data.catalog.CanonicalBookCatalog

@Composable
fun BooksScreen(onSelectBook: (String) -> Unit, modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    val colors = MaterialTheme.colorScheme
    val books = remember(query) {
        CanonicalBookCatalog.books.filter {
            it.englishName.contains(query.trim(), ignoreCase = true) ||
                it.usfmId.contains(query.trim(), ignoreCase = true)
        }
    }
    Column(modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("The Bible", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Explore the books of the Maale Bible", style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant)
        OutlinedTextField(value = query, onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            shape = RoundedCornerShape(18.dp), label = { Text("Find a book") },
            placeholder = { Text("Search by name or book code") },
            leadingIcon = { Text("⌕", style = MaterialTheme.typography.titleLarge, color = colors.primary) })
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("BOOK COLLECTION", style = MaterialTheme.typography.labelMedium,
                color = colors.primary, fontWeight = FontWeight.Bold)
            Surface(shape = CircleShape, color = colors.secondaryContainer) {
                Text("${books.size} / ${CanonicalBookCatalog.books.size}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium, color = colors.onSecondaryContainer,
                    fontWeight = FontWeight.Bold)
            }
        }
        if (books.isEmpty()) {
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = colors.surfaceVariant) {
                Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("No matching books", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Check the spelling or try a book code.", style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                items(books, key = { it.usfmId }) { book ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { onSelectBook(book.usfmId) }
                        .semantics { contentDescription = "Open " + book.englishName },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
                            horizontalArrangement = Arrangement.spacedBy(13.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(13.dp), color = colors.primaryContainer) {
                                Text(book.order.toString().padStart(2, '0'),
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 10.dp),
                                    style = MaterialTheme.typography.labelLarge, color = colors.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold)
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(book.englishName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(book.usfmId, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                            }
                            Text("›", style = MaterialTheme.typography.headlineMedium, color = colors.primary)
                        }
                    }
                }
            }
        }
    }
}
