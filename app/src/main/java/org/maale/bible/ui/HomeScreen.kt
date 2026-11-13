package org.maale.bible.ui
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
@Composable
fun HomeScreen(onOpenBooks: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("ጌኤዦ ማፃኣፖ", style = MaterialTheme.typography.headlineMedium)
        Text("Maale Bible", style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.home_tagline), modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))
        Button(onClick = onOpenBooks) { Text(stringResource(R.string.browse_books)) }
    }
}
