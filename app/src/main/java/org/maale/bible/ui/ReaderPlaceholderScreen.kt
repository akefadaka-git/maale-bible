package org.maale.bible.ui
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.maale.bible.data.catalog.CanonicalBookCatalog
@Composable
fun ReaderPlaceholderScreen(bookId: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val book = CanonicalBookCatalog.find(bookId)
    Column(modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(book?.englishName ?: bookId, style = MaterialTheme.typography.headlineSmall)
        Text("Scripture content is intentionally not bundled in this build. The approved source remains outside the public repository pending a separate content-intake decision.", modifier = Modifier.padding(vertical = 16.dp))
        Button(onClick = onBack) { Text("Back to books") }
    }
}
