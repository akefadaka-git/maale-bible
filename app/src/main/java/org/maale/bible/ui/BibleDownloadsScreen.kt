package org.maale.bible.ui

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.res.stringResource
import org.maale.bible.R
import androidx.compose.ui.unit.dp
import org.maale.bible.data.catalog.BibleEditionCatalog
import org.maale.bible.data.catalog.BibleEditionSource
import org.maale.bible.data.catalog.EditionReadiness

@Composable
fun BibleDownloadsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val eligible = BibleEditionCatalog.eligibleForPackage().groupBy { it.languageName }
    val pending = BibleEditionCatalog.awaitingReview().groupBy { it.languageName }

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.library_title), style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary)
        Text(stringResource(R.string.library_choose_editions), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(R.string.library_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyColumn(
            modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            eligible.forEach { (language, editions) ->
                item(key = "eligible-$language") {
                    Text("$language · " + stringResource(R.string.library_open_license_sources).uppercase(), style = MaterialTheme.typography.titleSmall)
                }
                items(editions, key = { it.id }) { edition ->
                    EditionCard(edition) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(edition.downloadUrl ?: edition.sourceUrl)))
                    }
                }
            }
            pending.forEach { (language, editions) ->
                item(key = "pending-$language") {
                    Text("$language · " + stringResource(R.string.library_review_before_install).uppercase(), style = MaterialTheme.typography.titleSmall)
                }
                items(editions, key = { it.id }) { edition ->
                    EditionCard(edition, reviewOnly = true) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(edition.sourceUrl)))
                    }
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
                    if (reviewOnly) stringResource(R.string.library_review_required).uppercase() else when (edition.readiness) {
                        EditionReadiness.PARTIAL_TEXT -> stringResource(R.string.library_partial).uppercase()
                        else -> stringResource(R.string.library_open_source).uppercase()
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
                Text(stringResource(R.string.library_incomplete_notice),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            if (reviewOnly) {
                Text(stringResource(R.string.library_install_blocked),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            Button(onClick = onOpenSource) {
                Text(if (reviewOnly) stringResource(R.string.library_view_source_license) else if (edition.downloadUrl != null) stringResource(R.string.library_download_package) else stringResource(R.string.library_open_official_source))
            }
        }
    }
}
