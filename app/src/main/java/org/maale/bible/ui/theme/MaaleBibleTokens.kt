package org.maale.bible.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

/**
 * Maale Bible's visual foundation.
 *
 * Keep these values semantic and centralized. Scripture content, language-specific
 * font selection, and decorative motion are intentionally handled by later UI slices.
 */
object MaaleBibleColors {
    // Brand anchors
    val Midnight = Color(0xFF0B1020)
    val DeepIndigo = Color(0xFF171D35)
    val RoyalGold = Color(0xFFD9B76E)
    val LivingCyan = Color(0xFF55D6D0)
    val ScriptureWhite = Color(0xFFF5F1E8)
    val MutedText = Color(0xFFA6ADBC)

    // Dark surfaces
    val DarkBackground = Color(0xFF0B1020)
    val DarkSurface = Color(0xFF111A2C)
    val DarkSurfaceVariant = Color(0xFF1C2940)
    val DarkOutline = Color(0xFF56627A)

    // Light surfaces
    val LightBackground = Color(0xFFF7F4ED)
    val LightSurface = Color(0xFFFFFEFA)
    val LightSurfaceVariant = Color(0xFFECE7DC)
    val LightOnSurface = Color(0xFF202536)
    val LightOnSurfaceVariant = Color(0xFF51596A)
    val LightPrimary = Color(0xFF75551A)
    val LightSecondary = Color(0xFF006B67)
    val LightOutline = Color(0xFF777363)

    // Status colors are semantic, not decorative.
    val Success = Color(0xFF3D8067)
    val Warning = Color(0xFF9A681B)
    val Error = Color(0xFFBA4545)
}

object MaaleBibleSpacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    val section = 40.dp
}

object MaaleBibleRadii {
    val compact = 12.dp
    val control = 16.dp
    val card = 22.dp
    val hero = 28.dp
    val pill = 50.dp
}

val MaaleBibleShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(MaaleBibleRadii.compact),
    medium = RoundedCornerShape(MaaleBibleRadii.control),
    large = RoundedCornerShape(MaaleBibleRadii.card),
    extraLarge = RoundedCornerShape(MaaleBibleRadii.hero)
)
