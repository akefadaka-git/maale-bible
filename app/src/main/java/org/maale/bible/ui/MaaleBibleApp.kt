package org.maale.bible.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.res.stringResource
import org.maale.bible.R

private enum class AppDestination(val label: Int) {
    HOME(R.string.nav_home),
    BOOKS(R.string.nav_books),
    SEARCH(R.string.nav_search),
    BIBLES(R.string.nav_bibles),
    LISTEN(R.string.nav_listen),
    STUDY(R.string.nav_study)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaaleBibleApp() {
    val context = LocalContext.current
    var language by remember { mutableStateOf(AppLanguage.load(context)) }
    val baseConfiguration = LocalConfiguration.current
    val localizedConfiguration = remember(baseConfiguration, language) {
        Configuration(baseConfiguration).apply {
            setLocales(LocaleList.forLanguageTags(language.tag))
        }
    }

    CompositionLocalProvider(LocalConfiguration provides localizedConfiguration) {
        var destination by remember { mutableStateOf(AppDestination.HOME) }
        var selectedBookId by remember { mutableStateOf<String?>(null) }
        var languageMenuExpanded by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    actions = {
                        TextButton(onClick = { languageMenuExpanded = true }) {
                            Text(stringResource(R.string.language_label))
                        }
                        DropdownMenu(
                            expanded = languageMenuExpanded,
                            onDismissRequest = { languageMenuExpanded = false }
                        ) {
                            if (language == AppLanguage.MAALE) {
                                Text(
                                    stringResource(R.string.language_fallback_notice),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                                )
                            }
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.language_maale)) },
                                onClick = {
                                    language = AppLanguage.MAALE
                                    AppLanguage.save(context, language)
                                    languageMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.language_amharic)) },
                                onClick = {
                                    language = AppLanguage.AMHARIC
                                    AppLanguage.save(context, language)
                                    languageMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.language_english)) },
                                onClick = {
                                    language = AppLanguage.ENGLISH
                                    AppLanguage.save(context, language)
                                    languageMenuExpanded = false
                                }
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    AppDestination.entries.forEach { item ->
                        NavigationBarItem(
                            selected = destination == item && selectedBookId == null,
                            onClick = { selectedBookId = null; destination = item },
                            icon = {},
                            label = { Text(stringResource(item.label)) }
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
            } else when (destination) {
                AppDestination.HOME -> HomeScreen(
                    { destination = AppDestination.BOOKS },
                    Modifier.padding(padding)
                )
                AppDestination.BOOKS -> BooksScreen(
                    { selectedBookId = it },
                    Modifier.padding(padding)
                )
                AppDestination.SEARCH -> SearchPlaceholderScreen(
                    { selectedBookId = it },
                    Modifier.padding(padding)
                )
                AppDestination.BIBLES -> BibleDownloadsScreen(Modifier.padding(padding))
                AppDestination.LISTEN -> AudioPlaceholderScreen(Modifier.padding(padding))
                AppDestination.STUDY -> SmartStudyWorkspaceScreen(Modifier.padding(padding))
            }
        }
    }
}
