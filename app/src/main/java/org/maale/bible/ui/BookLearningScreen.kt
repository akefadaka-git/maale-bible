package org.maale.bible.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.maale.bible.R
import org.maale.bible.data.catalog.CanonicalBookCatalog

private enum class BookLearningTab(val label: Int) {
    OVERVIEW(R.string.learning_tab_overview),
    INDUCTIVE(R.string.learning_tab_inductive),
    QUIZ(R.string.learning_tab_quiz)
}

@Composable
fun BookLearningScreen(
    bookId: String,
    onBack: () -> Unit,
    onOpenReader: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val book = remember(bookId) { CanonicalBookCatalog.find(bookId) }
    var selectedTab by rememberSaveable { mutableStateOf(BookLearningTab.OVERVIEW) }
    val title = book?.englishName ?: bookId
    val testament = if ((book?.order ?: 0) <= 39) stringResource(R.string.explorer_old_testament) else stringResource(R.string.explorer_new_testament)

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onBack) { Text("‹  " + stringResource(R.string.learning_back_to_books)) }
            Spacer(Modifier.weight(1f))
            Text(testament, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
        }
        Text(title, style = MaterialTheme.typography.headlineLarge)
        Text(
            stringResource(R.string.learning_book_metadata, book?.usfmId ?: bookId, book?.order ?: 0),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.learning_centre_title), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(stringResource(R.string.learning_centre_description), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(stringResource(R.string.learning_bilingual_status), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        TabRow(selectedTabIndex = selectedTab.ordinal) {
            BookLearningTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(stringResource(tab.label)) }
                )
            }
        }
        when (selectedTab) {
            BookLearningTab.OVERVIEW -> {
                LearningSectionCard(
                    title = stringResource(R.string.learning_introduction_title),
                    description = stringResource(R.string.learning_introduction_pending)
                )
                LearningSectionCard(
                    title = stringResource(R.string.learning_background_title),
                    description = stringResource(R.string.learning_background_fields)
                )
                LearningSectionCard(
                    title = stringResource(R.string.learning_structure_title),
                    description = stringResource(R.string.learning_structure_fields)
                )
                LearningSectionCard(
                    title = stringResource(R.string.learning_themes_title),
                    description = stringResource(R.string.learning_themes_fields)
                )
                LearningSectionCard(
                    title = stringResource(R.string.learning_related_title),
                    description = stringResource(R.string.learning_related_fields)
                )
                Button(onClick = { onOpenReader(bookId) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.learning_open_reader))
                }
            }
            BookLearningTab.INDUCTIVE -> {
                Text(stringResource(R.string.learning_inductive_intro), style = MaterialTheme.typography.bodyMedium)
                LearningSectionCard(
                    title = stringResource(R.string.learning_observation_title),
                    description = stringResource(R.string.learning_observation_prompts)
                )
                LearningSectionCard(
                    title = stringResource(R.string.learning_interpretation_title),
                    description = stringResource(R.string.learning_interpretation_prompts)
                )
                LearningSectionCard(
                    title = stringResource(R.string.learning_application_title),
                    description = stringResource(R.string.learning_application_prompts)
                )
                LearningSectionCard(
                    title = stringResource(R.string.learning_study_guide_pending_title),
                    description = stringResource(R.string.learning_study_guide_pending)
                )
            }
            BookLearningTab.QUIZ -> {
                LearningSectionCard(
                    title = stringResource(R.string.learning_quiz_title),
                    description = stringResource(R.string.learning_quiz_pending)
                )
                OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.learning_quiz_unavailable))
                }
            }
        }
        Text(
            stringResource(R.string.learning_provenance_notice),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LearningSectionCard(title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
