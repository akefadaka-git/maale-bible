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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SearchPlaceholderScreen(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(shape = RoundedCornerShape(28.dp), color = colors.primaryContainer) {
            Text("⌕", modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp),
                style = MaterialTheme.typography.displayMedium, color = colors.onPrimaryContainer)
        }
        Text("Search the Bible", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Find a book or passage and return to the Word with ease.",
            style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("Offline search is coming", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Passage search will be activated when the approved local scripture dataset and search index are integrated. This screen intentionally shows no invented verses or substitute results.",
                    style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
    }
}
