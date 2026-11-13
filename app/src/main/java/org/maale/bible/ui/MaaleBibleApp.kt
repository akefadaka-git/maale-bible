package org.maale.bible.ui

import org.maale.bible.R

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import org.maale.bible.data.repository.StudyDatabase

private enum class AppDestination(val labelRes: Int) {
    HOME(R.string.nav_home),
    BOOKS(R.string.nav_books),
    SEARCH(R.string.nav_search),
    LISTEN(R.string.nav_listen),
    STUDY(R.string.nav_study),
    SETTINGS(R.string.nav_settings)
}

@Composable
fun MaaleBibleApp() {
    val context = LocalContext.current
    val studyRepository = remember(context) { StudyDatabase(context.applicationContext) }
    DisposableEffect(studyRepository) { onDispose { studyRepository.close() } }
    var destination by rememberSaveable { mutableStateOf(AppDestination.HOME.name) }
    var selectedBookId by rememberSaveable { mutableStateOf<String?>(null) }
    val current = AppDestination.valueOf(destination)

    Scaffold(
        bottomBar = {
            NavigationBar {
                AppDestination.entries.forEach { item ->
                    NavigationBarItem(
                        selected = current == item && selectedBookId == null,
                        onClick = { selectedBookId = null; destination = item.name },
                        icon = {},
                        label = { Text(stringResource(item.labelRes), maxLines = 1) }
                    )
                }
            }
        }
    ) { padding ->
        val contentModifier = Modifier.padding(padding)
        if (selectedBookId != null) {
            ReaderPlaceholderScreen(
                bookId = selectedBookId.orEmpty(),
                repository = studyRepository,
                onBack = { selectedBookId = null; destination = AppDestination.BOOKS.name },
                modifier = contentModifier
            )
        } else {
            when (current) {
                AppDestination.HOME -> HomeScreen(onOpenBooks = { destination = AppDestination.BOOKS.name }, modifier = contentModifier)
                AppDestination.BOOKS -> BooksScreen(onSelectBook = { selectedBookId = it }, modifier = contentModifier)
                AppDestination.SEARCH -> CatalogueSearchScreen(onSelectBook = { selectedBookId = it }, modifier = contentModifier)
                AppDestination.LISTEN -> AudioPlaceholderScreen(modifier = contentModifier)
                AppDestination.STUDY -> StudyHubScreen(repository = studyRepository, onOpenBook = { selectedBookId = it }, modifier = contentModifier)
                AppDestination.SETTINGS -> SettingsScreen(repository = studyRepository, modifier = contentModifier)
            }
        }
    }
}
