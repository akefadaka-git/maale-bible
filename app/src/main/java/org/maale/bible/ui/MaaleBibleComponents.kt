package org.maale.bible.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Shared brand primitive used by the app shell and feature surfaces. */
@Composable
fun MaaleBibleBrandSymbol(modifier: Modifier = Modifier, size: Dp = 40.dp) {
    Box(
        modifier.size(size).clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color(0xFF173F38), Color(0xFF0B1020)))),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize().padding(size * .18f)) {
            val stroke = Stroke(width = this.size.minDimension * .075f)
            val p = Path().apply {
                moveTo(size.width * .08f, size.height * .2f)
                cubicTo(size.width * .32f, size.height * .12f, size.width * .43f, size.height * .25f, size.width * .5f, size.height * .38f)
                cubicTo(size.width * .57f, size.height * .25f, size.width * .68f, size.height * .12f, size.width * .92f, size.height * .2f)
                lineTo(size.width * .92f, size.height * .8f)
                cubicTo(size.width * .68f, size.height * .7f, size.width * .57f, size.height * .8f, size.width * .5f, size.height * .92f)
                cubicTo(size.width * .43f, size.height * .8f, size.width * .32f, size.height * .7f, size.width * .08f, size.height * .8f)
                close()
            }
            drawPath(p, Color(0xFFD9B76E), style = stroke)
            drawLine(Color(0xFFD9B76E), androidx.compose.ui.geometry.Offset(size.width * .5f, size.height * .38f), androidx.compose.ui.geometry.Offset(size.width * .5f, size.height * .9f), stroke.width)
        }
    }
}

@Composable
fun MaaleBibleSectionHeading(eyebrow: String, title: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(eyebrow.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
        Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun MaaleBibleStatusBanner(label: String, body: String, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.large) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
            Box(Modifier.size(8.dp).padding(top = 3.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
    }
}

@Composable
fun MaaleBibleDecorativeMapSurface(modifier: Modifier = Modifier) {
    Box(
        modifier.clip(MaterialTheme.shapes.large)
            .background(Brush.linearGradient(listOf(Color(0xFF173F38), Color(0xFF24334C))))
            .semantics { contentDescription = "Decorative map preview; no geographic data" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize().padding(12.dp)) {
            val p = Path().apply {
                moveTo(size.width * .08f, size.height * .22f)
                lineTo(size.width * .38f, size.height * .12f)
                lineTo(size.width * .5f, size.height * .34f)
                lineTo(size.width * .82f, size.height * .2f)
                lineTo(size.width * .92f, size.height * .56f)
                lineTo(size.width * .68f, size.height * .88f)
                lineTo(size.width * .34f, size.height * .78f)
                close()
            }
            drawPath(p, Color(0xFF77B7A2).copy(alpha = .7f))
            drawCircle(Color(0xFFD9B76E), radius = 3.dp.toPx(), center = androidx.compose.ui.geometry.Offset(size.width * .52f, size.height * .48f))
            drawPath(Path().apply { moveTo(size.width * .18f, size.height * .55f); cubicTo(size.width * .38f, size.height * .38f, size.width * .62f, size.height * .72f, size.width * .84f, size.height * .4f) }, Color.White.copy(alpha = .4f), style = Stroke(width = 1.dp.toPx()))
        }
    }
}
