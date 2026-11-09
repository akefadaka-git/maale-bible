package org.maale.bible.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.maale.bible.data.catalog.CanonicalBookCatalog
import org.maale.bible.ui.theme.MaaleBibleColors

/**
 * Immersive reader shell. It intentionally renders no passage until the
 * separately authorized and verified scripture corpus is available.
 */
@Composable
fun ReaderPlaceholderScreen(
    bookId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val book = CanonicalBookCatalog.find(bookId)
    var readingFontSize by remember { mutableFloatStateOf(20f) }
    var focusMode by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (!focusMode) {
            ReaderTopBar(
                title = book?.englishName ?: bookId,
                onBack = onBack
            )
        }

        Column(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = if (focusMode) 28.dp else 18.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (!focusMode) {
                ReaderBookIdentity(
                    bookName = book?.englishName ?: bookId,
                    usfmId = book?.usfmId ?: bookId
                )
                ReaderControlPanel(
                    fontSize = readingFontSize,
                    onDecreaseFont = { readingFontSize = (readingFontSize - 1f).coerceAtLeast(16f) },
                    onIncreaseFont = { readingFontSize = (readingFontSize + 1f).coerceAtMost(32f) },
                    onToggleFocus = { focusMode = true }
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = { focusMode = false }) {
                        Text("Exit focus mode")
                    }
                }
            }

            ScriptureUnavailablePanel(fontSize = readingFontSize)

            if (!focusMode) {
                ReaderNavigationUnavailable()
            }
        }
    }
}

@Composable
private fun ReaderTopBar(title: String, onBack: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Text("←", style = MaterialTheme.typography.headlineSmall)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "BIBLE READER",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
private fun ReaderBookIdentity(bookName: String, usfmId: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "THE SCRIPTURE COLLECTION",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = bookName,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "CANONICAL BOOK ID  •  $usfmId",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ReaderControlPanel(
    fontSize: Float,
    onDecreaseFont: () -> Unit,
    onIncreaseFont: () -> Unit,
    onToggleFocus: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Aa",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDecreaseFont, enabled = fontSize > 16f) {
                Text("A−", color = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                text = "${fontSize.toInt()} sp",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(onClick = onIncreaseFont, enabled = fontSize < 32f) {
                Text("A+", color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.weight(1f))
            OutlinedButton(onClick = onToggleFocus) {
                Text("Focus")
            }
        }
    }
}

@Composable
private fun ScriptureUnavailablePanel(fontSize: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)
                        )
                    )
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "✦",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "A quiet space for the Word",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "The verified Maale scripture text is not available in this build yet.",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = "CONTENT NOT LOADED",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "No sample or reconstructed verses are displayed. Chapter and verse controls will activate when the authorized source corpus is integrated and validated.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReaderNavigationUnavailable() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "CHAPTER NAVIGATION",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Chapter and verse navigation is waiting for verified book structure.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Button(
                onClick = {},
                enabled = false,
                colors = ButtonDefaults.buttonColors()
            ) {
                Text("Unavailable until source integration")
            }
        }
    }
}
