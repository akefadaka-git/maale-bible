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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
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
fun BooksScreen(onSelectBook: (String) -> Unit, modifier: Modifier = Modifier) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("all") }
    val matchingBooks = CanonicalBookCatalog.books.filter { book ->
        val term = query.trim()
        val matchesQuery = term.isBlank() || book.englishName.contains(term, true) || book.usfmId.contains(term, true)
        val matchesCategory = selectedCategory == "all" || bookCategories.firstOrNull { it.id == selectedCategory }?.range?.contains(book.order) == true
        matchesQuery && matchesCategory
    }
    val groupedBooks = matchingBooks.groupBy { book -> bookCategories.first { book.order in it.range } }

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
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("66", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(stringResource(R.string.explorer_catalogue_count), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(stringResource(R.string.explorer_catalogue_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
        OutlinedTextField(
            value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Search Bible books by name or canonical code" },
            singleLine = true, label = { Text(stringResource(R.string.explorer_search_label)) },
            placeholder = { Text(stringResource(R.string.explorer_search_hint)) },
            leadingIcon = { Text("⌕", style = MaterialTheme.typography.headlineSmall) },
            trailingIcon = { if (query.isNotEmpty()) Text("×", Modifier.clickable { query = "" }.padding(horizontal = 14.dp), style = MaterialTheme.typography.titleLarge) }
        )
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = selectedCategory == "all", onClick = { selectedCategory = "all" }, label = { Text(stringResource(R.string.explorer_all_categories)) })
            bookCategories.forEach { category ->
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
        LazyColumn(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (matchingBooks.isEmpty()) item {
                Text(stringResource(R.string.explorer_no_results), Modifier.fillMaxWidth().padding(24.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            bookCategories.filter { groupedBooks.containsKey(it) }.forEach { category ->
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
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).semantics { contentDescription = "Open " + book.englishName },
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
