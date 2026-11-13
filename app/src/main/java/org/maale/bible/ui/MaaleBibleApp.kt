package org.maale.bible.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import org.maale.bible.data.repository.StudyDatabase
import org.maale.bible.ui.theme.MaaleBibleTheme

private enum class AppDestination(val label: String) {
    HOME("Home"), BOOKS("Books"), SEARCH("Search"), SETTINGS("Settings")
}

@Composable
fun MaaleBibleApp() {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.applicationContext.getSharedPreferences("maale_bible_app_preferences", android.content.Context.MODE_PRIVATE)
    }
    val studyRepository = remember(context) { StudyDatabase(context.applicationContext) }
    var destination by remember { mutableStateOf(AppDestination.HOME) }
    var selectedBookId by remember { mutableStateOf<String?>(null) }
    var darkTheme by remember { mutableStateOf(preferences.getBoolean("dark_theme", false)) }

    DisposableEffect(studyRepository) {
        onDispose { studyRepository.close() }
    }

    MaaleBibleTheme(darkTheme = darkTheme) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    AppDestination.entries.forEach { item ->
                        NavigationBarItem(
                            selected = destination == item && selectedBookId == null,
                            onClick = { selectedBookId = null; destination = item },
                            icon = { Text(item.label.take(1)) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        ) { padding ->
            val contentModifier = Modifier.padding(padding)
            if (selectedBookId != null) {
                ReaderScreen(
                    bookId = selectedBookId.orEmpty(),
                    repository = studyRepository,
                    onBack = { selectedBookId = null },
                    modifier = contentModifier
                )
            } else {
                when (destination) {
                    AppDestination.HOME -> HomeScreen(onOpenBooks = { destination = AppDestination.BOOKS }, modifier = contentModifier)
                    AppDestination.BOOKS -> BooksScreen(onSelectBook = { selectedBookId = it }, modifier = contentModifier)
                    AppDestination.SEARCH -> CatalogueSearchScreen(onSelectBook = { selectedBookId = it }, modifier = contentModifier)
                    AppDestination.SETTINGS -> SettingsScreen(
                        darkTheme = darkTheme,
                        onDarkThemeChange = {
                            darkTheme = it
                            preferences.edit().putBoolean("dark_theme", it).apply()
                        },
                        modifier = contentModifier
                    )
                }
            }
        }
    }
}
