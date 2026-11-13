package org.maale.bible.ui

import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.maale.bible.R
import org.maale.bible.data.repository.StudyDatabase

private enum class AppDestination(val labelRes: Int, val glyph: String) {
    HOME(R.string.nav_home, "⌂"),
    BOOKS(R.string.nav_books, "▤"),
    SEARCH(R.string.nav_search, "⌕"),
    MORE(R.string.nav_more, "⋯")
}
private enum class MoreDestination { LISTEN, STUDY, SETTINGS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaaleBibleApp() {
    val context = LocalContext.current
    val studyRepository = remember(context) { StudyDatabase(context.applicationContext) }
    DisposableEffect(studyRepository) { onDispose { studyRepository.close() } }

    var language by remember(context) { mutableStateOf(AppLanguage.load(context)) }
    val baseConfiguration = LocalConfiguration.current
    val localizedConfiguration = remember(baseConfiguration, language) {
        Configuration(baseConfiguration).apply {
            setLocales(LocaleList.forLanguageTags(language.tag))
        }
    }
    val localizedContext = remember(context, localizedConfiguration) {
        context.createConfigurationContext(localizedConfiguration)
    }

    CompositionLocalProvider(
        LocalConfiguration provides localizedConfiguration,
        LocalContext provides localizedContext,
        LocalResources provides localizedContext.resources
    ) {
        var destination by rememberSaveable { mutableStateOf(AppDestination.HOME.name) }
        var moreDestination by rememberSaveable { mutableStateOf(MoreDestination.LISTEN.name) }
        var selectedBookId by rememberSaveable { mutableStateOf<String?>(null) }
        var languageMenuExpanded by remember { mutableStateOf(false) }
        val current = AppDestination.valueOf(destination)

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    actions = {
                        TextButton(
                            onClick = { languageMenuExpanded = true },
                            modifier = Modifier.testTag("language_selector")
                        ) { Text(stringResource(R.string.language_label)) }
                        DropdownMenu(
                            expanded = languageMenuExpanded,
                            onDismissRequest = { languageMenuExpanded = false }
                        ) {
                            if (language == AppLanguage.MAALE) {
                                Text(
                                    stringResource(R.string.language_fallback_notice),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.language_maale)) },
                                modifier = Modifier.testTag("language_maale"),
                                onClick = {
                                    language = AppLanguage.MAALE
                                    AppLanguage.save(context, language)
                                    languageMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.language_amharic)) },
                                modifier = Modifier.testTag("language_amharic"),
                                onClick = {
                                    language = AppLanguage.AMHARIC
                                    AppLanguage.save(context, language)
                                    languageMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.language_english)) },
                                modifier = Modifier.testTag("language_english"),
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
                            selected = current == item && selectedBookId == null,
                            onClick = { selectedBookId = null; destination = item.name },
                            icon = { Text(item.glyph, style = MaterialTheme.typography.titleMedium) },
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
            } else when (current) {
                AppDestination.HOME -> HomeScreen(
                    onOpenBooks = { destination = AppDestination.BOOKS.name },
                    modifier = contentModifier
                )
                AppDestination.BOOKS -> BooksScreen(
                    onSelectBook = { selectedBookId = it },
                    modifier = contentModifier
                )
                AppDestination.SEARCH -> CatalogueSearchScreen(
                    onSelectBook = { selectedBookId = it },
                    modifier = contentModifier
                )
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
}

@Composable
private fun MoreScreen(
    selected: MoreDestination,
    onSelect: (MoreDestination) -> Unit,
    repository: StudyDatabase,
    onOpenBook: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
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
