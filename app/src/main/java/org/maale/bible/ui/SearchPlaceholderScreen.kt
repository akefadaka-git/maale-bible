package org.maale.bible.ui
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun SearchPlaceholderScreen(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Search", style = MaterialTheme.typography.headlineSmall)
        Text("Offline search becomes available when the separately approved local scripture dataset is integrated.", modifier = Modifier.padding(top = 8.dp))
    }
}
