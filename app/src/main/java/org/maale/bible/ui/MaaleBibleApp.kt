package org.maale.bible.ui

import org.maale.bible.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.maale.bible.data.repository.StudyDatabase

private enum class AppDestination(val labelRes: Int, val glyph: String) {
    HOME(R.string.nav_home, "⌂"),
    BOOKS(R.string.nav_books, "▤"),
    SEARCH(R.string.nav_search, "⌕"),
    MORE(R.string.nav_more, "⋯")
}
private enum class MoreDestination { LISTEN, STUDY, SETTINGS }

@Composable
fun MaaleBibleApp() {
    val context = LocalContext.current
    val studyRepository = remember(context) { StudyDatabase(context.applicationContext) }
    DisposableEffect(studyRepository) { onDispose { studyRepository.close() } }
    var destination by rememberSaveable { mutableStateOf(AppDestination.HOME.name) }
    var moreDestination by rememberSaveable { mutableStateOf(MoreDestination.LISTEN.name) }
    var selectedBookId by rememberSaveable { mutableStateOf<String?>(null) }
    val current = AppDestination.valueOf(destination)
    Scaffold(bottomBar = {
        NavigationBar {
            AppDestination.entries.forEach { item ->
                NavigationBarItem(
                    selected = current == item && selectedBookId == null,
                    onClick = { selectedBookId = null; destination = item.name },
                    icon = { Text(item.glyph, style = MaterialTheme.typography.titleMedium) },
                    label = { Text(stringResource(item.labelRes), maxLines = 1) }
                )
            }
        }
    }) { padding ->
        val contentModifier = Modifier.padding(padding)
        if (selectedBookId != null) {
            ReaderPlaceholderScreen(bookId = selectedBookId.orEmpty(), repository = studyRepository,
                onBack = { selectedBookId = null; destination = AppDestination.BOOKS.name }, modifier = contentModifier)
        } else when (current) {
            AppDestination.HOME -> HomeScreen(onOpenBooks = { destination = AppDestination.BOOKS.name }, modifier = contentModifier)
            AppDestination.BOOKS -> BooksScreen(onSelectBook = { selectedBookId = it }, modifier = contentModifier)
            AppDestination.SEARCH -> CatalogueSearchScreen(onSelectBook = { selectedBookId = it }, modifier = contentModifier)
            AppDestination.MORE -> MoreScreen(
                selected = MoreDestination.valueOf(moreDestination),
                onSelect = { moreDestination = it.name },
                repository = studyRepository,
                onOpenBook = { selectedBookId = it },
                modifier = contentModifier
            )
        }
    }
}

@Composable
private fun MoreScreen(
    selected: MoreDestination,
    onSelect: (MoreDestination) -> Unit,
    repository: StudyDatabase,
    onOpenBook: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(stringResource(R.string.nav_more), style = MaterialTheme.typography.headlineSmall)
        androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MoreDestination.entries.forEach { item ->
                Button(onClick = { onSelect(item) }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(when(item) {
                        MoreDestination.LISTEN -> R.string.nav_listen
                        MoreDestination.STUDY -> R.string.nav_study
                        MoreDestination.SETTINGS -> R.string.nav_settings
                    }), maxLines = 1)
                }
            }
        }
        when (selected) {
            MoreDestination.LISTEN -> AudioPlaceholderScreen()
            MoreDestination.STUDY -> StudyHubScreen(repository = repository, onOpenBook = onOpenBook)
            MoreDestination.SETTINGS -> SettingsScreen(repository = repository)
        }
    }
}
