package org.maale.bible.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.maale.bible.data.catalog.CanonicalBookCatalog

@Composable
fun ReaderPlaceholderScreen(bookId: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val book = CanonicalBookCatalog.find(bookId)
    val colors = MaterialTheme.colorScheme
    Column(modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("YOUR READING SPACE", style = MaterialTheme.typography.labelMedium,
            color = colors.primary, fontWeight = FontWeight.Bold)
        Text(book?.englishName ?: bookId, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("MAALE BIBLE  ·  ${book?.usfmId ?: bookId}", style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant)
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)) {
            Column(Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(shape = RoundedCornerShape(22.dp), color = colors.primaryContainer) {
                    Text("▤", modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp),
                        style = MaterialTheme.typography.displaySmall, color = colors.onPrimaryContainer)
                }
                Text("A quiet place to reflect", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("The approved Maale scripture source is not bundled in this build. Verified chapter text will appear here after the separately governed offline content integration is complete.",
                    style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Text("Back to all books")
        }
    }
}
