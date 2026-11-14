package org.maale.bible.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import org.maale.bible.R
import org.maale.bible.ui.theme.MaaleBibleColors

@Composable
fun HomeScreen(onOpenBooks: () -> Unit, modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val wideLayout = maxWidth >= 600.dp
        Column(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (wideLayout) 32.dp else 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            HomeHeader()
            LivingWordHero(onOpenBooks = onOpenBooks)
            DailyScriptureCard()
            SectionHeading(
                eyebrow = stringResource(R.string.home_make_it_yours),
                title = stringResource(R.string.home_reading_journey)
            )
            ReadingJourneyCard(onOpenBooks = onOpenBooks)
            SectionHeading(
                eyebrow = stringResource(R.string.home_explore),
                title = stringResource(R.string.home_world_of_scripture)
            )
            if (wideLayout) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExploreCard(
                        title = stringResource(R.string.home_catalogue),
                        description = stringResource(R.string.home_browse_catalogue),
                        marker = "01",
                        modifier = Modifier.weight(1f)
                    )
                    ExploreCard(
                        title = stringResource(R.string.home_study_space),
                        description = stringResource(R.string.home_study_description),
                        marker = "02",
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExploreCard(
                        title = stringResource(R.string.home_catalogue),
                        description = stringResource(R.string.home_browse_catalogue),
                        marker = "01"
                    )
                    ExploreCard(
                        title = stringResource(R.string.home_study_space),
                        description = stringResource(R.string.home_study_description),
                        marker = "02"
                    )
                }
            }
            Text(
                text = stringResource(R.string.home_living_word_experience),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 12.dp)
            )
        }
    }
}

@Composable
private fun HomeHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text = "ጌኤዦ ማፃኣፖ",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "MAALE BIBLE",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(9.dp))
            Box(
                Modifier
                    .size(4.dp)
                    .background(MaterialTheme.colorScheme.secondary, CircleShape)
            )
            Spacer(Modifier.width(9.dp))
            Text(
                text = stringResource(R.string.home_living_word_experience),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LivingWordHero(onOpenBooks: () -> Unit) {
    val shape = RoundedCornerShape(28.dp)
    // Keep decorative artwork static: an endless animation prevents UI-test and
    // accessibility idling resources from reaching a stable idle state.
    val orbitAlpha = 0.42f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF18243D),
                        Color(0xFF26324A),
                        Color(0xFF123B3A)
                    )
                )
            )
            .padding(22.dp)
    ) {
        // Decorative orbital rings: static, lightweight and excluded from accessibility.
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 2.dp, end = 2.dp)
                .size(116.dp)
                .border(1.dp, MaaleBibleColors.RoyalGold.copy(alpha = orbitAlpha), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 17.dp, end = 17.dp)
                .size(86.dp)
                .border(1.dp, MaaleBibleColors.LivingCyan.copy(alpha = 0.68f - orbitAlpha / 2f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 42.dp, end = 42.dp)
                .size(36.dp)
                .background(
                    Brush.radialGradient(
                        listOf(MaaleBibleColors.RoyalGold, MaaleBibleColors.RoyalGold.copy(alpha = 0.08f))
                    ),
                    CircleShape
                )
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.home_the_living_word),
                style = MaterialTheme.typography.labelMedium,
                color = MaaleBibleColors.LivingCyan,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.home_pause_read_reflect),
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFFF5F1E8),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.home_make_space),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE0E5ED),
                modifier = Modifier.fillMaxWidth(0.82f)
            )
            Spacer(Modifier.height(2.dp))
            Button(
                onClick = onOpenBooks,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaaleBibleColors.RoyalGold,
                    contentColor = MaaleBibleColors.Midnight
                )
            ) {
                Text(stringResource(R.string.home_explore_bible), fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
                Text("→", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DailyScriptureCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "✦",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.width(9.dp))
                Text(
                    text = stringResource(R.string.home_daily_scripture),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = stringResource(R.string.home_quiet_place),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.home_passage_pending),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.home_content_not_loaded),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SectionHeading(eyebrow: String, title: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = eyebrow,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ReadingJourneyCard(onOpenBooks: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "↗",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.home_begin_journey),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.home_choose_book),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(onClick = onOpenBooks) {
                Text(stringResource(R.string.home_start))
            }
        }
    }
}

@Composable
private fun ExploreCard(
    title: String,
    description: String,
    marker: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = marker,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
