package org.maale.bible.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.maale.bible.R
import org.maale.bible.data.catalog.CanonicalBookCatalog
import org.maale.bible.data.content.BookInductiveStudy
import org.maale.bible.data.content.BookLearningContent
import org.maale.bible.data.content.BookLearningContentRepository
import org.maale.bible.data.content.BookQuizQuestion

private enum class BookLearningTab(val label: Int) {
    OVERVIEW(R.string.learning_tab_overview),
    INDUCTIVE(R.string.learning_tab_inductive),
    QUIZ(R.string.learning_tab_quiz)
}

@Composable
fun BookLearningScreen(
    bookId: String,
    onBack: () -> Unit,
    onOpenReader: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val book = remember(bookId) { CanonicalBookCatalog.find(bookId) }
    val content = remember(bookId, context) { BookLearningContentRepository.load(context, bookId) }
    var selectedTab by rememberSaveable(bookId) { mutableStateOf(BookLearningTab.OVERVIEW) }
    val title = book?.englishName ?: bookId
    val testament = if ((book?.order ?: 0) <= 39) stringResource(R.string.explorer_old_testament) else stringResource(R.string.explorer_new_testament)

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onBack) { Text("‹  " + stringResource(R.string.learning_back_to_books)) }
            Spacer(Modifier.weight(1f))
            Text(testament, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
        }
        Text(title, style = MaterialTheme.typography.headlineLarge)
        Text(
            stringResource(R.string.learning_book_metadata, book?.usfmId ?: bookId, book?.order ?: 0),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.learning_centre_title), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(stringResource(R.string.learning_centre_description), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(stringResource(R.string.learning_bilingual_status), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        TabRow(selectedTabIndex = selectedTab.ordinal) {
            BookLearningTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(stringResource(tab.label)) }
                )
            }
        }
        when (selectedTab) {
            BookLearningTab.OVERVIEW -> OverviewContent(content, bookId, onOpenReader)
            BookLearningTab.INDUCTIVE -> InductiveContent(content)
            BookLearningTab.QUIZ -> QuizContent(content)
        }
        Text(
            stringResource(R.string.learning_provenance_notice),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OverviewContent(content: BookLearningContent?, bookId: String, onOpenReader: (String) -> Unit) {
    if (content == null) {
        LearningSectionCard(
            title = stringResource(R.string.learning_introduction_title),
            description = stringResource(R.string.learning_introduction_pending)
        )
        LearningSectionCard(stringResource(R.string.learning_background_title), stringResource(R.string.learning_background_fields))
        LearningSectionCard(stringResource(R.string.learning_structure_title), stringResource(R.string.learning_structure_fields))
        LearningSectionCard(stringResource(R.string.learning_themes_title), stringResource(R.string.learning_themes_fields))
        LearningSectionCard(stringResource(R.string.learning_related_title), stringResource(R.string.learning_related_fields))
    } else {
        DraftWarning(content)
        val intro = content.introduction
        LearningSectionCard(intro.title, intro.summary)
        LearningSectionCard(stringResource(R.string.learning_background_title), intro.authorship)
        LearningSectionCard(stringResource(R.string.learning_setting_title), intro.setting)
        LearningSectionCard(stringResource(R.string.learning_audience_title), intro.audienceAndPurpose)
        LearningListCard(
            stringResource(R.string.learning_structure_title),
            intro.structure.map { "${it.range}: ${it.heading} — ${it.summary}" }
        )
        LearningListCard(stringResource(R.string.learning_themes_title), intro.themes)
        LearningListCard(stringResource(R.string.learning_key_people_title), intro.keyPeople)
        LearningListCard(stringResource(R.string.learning_related_title), intro.keyPassages)
        LearningListCard(stringResource(R.string.learning_interpretive_care_title), intro.interpretiveCare)
        Text(
            stringResource(R.string.learning_source_count, content.sourceCount),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
    Button(onClick = { onOpenReader(bookId) }, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.learning_open_reader))
    }
}

@Composable
private fun InductiveContent(content: BookLearningContent?) {
    if (content == null || content.inductiveStudies.isEmpty()) {
        Text(stringResource(R.string.learning_inductive_intro), style = MaterialTheme.typography.bodyMedium)
        LearningSectionCard(stringResource(R.string.learning_observation_title), stringResource(R.string.learning_observation_prompts))
        LearningSectionCard(stringResource(R.string.learning_interpretation_title), stringResource(R.string.learning_interpretation_prompts))
        LearningSectionCard(stringResource(R.string.learning_application_title), stringResource(R.string.learning_application_prompts))
        LearningSectionCard(stringResource(R.string.learning_study_guide_pending_title), stringResource(R.string.learning_study_guide_pending))
    } else {
        DraftWarning(content)
        Text(stringResource(R.string.learning_inductive_intro), style = MaterialTheme.typography.bodyMedium)
        content.inductiveStudies.forEach { study -> InductiveStudyCard(study) }
    }
}

@Composable
private fun InductiveStudyCard(study: BookInductiveStudy) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(study.range, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
        Text(study.title, style = MaterialTheme.typography.titleLarge)
        LearningListCard(stringResource(R.string.learning_observation_title), study.observation)
        LearningListCard(stringResource(R.string.learning_interpretation_title), study.interpretation)
        LearningListCard(stringResource(R.string.learning_application_title), study.application)
    }
}

