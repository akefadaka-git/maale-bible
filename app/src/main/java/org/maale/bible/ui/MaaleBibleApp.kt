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
    HOME("Home"), BOOKS("Books"), SEARCH("Search")
}

@Composable
fun MaaleBibleApp() {
    val context = LocalContext.current
    val studyRepository = remember(context) { StudyDatabase(context.applicationContext) }
    var destination by remember { mutableStateOf(AppDestination.HOME) }
    var selectedBookId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                AppDestination.entries.forEach { item ->
                    NavigationBarItem(
                        selected = destination == item && selectedBookId == null,
                        onClick = { selectedBookId = null; destination = item },
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
                onBack = { selectedBookId = null },
                modifier = contentModifier
            )
        } else {
            when (destination) {
                AppDestination.HOME -> HomeScreen(onOpenBooks = { destination = AppDestination.BOOKS }, modifier = contentModifier)
                AppDestination.BOOKS -> BooksScreen(onSelectBook = { selectedBookId = it }, modifier = contentModifier)
                AppDestination.SEARCH -> SearchPlaceholderScreen(onSelectBook = { selectedBookId = it }, modifier = contentModifier)
            }
        }
    }
}
