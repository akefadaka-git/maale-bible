package org.maale.bible.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Card(Modifier.fillMaxWidth()) {
            androidx.compose.foundation.layout.Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Dark theme", style = MaterialTheme.typography.titleMedium)
                    Text("Use a dark colour scheme for reading in low-light environments.", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = darkTheme,
                    onCheckedChange = onDarkThemeChange,
                    modifier = Modifier.semantics { contentDescription = "Enable dark theme" }
                )
            }
        }
        Text("Accessibility", style = MaterialTheme.typography.titleMedium)
        Text("The app uses Material components and labelled navigation controls. System font scaling and Android accessibility services remain supported; verify screen-reader order and contrast on target devices during A4 acceptance.")
        Text("Language", style = MaterialTheme.typography.titleMedium)
        Text("Maale-first branding is present. Full Amharic and English interface translation is not yet complete and remains an A3 acceptance item.")
        Text("Privacy and offline use", style = MaterialTheme.typography.titleMedium)
        Text("Personal notes, bookmarks, highlights and reading plans are stored in the encrypted on-device study database. Scripture text and online account sync are not included in this foundation.")
    }
}
