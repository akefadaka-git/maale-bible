package org.maale.bible.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onOpenBooks: () -> Unit,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    "ጌኤዦ ማፃኣፖ",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    "MAALE BIBLE",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Surface(
                shape = CircleShape,
                color = colors.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("✝", style = MaterialTheme.typography.headlineSmall, color = colors.onPrimaryContainer)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = colors.primary)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "YOUR SCRIPTURE, YOUR LANGUAGE",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onPrimary.copy(alpha = 0.78f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Read. Reflect. Grow.",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.onPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "A calm, focused home for the Maale Bible and your personal study.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onPrimary.copy(alpha = 0.9f)
                )
                Button(
                    onClick = onOpenBooks,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.surface,
                        contentColor = colors.primary
                    )
                ) {
                    Text("Explore the Bible")
                    Text("  →")
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Continue your journey", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Card(
                onClick = onOpenBooks,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(shape = RoundedCornerShape(14.dp), color = colors.secondaryContainer) {
                        Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                            Text("▤", style = MaterialTheme.typography.headlineSmall, color = colors.onSecondaryContainer)
                        }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("Browse all books", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Explore the canonical 66-book catalogue", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                    Text("›", style = MaterialTheme.typography.headlineMedium, color = colors.primary)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Study tools", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickAction(
                    symbol = "⌕",
                    title = "Search",
                    detail = "Find passages",
                    onClick = onOpenSearch,
                    modifier = Modifier.weight(1f)
                )
                QuickAction(
                    symbol = "▧",
                    title = "Books",
                    detail = "Browse the catalogue",
                    onClick = onOpenBooks,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = colors.surfaceVariant.copy(alpha = 0.7f)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("A note about this edition", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "The app currently provides the book catalogue. Scripture text and content-based study features will appear only after their approved offline integration is complete.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun QuickAction(
    symbol: String,
    title: String,
    detail: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(symbol, style = MaterialTheme.typography.headlineMedium, color = colors.primary)
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
    }
}
