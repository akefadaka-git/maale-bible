package org.maale.bible.ui

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

private enum class AppDestination(val label: String, val symbol: String) {
    HOME("Home", "⌂"),
    BOOKS("Books", "▤"),
    SEARCH("Search", "⌕")
}

@Composable
fun MaaleBibleApp() {
    var destination by remember { mutableStateOf(AppDestination.HOME) }
    var selectedBookId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                AppDestination.entries.forEach { item ->
                    NavigationBarItem(
                        selected = destination == item && selectedBookId == null,
                        onClick = { selectedBookId = null; destination = item },
                        icon = { Text(item.symbol) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        if (selectedBookId != null) {
            ReaderPlaceholderScreen(
                selectedBookId!!,
                { selectedBookId = null },
                Modifier.padding(padding)
            )
        } else {
            when (destination) {
                AppDestination.HOME -> HomeScreen(
                    onOpenBooks = { destination = AppDestination.BOOKS },
                    onOpenSearch = { destination = AppDestination.SEARCH },
                    modifier = Modifier.padding(padding)
                )
                AppDestination.BOOKS -> BooksScreen(
                    onSelectBook = { selectedBookId = it },
                    modifier = Modifier.padding(padding)
                )
                AppDestination.SEARCH -> SearchPlaceholderScreen(Modifier.padding(padding))
            }
        }
    }
}
