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

private enum class AppDestination(val label: String) {
    HOME("Home"), BOOKS("Books"), SEARCH("Search"), STUDY("Study"), SETTINGS("Settings")
}

@Composable
fun MaaleBibleApp() {
    val context = LocalContext.current
    val studyRepository = remember(context) { StudyDatabase(context.applicationContext) }
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
                        label = { Text(item.label) }
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
                AppDestination.SEARCH -> SearchPlaceholderScreen(onSelectBook = { selectedBookId = it }, modifier = contentModifier)
                AppDestination.STUDY -> StudyHubScreen(repository = studyRepository, onOpenBook = { selectedBookId = it }, modifier = contentModifier)
                AppDestination.SETTINGS -> SettingsScreen(modifier = contentModifier)
            }
        }
    }
}
