package org.maale.bible.ui
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.maale.bible.data.catalog.CanonicalBookCatalog
@Composable
fun BooksScreen(onSelectBook: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text("Books of the Maale Bible", style = MaterialTheme.typography.headlineSmall)
        Text("Official 66-book catalogue. Scripture content is not loaded in this foundation build.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp, bottom = 12.dp))
        LazyColumn {
            items(CanonicalBookCatalog.books, key = { it.usfmId }) { book ->
                Column(
                    Modifier.fillMaxWidth().clickable { onSelectBook(book.usfmId) }
                        .semantics { contentDescription = "Open " + book.englishName }
                        .padding(vertical = 14.dp, horizontal = 8.dp)
                ) {
                    Text(book.order.toString() + ". " + book.englishName, style = MaterialTheme.typography.titleMedium)
                    Text(book.usfmId, style = MaterialTheme.typography.labelSmall)
                }
                HorizontalDivider()
            }
        }
    }
}
