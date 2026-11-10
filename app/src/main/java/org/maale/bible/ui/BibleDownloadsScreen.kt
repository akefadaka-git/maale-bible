package org.maale.bible.ui

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.maale.bible.data.catalog.BibleEditionCatalog
import org.maale.bible.data.catalog.BibleEditionSource
import org.maale.bible.data.catalog.EditionReadiness

@Composable
fun BibleDownloadsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val eligible = BibleEditionCatalog.eligibleForPackage()
    val pending = BibleEditionCatalog.awaitingReview()

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("BIBLE LIBRARY", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary)
        Text("Choose your editions.", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Browse free-license Bible editions by language. Each translation has its own licence, source and completeness status.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyColumn(
            modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Text("ENGLISH · OPEN-LICENSE SOURCES", style = MaterialTheme.typography.titleSmall) }
            items(eligible, key = { it.id }) { edition ->
                EditionCard(edition) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(edition.downloadUrl ?: edition.sourceUrl)))
                }
            }
            item { Text("AMHARIC · REVIEW BEFORE INSTALLATION", style = MaterialTheme.typography.titleSmall) }
            items(pending, key = { it.id }) { edition ->
                EditionCard(edition, reviewOnly = true) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(edition.sourceUrl)))
                }
            }
        }
    }
}

@Composable
private fun EditionCard(
    edition: BibleEditionSource,
    reviewOnly: Boolean = false,
    onOpenSource: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(edition.title, style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f))
                Text(
                    if (reviewOnly) "REVIEW REQUIRED" else when (edition.readiness) {
                        EditionReadiness.PARTIAL_TEXT -> "PARTIAL"
                        else -> "OPEN SOURCE"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Text(edition.languageName + " · " + edition.abbreviation,
                style = MaterialTheme.typography.labelMedium)
            Text(edition.license, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary)
            Text(edition.rightsSummary, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(edition.attribution, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (edition.readiness == EditionReadiness.PARTIAL_TEXT) {
                Text("This edition is not a complete Bible release yet.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            if (reviewOnly) {
                Text("Not enabled for in-app installation until editorial or rights review is complete.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            Button(onClick = onOpenSource) {
                Text(if (reviewOnly) "View source and licence" else if (edition.downloadUrl != null) "Download source package" else "Open official source")
            }
        }
    }
}
