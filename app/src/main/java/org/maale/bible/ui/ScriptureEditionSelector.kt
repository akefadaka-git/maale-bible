package org.maale.bible.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.maale.bible.data.scripture.ScriptureEditionCatalog
import org.maale.bible.data.scripture.ScriptureEditionStatus

@Composable
fun ScriptureEditionSelector(
    selectedEditionId: String,
    onEditionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    selectorTag: String = "scripture_edition_selector"
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val selected = ScriptureEditionCatalog.find(selectedEditionId)
        ?: ScriptureEditionCatalog.editions.first()
    val matches = remember(query) {
        val term = query.trim()
        ScriptureEditionCatalog.editions.filter { edition ->
            term.isBlank() || edition.title.contains(term, ignoreCase = true) ||
                edition.id.contains(term, ignoreCase = true) ||
                edition.languageName.contains(term, ignoreCase = true)
        }
    }

    Button(
        onClick = { query = ""; expanded = true },
        modifier = modifier.fillMaxWidth().testTag(selectorTag)
    ) {
        Text("${selected.languageName}: ${selected.title}")
    }

    if (expanded) {
        AlertDialog(
            onDismissRequest = { expanded = false },
            title = { Text("Choose Bible edition") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth().testTag("${selectorTag}_search"),
                        singleLine = true,
                        label = { Text("Search editions") }
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(matches, key = { it.id }) { edition ->
                            TextButton(
                                onClick = {
                                    onEditionSelected(edition.id)
                                    expanded = false
                                },
                                modifier = Modifier.fillMaxWidth().testTag("reader_edition_${edition.id}")
                            ) {
                                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(edition.title, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        "${edition.languageName} · ${edition.status.name.replace('_', ' ')}" +
                                            if (edition.status == ScriptureEditionStatus.PRIMARY_SOURCE)
                                                " · primary source; package checked separately"
                                            else " · catalogue only; text and digital rights not verified",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    if (matches.isEmpty()) {
                        Text("No editions match this search.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { expanded = false }) { Text("Close") }
            }
        )
    }
}