@Composable
private fun QuizContent(content: BookLearningContent?) {
    val questions = content?.quizQuestions.orEmpty()
    if (content == null || questions.isEmpty()) {
        LearningSectionCard(stringResource(R.string.learning_quiz_title), stringResource(R.string.learning_quiz_pending))
        Text(stringResource(R.string.learning_quiz_unavailable), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
    } else {
        DraftWarning(content)
        var answers by remember(content.bookId, content.locale) { mutableStateOf<Map<String, String>>(emptyMap()) }
        var submitted by remember(content.bookId, content.locale) { mutableStateOf<Set<String>>(emptySet()) }
        val correctCount = questions.count { question ->
            val answer = answers[question.id]
            question.id in submitted && quizAnswerIsCorrect(question, answer)
        }
        Text(stringResource(R.string.learning_quiz_score, submitted.size, correctCount), style = MaterialTheme.typography.titleSmall)
        questions.forEach { question ->
            QuizQuestionCard(
                question = question,
                selectedAnswer = answers[question.id],
                submitted = question.id in submitted,
                onSelect = { value ->
                    answers = answers + (question.id to value)
                    submitted = submitted - question.id
                },
                onSubmit = { submitted = submitted + question.id }
            )
        }
    }
}

@Composable
private fun QuizQuestionCard(
    question: BookQuizQuestion,
    selectedAnswer: String?,
    submitted: Boolean,
    onSelect: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val correct = quizAnswerIsCorrect(question, selectedAnswer)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(question.id, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                Text(question.difficulty, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            }
            Text(question.prompt, style = MaterialTheme.typography.titleMedium)
            if (question.type == "true_false") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedAnswer == "true",
                        onClick = { onSelect("true") },
                        label = { Text(stringResource(R.string.learning_true)) }
                    )
                    FilterChip(
                        selected = selectedAnswer == "false",
                        onClick = { onSelect("false") },
                        label = { Text(stringResource(R.string.learning_false)) }
                    )
                }
            } else {
                question.options.forEachIndexed { index, option ->
                    val optionId = ('A'.code + index).toChar().toString()
                    Row(
                        Modifier.fillMaxWidth().clickable { onSelect(optionId) }.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedAnswer == optionId, onClick = { onSelect(optionId) })
                        Text(option, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Button(onClick = onSubmit, enabled = selectedAnswer != null && !submitted, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.learning_check_answer))
            }
            if (submitted) {
                Text(
                    stringResource(if (correct) R.string.learning_answer_correct else R.string.learning_answer_incorrect),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (correct) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Text(question.explanation, style = MaterialTheme.typography.bodyMedium)
                if (question.references.isNotEmpty()) {
                    Text(
                        stringResource(R.string.learning_answer_references, question.references.joinToString()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

private fun quizAnswerIsCorrect(question: BookQuizQuestion, answer: String?): Boolean {
    if (answer == null) return false
    return if (question.type == "true_false") {
        question.correctAnswer?.toString() == answer
    } else {
        question.correctOptionId == answer
    }
}

@Composable
private fun DraftWarning(content: BookLearningContent) {
    if (!content.isApproved) {
        Text(
            stringResource(R.string.learning_draft_warning),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun LearningListCard(title: String, items: List<String>) {
    if (items.isEmpty()) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            items.forEach { item ->
                Text("•  $item", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LearningSectionCard(title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
