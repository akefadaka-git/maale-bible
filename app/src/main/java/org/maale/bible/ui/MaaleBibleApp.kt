package org.maale.bible.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

private enum class AppDestination(val label: String) {
    HOME("Home"), BOOKS("Books"), SEARCH("Search"), BIBLES("Bibles"), LISTEN("Listen"), STUDY("Study")
}

@Composable
fun MaaleBibleApp() {
    var destination by remember { mutableStateOf(AppDestination.HOME) }
    var selectedBookId by remember { mutableStateOf<String?>(null) }
    Scaffold(bottomBar = {
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
    }) { padding ->
        if (selectedBookId != null) {
            ReaderPlaceholderScreen(selectedBookId!!, { selectedBookId = null }, Modifier.padding(padding))
        } else when (destination) {
            AppDestination.HOME -> HomeScreen({ destination = AppDestination.BOOKS }, Modifier.padding(padding))
            AppDestination.BOOKS -> BooksScreen({ selectedBookId = it }, Modifier.padding(padding))
            AppDestination.SEARCH -> SearchPlaceholderScreen({ selectedBookId = it }, Modifier.padding(padding))
            AppDestination.BIBLES -> BibleDownloadsScreen(Modifier.padding(padding))
            AppDestination.LISTEN -> AudioPlaceholderScreen(Modifier.padding(padding))
            AppDestination.STUDY -> SmartStudyWorkspaceScreen(Modifier.padding(padding))
        }
    }
}
