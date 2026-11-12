package org.maale.bible.ui
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun HomeScreen(onOpenBooks: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("ጌኤዦ ማፃኣፖ", style = MaterialTheme.typography.headlineMedium)
        Text("Maale Bible", style = MaterialTheme.typography.titleLarge)
        Text("Offline-first reading experience", modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))
        Button(onClick = onOpenBooks) { Text("Browse 66 books") }
    }
}
