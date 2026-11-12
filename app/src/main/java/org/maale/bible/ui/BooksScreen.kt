package org.maale.bible.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.maale.bible.R
import org.maale.bible.data.catalog.CanonicalBookCatalog
import org.maale.bible.domain.model.Book

private data class BookCategory(val id: String, val titleRes: Int, val subtitleRes: Int, val symbol: String, val range: IntRange)

private val bookCategories = listOf(
    BookCategory("law", R.string.explorer_category_law, R.string.explorer_category_law_desc, "📜", 1..5),
    BookCategory("history", R.string.explorer_category_history, R.string.explorer_category_history_desc, "🏛️", 6..17),
    BookCategory("wisdom", R.string.explorer_category_wisdom, R.string.explorer_category_wisdom_desc, "🎼", 18..22),
    BookCategory("major_prophets", R.string.explorer_category_major_prophets, R.string.explorer_category_major_prophets_desc, "📯", 23..27),
    BookCategory("minor_prophets", R.string.explorer_category_minor_prophets, R.string.explorer_category_minor_prophets_desc, "🕊️", 28..39),
    BookCategory("gospels", R.string.explorer_category_gospels, R.string.explorer_category_gospels_desc, "✝️", 40..43),
    BookCategory("acts", R.string.explorer_category_acts, R.string.explorer_category_acts_desc, "🌍", 44..44),
    BookCategory("paul", R.string.explorer_category_paul, R.string.explorer_category_paul_desc, "✉️", 45..57),
    BookCategory("general", R.string.explorer_category_general, R.string.explorer_category_general_desc, "🤝", 58..65),
    BookCategory("revelation", R.string.explorer_category_revelation, R.string.explorer_category_revelation_desc, "👑", 66..66)
)

@Composable
fun BooksScreen(onSelectBook: (String) -> Unit, onOpenAtlas: () -> Unit = {}, modifier: Modifier = Modifier) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedTestament by rememberSaveable { mutableStateOf("ot") }
    var selectedCategory by rememberSaveable { mutableStateOf("all") }
    val testamentBooks = CanonicalBookCatalog.books.filter { book ->
        if (selectedTestament == "ot") book.order in 1..39 else book.order in 40..66
    }
    val visibleCategories = bookCategories.filter { category ->
        if (selectedTestament == "ot") category.range.first <= 39 else category.range.last >= 40
    }
    val matchingBooks = testamentBooks.filter { book ->
        val term = query.trim()
        val matchesQuery = term.isBlank() || book.englishName.contains(term, true) || book.usfmId.contains(term, true)
        val matchesCategory = selectedCategory == "all" || visibleCategories.firstOrNull { it.id == selectedCategory }?.range?.contains(book.order) == true
        matchesQuery && matchesCategory
    }
    val groupedBooks = matchingBooks.groupBy { book -> bookCategories.first { book.order in it.range } }
    val testamentCount = if (selectedTestament == "ot") 39 else 27

    Column(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(54.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                Text("📖", style = MaterialTheme.typography.headlineMedium)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.books_title), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(R.string.explorer_eyebrow), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TestamentCard(
                title = stringResource(R.string.explorer_old_testament),
                count = "39",
                selected = selectedTestament == "ot",
                modifier = Modifier.weight(1f),
                onClick = { selectedTestament = "ot"; selectedCategory = "all" }
            )
            TestamentCard(
                title = stringResource(R.string.explorer_new_testament),
                count = "27",
                selected = selectedTestament == "nt",
                modifier = Modifier.weight(1f),
                onClick = { selectedTestament = "nt"; selectedCategory = "all" }
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(testamentCount.toString(), style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(if (selectedTestament == "ot") stringResource(R.string.explorer_old_testament_desc) else stringResource(R.string.explorer_new_testament_desc), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(stringResource(R.string.explorer_catalogue_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
        TextButton(onClick = onOpenAtlas, modifier = Modifier.fillMaxWidth()) {
            Text("🗺️  " + stringResource(R.string.explorer_open_atlas))
        }
        OutlinedTextField(
            value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth().testTag("books_search").semantics { contentDescription = "Search Bible books by name or canonical code" },
            singleLine = true, label = { Text(stringResource(R.string.explorer_search_label)) },
            placeholder = { Text(stringResource(R.string.explorer_search_hint)) },
            leadingIcon = { Text("⌕", style = MaterialTheme.typography.headlineSmall) },
            trailingIcon = { if (query.isNotEmpty()) Text("×", Modifier.clickable { query = "" }.padding(horizontal = 14.dp), style = MaterialTheme.typography.titleLarge) }
        )
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = selectedCategory == "all", onClick = { selectedCategory = "all" }, label = { Text(stringResource(R.string.explorer_all_categories)) })
            visibleCategories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category.id,
                    onClick = { selectedCategory = if (selectedCategory == category.id) "all" else category.id },
                    label = { Text(stringResource(category.titleRes)) },
                    leadingIcon = { Text(category.symbol) }
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.explorer_results_count, matchingBooks.size), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            Text(stringResource(R.string.explorer_content_status), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LazyColumn(Modifier.fillMaxWidth().weight(1f).testTag("books_list"), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (matchingBooks.isEmpty()) item {
                Text(stringResource(R.string.explorer_no_results), Modifier.fillMaxWidth().padding(24.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            visibleCategories.filter { groupedBooks.containsKey(it) }.forEach { category ->
                item(key = "category-" + category.id) { CategoryHeading(category, groupedBooks[category].orEmpty().size) }
                items(groupedBooks[category].orEmpty(), key = { it.usfmId }) { book ->
                    BookExplorerCard(book, category) { onSelectBook(book.usfmId) }
                }
            }
            item {
                Text(stringResource(R.string.explorer_footer_notice), Modifier.fillMaxWidth().padding(vertical = 12.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun TestamentCard(title: String, count: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(count, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun CategoryHeading(category: BookCategory, count: Int) {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
            Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) { Text(category.symbol, style = MaterialTheme.typography.titleLarge) }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(category.titleRes), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(category.subtitleRes), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(count.toString(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun BookExplorerCard(book: Book, category: BookCategory, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick)
            .testTag("book_card_" + book.usfmId)
            .semantics(mergeDescendants = true) { contentDescription = "Open " + book.englishName; role = Role.Button },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.tertiaryContainer) {
                Box(Modifier.size(38.dp), contentAlignment = Alignment.Center) { Text(category.symbol, style = MaterialTheme.typography.titleMedium) }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(book.englishName, style = MaterialTheme.typography.titleSmall)
                Text(stringResource(R.string.explorer_book_code, book.usfmId, book.order), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.explorer_book_content_pending), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
            Text("›", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}
