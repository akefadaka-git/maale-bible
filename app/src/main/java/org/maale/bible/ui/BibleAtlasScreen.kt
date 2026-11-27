package org.maale.bible.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.maale.bible.R

/**
 * Bible Atlas shell. The contour artwork is decorative only; no geographic
 * locations, borders, coordinates or routes are asserted until licensed
 * source data has been reviewed and integrated.
 */
@Composable
fun BibleAtlasScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth().align(Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(stringResource(R.string.atlas_title), style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground)
            Text(stringResource(R.string.atlas_subtitle), style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Card(
            modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth().align(Alignment.CenterHorizontally),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.atlas_map_placeholder),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary)
                val contourTint = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                Box(
                    modifier = Modifier.fillMaxWidth().height(250.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .semantics { contentDescription = "Decorative atlas preview; no geographic data loaded" },
                    contentAlignment = Alignment.Center
                ) {
                    // Decorative contour rings only; not a geographic map.
                    Canvas(Modifier.fillMaxSize()) {
                        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2.dp.toPx())
                        val tint = contourTint
                        val centerX = size.width * 0.53f
                        val centerY = size.height * 0.48f
                        for (index in 0..5) {
                            drawCircle(
                                color = tint,
                                radius = size.minDimension * (0.12f + index * 0.075f),
                                center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                                style = stroke
                            )
                        }
                        for (index in 0..6) {
                            val y = size.height * (0.12f + index * 0.12f)
                            drawLine(tint.copy(alpha = 0.45f), androidx.compose.ui.geometry.Offset(0f, y),
                                androidx.compose.ui.geometry.Offset(size.width, y), strokeWidth = 0.6.dp.toPx())
                        }
                    }
                    Text("⌖", style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f))
                }
                Text(stringResource(R.string.atlas_source_pending),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.atlas_locations_pending),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.atlas_no_fabricated_data),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary)
                Text(stringResource(R.string.atlas_map_legend),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
