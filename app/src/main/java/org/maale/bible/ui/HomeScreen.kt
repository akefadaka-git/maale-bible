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
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("ጌኤዦ ማፃኣፖ", style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold, color = colors.onSurface)
                Text("MAALE BIBLE  ·  WORD FOR LIFE", style = MaterialTheme.typography.labelSmall,
                    color = colors.primary, fontWeight = FontWeight.Bold)
            }
            Surface(shape = CircleShape, color = colors.primaryContainer, modifier = Modifier.size(52.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text("✝", style = MaterialTheme.typography.headlineMedium, color = colors.onPrimaryContainer)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = colors.primary)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(shape = RoundedCornerShape(50), color = colors.onPrimary.copy(alpha = 0.14f)) {
                    Text("✦  YOUR FAITH. YOUR LANGUAGE.", modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelSmall, color = colors.onPrimary, fontWeight = FontWeight.Bold)
                }
                Text("Let the Word
light your way.", style = MaterialTheme.typography.headlineLarge,
                    color = colors.onPrimary, fontWeight = FontWeight.Bold)
                Text("A dedicated home for the Maale Bible, reading and reflection.",
                    style = MaterialTheme.typography.bodyLarge, color = colors.onPrimary.copy(alpha = 0.88f))
                Button(onClick = onOpenBooks, shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.surface, contentColor = colors.primary)) {
                    Text("Explore the Bible", fontWeight = FontWeight.Bold)
                    Text("   →")
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Your library", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Card(
                onClick = onOpenBooks, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)
            ) {
                Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Surface(shape = RoundedCornerShape(17.dp), color = colors.secondaryContainer) {
                        Box(Modifier.size(54.dp), contentAlignment = Alignment.Center) {
                            Text("▤", style = MaterialTheme.typography.headlineMedium, color = colors.onSecondaryContainer)
                        }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Browse all books", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Explore the canonical 66-book catalogue", style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant)
                    }
                    Text("›", style = MaterialTheme.typography.headlineLarge, color = colors.primary)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Study tools", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickAction("⌕", "Search", "Find passages", onOpenSearch, Modifier.weight(1f))
                QuickAction("▧", "Books", "Browse library", onOpenBooks, Modifier.weight(1f))
            }
        }

        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = colors.surfaceVariant.copy(alpha = 0.72f)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("A trusted edition", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("This build provides the book catalogue. Verified scripture text and content-based study tools will be enabled after approved offline content integration.",
                    style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun QuickAction(symbol: String, title: String, detail: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Card(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Surface(shape = RoundedCornerShape(13.dp), color = colors.primaryContainer) {
                Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                    Text(symbol, style = MaterialTheme.typography.headlineSmall, color = colors.onPrimaryContainer)
                }
            }
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
    }
}
