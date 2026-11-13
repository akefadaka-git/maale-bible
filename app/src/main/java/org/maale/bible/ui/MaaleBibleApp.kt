package org.maale.bible.ui

import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
undefinedimport androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
        Configuration(baseConfiguration).apply { setLocales(LocaleList.forLanguageTags(language.tag)) }
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
        var languageMenuExpanded by rememberSaveable { mutableStateOf(false) }
        var notificationsExpanded by rememberSaveable { mutableStateOf(false) }
        var profileExpanded by rememberSaveable { mutableStateOf(false) }
        val current = AppDestination.valueOf(destination)
        val expandedLayout = LocalConfiguration.current.screenWidthDp >= 840

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                            MaaleBibleBrandSymbol(size = 38.dp)
                            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium)
                                Text(
                                    stringResource(R.string.home_living_experience),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { notificationsExpanded = true },
                            modifier = Modifier.testTag("notifications_button").semantics {
                                contentDescription = context.getString(R.string.header_notifications)
                            }
                        ) { MaaleBellGlyph() }
                        DropdownMenu(expanded = notificationsExpanded, onDismissRequest = { notificationsExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.header_no_notifications)) },
                                onClick = { notificationsExpanded = false },
                                enabled = false
                            )
                        }
                        IconButton(
                            onClick = { profileExpanded = true },
                            modifier = Modifier.testTag("profile_button").semantics {
                                contentDescription = context.getString(R.string.header_profile)
                            }
                        ) { MaaleProfileGlyph() }
                        DropdownMenu(expanded = profileExpanded, onDismissRequest = { profileExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.header_guest_mode)) },
                                onClick = { profileExpanded = false },
                                enabled = false
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.header_language)) },
                                onClick = { profileExpanded = false; languageMenuExpanded = true }
                            )
                        }
                        Box {
                            TextButton(
                                onClick = { languageMenuExpanded = true },
                                modifier = Modifier.testTag("language_selector")
                            ) { Text(stringResource(R.string.language_label), maxLines = 1) }
                            DropdownMenu(expanded = languageMenuExpanded, onDismissRequest = { languageMenuExpanded = false }) {
                                if (language == AppLanguage.MAALE) {
                                    Text(
                                        stringResource(R.string.language_fallback_notice),
                                        modifier = Modifier.widthIn(max = 280.dp).padding(horizontal = 16.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.language_maale)) },
                                    modifier = Modifier.testTag("language_maale"),
                                    onClick = { language = AppLanguage.MAALE; AppLanguage.save(context, language); languageMenuExpanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.language_amharic)) },
                                    modifier = Modifier.testTag("language_amharic"),
                                    onClick = { language = AppLanguage.AMHARIC; AppLanguage.save(context, language); languageMenuExpanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.language_english)) },
                                    modifier = Modifier.testTag("language_english"),
                                    onClick = { language = AppLanguage.ENGLISH; AppLanguage.save(context, language); languageMenuExpanded = false }
                                )
                            }
                        }
                    }
                )
            },
            bottomBar = {
                if (!expandedLayout) {
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
            }
        ) { padding ->
            Row(Modifier.fillMaxSize().padding(padding)) {
                if (expandedLayout) {
                    NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
                        Spacer(Modifier.height(8.dp))
                        AppDestination.entries.forEach { item ->
                            NavigationRailItem(
                                selected = current == item && selectedBookId == null,
                                onClick = { selectedBookId = null; destination = item.name },
                                icon = { Text(item.glyph, style = MaterialTheme.typography.titleLarge) },
                                label = { Text(stringResource(item.labelRes), maxLines = 1) },
                                alwaysShowLabel = true
                            )
                        }
                    }
                }
                Box(Modifier.weight(1f).fillMaxHeight()) {
                    if (selectedBookId != null) {
                        ReaderPlaceholderScreen(
                            bookId = selectedBookId.orEmpty(),
                            repository = studyRepository,
                            onBack = { selectedBookId = null; destination = AppDestination.BOOKS.name },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else when (current) {
                        AppDestination.HOME -> HomeScreen(onOpenBooks = { destination = AppDestination.BOOKS.name })
                        AppDestination.BOOKS -> BooksScreen(onSelectBook = { selectedBookId = it })
                        AppDestination.SEARCH -> CatalogueSearchScreen(onSelectBook = { selectedBookId = it })
                        AppDestination.MORE -> MoreScreen(
                            selected = MoreDestination.valueOf(moreDestination),
                            onSelect = { moreDestination = it.name },
                            repository = studyRepository,
                            onOpenBook = { selectedBookId = it },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MaaleBellGlyph() {
    val ink = MaterialTheme.colorScheme.onSurface
    val accent = MaterialTheme.colorScheme.primary
    Canvas(Modifier.size(23.dp).semantics { contentDescription = "Notifications" }) {
        val p = Path().apply {
            moveTo(size.width * .5f, size.height * .13f)
            cubicTo(size.width * .29f, size.height * .13f, size.width * .22f, size.height * .31f, size.width * .22f, size.height * .5f)
            lineTo(size.width * .18f, size.height * .75f)
            lineTo(size.width * .82f, size.height * .75f)
            lineTo(size.width * .78f, size.height * .5f)
            cubicTo(size.width * .78f, size.height * .31f, size.width * .71f, size.height * .13f, size.width * .5f, size.height * .13f)
        }
        drawPath(p, ink, style = Stroke(width = 1.8.dp.toPx()))
        drawLine(ink, androidx.compose.ui.geometry.Offset(size.width * .43f, size.height * .84f), androidx.compose.ui.geometry.Offset(size.width * .57f, size.height * .84f), 1.8.dp.toPx())
        drawCircle(accent, radius = 1.6.dp.toPx(), center = androidx.compose.ui.geometry.Offset(size.width * .5f, size.height * .08f))
    }
}

@Composable
private fun MaaleProfileGlyph() {
    val ink = MaterialTheme.colorScheme.onSurface
    Canvas(Modifier.size(23.dp).semantics { contentDescription = "Profile" }) {
        drawCircle(ink, radius = size.minDimension * .18f, center = androidx.compose.ui.geometry.Offset(size.width * .5f, size.height * .31f), style = Stroke(width = 1.8.dp.toPx()))
        val shoulders = Path().apply {
            moveTo(size.width * .16f, size.height * .9f)
            cubicTo(size.width * .18f, size.height * .62f, size.width * .82f, size.height * .62f, size.width * .84f, size.height * .9f)
        }
        drawPath(shoulders, ink, style = Stroke(width = 1.8.dp.toPx()))
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
        modifier = modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(stringResource(R.string.nav_more), style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MoreDestination.entries.forEach { item ->
                val label = when (item) {
                    MoreDestination.LISTEN -> R.string.nav_listen
                    MoreDestination.STUDY -> R.string.nav_study
                    MoreDestination.SETTINGS -> R.string.nav_settings
                }
                FilterChip(
                    selected = selected == item,
                    onClick = { onSelect(item) },
                    label = { Text(stringResource(label), maxLines = 1) }
                )
            }
        }
        when (selected) {
            MoreDestination.LISTEN -> AudioPlaceholderScreen(Modifier.weight(1f))
            MoreDestination.STUDY -> StudyHubScreen(repository, onOpenBook, Modifier.weight(1f))
            MoreDestination.SETTINGS -> SettingsScreen(repository, Modifier.weight(1f))
        }
    }
}
