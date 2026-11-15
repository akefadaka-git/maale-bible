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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.maale.bible.R
import org.maale.bible.data.catalog.CanonicalBookCatalog
import org.maale.bible.data.content.BookChapterGuide
import org.maale.bible.data.content.BookInductiveStudy
import org.maale.bible.data.content.BookLearningContent
import org.maale.bible.data.content.BookLearningContentRepository
import org.maale.bible.data.content.BookQuizQuestion
import org.maale.bible.data.content.BookQuizSessionState
import org.maale.bible.data.content.isCorrect
import org.maale.bible.data.scripture.ScriptureEditionCatalog
import org.maale.bible.data.scripture.ScriptureEditionSelectionStore

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
    var selectedEditionId by rememberSaveable { mutableStateOf(ScriptureEditionSelectionStore.load(context)) }
    val selectedEdition = ScriptureEditionCatalog.find(selectedEditionId) ?: ScriptureEditionCatalog.editions.first()
    val book = remember(bookId) { CanonicalBookCatalog.find(bookId) }
    val content = remember(bookId, context) {
        BookLearningContentRepository.load(context, bookId)
    }
    var selectedTab by rememberSaveable(bookId) { mutableStateOf(BookLearningTab.OVERVIEW) }
    val title = book?.let { stringResource(bookNameResource(it.usfmId)) } ?: bookId
    val testament = if ((book?.order ?: 0) <= 39) stringResource(R.string.explorer_old_testament) else stringResource(R.string.explorer_new_testament)

    Column(
        modifier = modifier.fillMaxSize().testTag("book_learning_screen").verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onBack) { Text("‹  " + stringResource(R.string.learning_back_to_books)) }
            Spacer(Modifier.weight(1f))
            Text(testament, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
        }
        Text(title, style = MaterialTheme.typography.headlineLarge)
        ScriptureEditionSelector(
            selectedEditionId = selectedEditionId,
            onEditionSelected = { editionId ->
                if (ScriptureEditionSelectionStore.save(context, editionId)) selectedEditionId = editionId
            },
            selectorTag = "learning_edition_selector"
        )
        Text(
            "Selected edition: ${selectedEdition.title}. Learning guides and quizzes are shared study materials, not edition-specific scripture text.",
            modifier = Modifier.testTag("learning_edition_context"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            stringResource(R.string.learning_book_metadata, book?.usfmId ?: bookId, book?.order ?: 0),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (content?.localeStatus == "english_fallback_review_required") {
            Text(stringResource(R.string.learning_locale_fallback_notice), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
        }
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
        PrimaryTabRow(selectedTabIndex = selectedTab.ordinal) {
            BookLearningTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    modifier = Modifier.testTag("learning_tab_${tab.name.lowercase()}"),
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

/** Maps canonical USFM book identifiers to localized Android string resources. */
internal fun bookNameResource(usfmId: String): Int = when (usfmId) {
    "GEN" -> R.string.book_name_gen
    "EXO" -> R.string.book_name_exo
    "LEV" -> R.string.book_name_lev
    "NUM" -> R.string.book_name_num
    "DEU" -> R.string.book_name_deu
    "JOS" -> R.string.book_name_jos
    "JDG" -> R.string.book_name_jdg
    "RUT" -> R.string.book_name_rut
    "1SA" -> R.string.book_name_1sa
    "2SA" -> R.string.book_name_2sa
    "1KI" -> R.string.book_name_1ki
    "2KI" -> R.string.book_name_2ki
    "1CH" -> R.string.book_name_1ch
    "2CH" -> R.string.book_name_2ch
    "EZR" -> R.string.book_name_ezr
    "NEH" -> R.string.book_name_neh
    "EST" -> R.string.book_name_est
    "JOB" -> R.string.book_name_job
    "PSA" -> R.string.book_name_psa
    "PRO" -> R.string.book_name_pro
    "ECC" -> R.string.book_name_ecc
    "SNG" -> R.string.book_name_sng
    "ISA" -> R.string.book_name_isa
    "JER" -> R.string.book_name_jer
    "LAM" -> R.string.book_name_lam
    "EZK" -> R.string.book_name_ezk
    "DAN" -> R.string.book_name_dan
    "HOS" -> R.string.book_name_hos
    "JOL" -> R.string.book_name_jol
    "AMO" -> R.string.book_name_amo
    "OBA" -> R.string.book_name_oba
    "JON" -> R.string.book_name_jon
    "MIC" -> R.string.book_name_mic
    "NAM" -> R.string.book_name_nam
    "HAB" -> R.string.book_name_hab
    "ZEP" -> R.string.book_name_zep
    "HAG" -> R.string.book_name_hag
    "ZEC" -> R.string.book_name_zec
    "MAL" -> R.string.book_name_mal
    "MAT" -> R.string.book_name_mat
    "MRK" -> R.string.book_name_mrk
    "LUK" -> R.string.book_name_luk
    "JHN" -> R.string.book_name_jhn
    "ACT" -> R.string.book_name_act
    "ROM" -> R.string.book_name_rom
    "1CO" -> R.string.book_name_1co
    "2CO" -> R.string.book_name_2co
    "GAL" -> R.string.book_name_gal
    "EPH" -> R.string.book_name_eph
    "PHP" -> R.string.book_name_php
    "COL" -> R.string.book_name_col
    "1TH" -> R.string.book_name_1th
    "2TH" -> R.string.book_name_2th
    "1TI" -> R.string.book_name_1ti
    "2TI" -> R.string.book_name_2ti
    "TIT" -> R.string.book_name_tit
    "PHM" -> R.string.book_name_phm
    "HEB" -> R.string.book_name_heb
    "JAS" -> R.string.book_name_jas
    "1PE" -> R.string.book_name_1pe
    "2PE" -> R.string.book_name_2pe
    "1JN" -> R.string.book_name_1jn
    "2JN" -> R.string.book_name_2jn
    "3JN" -> R.string.book_name_3jn
    "JUD" -> R.string.book_name_jud
    "REV" -> R.string.book_name_rev
    else -> R.string.learning_unknown_book
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
        if (intro.dateAndComposition.isNotBlank()) LearningSectionCard(stringResource(R.string.learning_date_composition_title), intro.dateAndComposition)
        if (intro.genre.isNotBlank()) LearningSectionCard(stringResource(R.string.learning_genre_title), intro.genre)
        if (intro.theologicalContribution.isNotBlank()) LearningSectionCard(stringResource(R.string.learning_theological_contribution_title), intro.theologicalContribution)
        if (intro.canonicalConnections.isNotEmpty()) LearningListCard(stringResource(R.string.learning_canonical_connections_title), intro.canonicalConnections)
        LearningListCard(
            stringResource(R.string.learning_structure_title),
            intro.structure.map { "${it.range}: ${it.heading} — ${it.summary}" }
        )
        LearningListCard(stringResource(R.string.learning_themes_title), intro.themes)
        LearningListCard(stringResource(R.string.learning_key_people_title), intro.keyPeople)
        LearningListCard(stringResource(R.string.learning_related_title), intro.keyPassages)
        LearningListCard(stringResource(R.string.learning_interpretive_care_title), intro.interpretiveCare)
        content.commentaryUnits.forEach { unit ->
            LearningSectionCard(stringResource(R.string.learning_commentary_title), "${unit.title} · ${unit.range}\n${unit.note}")
        }
        Text(
            stringResource(R.string.learning_source_count, content.sourceCount),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
    Button(onClick = { onOpenReader(bookId) }, modifier = Modifier.fillMaxWidth().testTag("learning_open_reader_button")) {
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
        if (content.chapterGuides.isNotEmpty()) {
            Text(stringResource(R.string.learning_chapter_guides_title), style = MaterialTheme.typography.headlineSmall)
            content.chapterGuides.forEach { guide -> ChapterGuideCard(guide) }
        } else {
            LearningSectionCard(stringResource(R.string.learning_chapter_guides_title), stringResource(R.string.learning_chapter_guides_pending))
        }
    }
}

@Composable
private fun ChapterGuideCard(guide: BookChapterGuide) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("learning_chapter_guide_${guide.chapter}"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.learning_chapter_number, guide.chapter), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            Text(guide.title, style = MaterialTheme.typography.titleMedium)
            Text(guide.summary, style = MaterialTheme.typography.bodyMedium)
            if (guide.literaryContext.isNotBlank()) LearningSectionCard(stringResource(R.string.learning_literary_context_title), guide.literaryContext)
            LearningListCard(stringResource(R.string.learning_observation_title), guide.observation)
            LearningListCard(stringResource(R.string.learning_interpretation_title), guide.interpretation)
            LearningListCard(stringResource(R.string.learning_application_title), guide.application)
            if (guide.crossReferences.isNotEmpty()) LearningListCard(stringResource(R.string.learning_cross_references_title), guide.crossReferences)
            if (guide.discussionPrompts.isNotEmpty()) LearningListCard(stringResource(R.string.learning_discussion_prompts_title), guide.discussionPrompts)
            if (guide.review.isNotBlank()) LearningSectionCard(stringResource(R.string.learning_chapter_review_title), guide.review)
            if (guide.references.isNotEmpty()) {
                Text(stringResource(R.string.learning_answer_references, guide.references.joinToString()), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            }
        }
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
        if (study.prayerPrompt.isNotBlank()) LearningSectionCard(stringResource(R.string.learning_prayer_prompt_title), study.prayerPrompt)
    }
}

@Composable
private fun QuizContent(content: BookLearningContent?) {
    val questions = content?.quizQuestions.orEmpty()
    if (content == null || questions.isEmpty()) {
        LearningSectionCard(stringResource(R.string.learning_quiz_title), stringResource(R.string.learning_quiz_pending))
        Text(stringResource(R.string.learning_quiz_unavailable), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
        return
    }

    DraftWarning(content)
    var session by remember(content.bookId, content.locale) { mutableStateOf(BookQuizSessionState()) }
    val questionCount = questions.size
    val complete = session.isComplete(questionCount)
    val currentQuestion = questions.getOrNull(session.currentIndex)
    val score = session.correctCount(questions)

    if (complete) {
        Card(
            modifier = Modifier.fillMaxWidth().testTag("learning_quiz_results"),
            shape = MaterialTheme.shapes.large
        ) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.learning_quiz_complete), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(R.string.learning_quiz_final_score, score, questionCount), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.learning_quiz_progress, questionCount, questionCount), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = { session = session.restart() }, modifier = Modifier.fillMaxWidth().testTag("learning_quiz_retry")) {
                    Text(stringResource(R.string.learning_retry_quiz))
                }
            }
        }
        return
    }

    val question = currentQuestion ?: return
    val selectedAnswer = session.selectedAnswer(question.id)
    val submitted = session.isSubmitted(question.id)
    Text(stringResource(R.string.learning_quiz_progress, session.currentIndex + 1, questionCount), style = MaterialTheme.typography.titleSmall)
    Text(stringResource(R.string.learning_quiz_score, session.answeredCount(), score), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    QuizQuestionCard(
        question = question,
        selectedAnswer = selectedAnswer,
        submitted = submitted,
        showExplanationFallback = question.explanationLocale != content.locale,
        onSelect = { value -> session = session.select(question.id, value) },
        onSubmit = { session = session.submit(question.id) }
    )
    if (submitted) {
        Button(
            onClick = { session = session.advance(question.id, questionCount) },
            modifier = Modifier.fillMaxWidth().testTag("learning_quiz_next")
        ) {
            Text(stringResource(if (session.currentIndex == questionCount - 1) R.string.learning_see_results else R.string.learning_next_question))
        }
    }
}

@Composable
internal fun QuizQuestionCard(
    question: BookQuizQuestion,
    selectedAnswer: String?,
    submitted: Boolean,
    showExplanationFallback: Boolean,
    onSelect: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val correct = question.isCorrect(selectedAnswer)
    Card(
        modifier = Modifier.fillMaxWidth().testTag("learning_quiz_question_${question.id}"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(question.id, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                Text(question.difficulty, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            }
            Text(question.prompt, style = MaterialTheme.typography.titleMedium)
            if (question.type == "short_answer") {
                OutlinedTextField(
                    value = selectedAnswer.orEmpty(),
                    onValueChange = onSelect,
                    enabled = !submitted,
                    modifier = Modifier.fillMaxWidth().testTag("learning_quiz_short_answer"),
                    label = { Text(stringResource(R.string.learning_quiz_short_answer_hint)) },
                    singleLine = true
                )
            } else if (question.type == "true_false") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = selectedAnswer == "true", onClick = { onSelect("true") }, enabled = !submitted, modifier = Modifier.testTag("learning_quiz_option_true"), label = { Text(stringResource(R.string.learning_true)) })
                    FilterChip(selected = selectedAnswer == "false", onClick = { onSelect("false") }, enabled = !submitted, modifier = Modifier.testTag("learning_quiz_option_false"), label = { Text(stringResource(R.string.learning_false)) })
                }
            } else if (question.type == "multiple_select") {
                val selectedIds = selectedAnswer.orEmpty().split(",").filter { it.isNotBlank() }.toSet()
                Text(stringResource(R.string.learning_quiz_select_all_hint), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                question.options.forEachIndexed { index, option ->
                    val optionId = ('A'.code + index).toChar().toString()
                    Row(
                        Modifier.fillMaxWidth().clickable(enabled = !submitted) {
                            val next = if (optionId in selectedIds) selectedIds - optionId else selectedIds + optionId
                            onSelect(next.sorted().joinToString(","))
                        }.padding(vertical = 2.dp).testTag("learning_quiz_option_$optionId"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = optionId in selectedIds,
                            onCheckedChange = null,
                            enabled = !submitted
                        )
                        Text(option, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                question.options.forEachIndexed { index, option ->
                    val optionId = ('A'.code + index).toChar().toString()
                    Row(
                        Modifier.fillMaxWidth().clickable(enabled = !submitted) { onSelect(optionId) }.padding(vertical = 2.dp).testTag("learning_quiz_option_$optionId"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedAnswer == optionId, onClick = { onSelect(optionId) }, enabled = !submitted)
                        Text(option, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Button(onClick = onSubmit, enabled = selectedAnswer != null && !submitted, modifier = Modifier.fillMaxWidth().testTag("learning_quiz_submit")) {
                Text(stringResource(R.string.learning_check_answer))
            }
            if (submitted) {
                Text(
                    stringResource(if (correct) R.string.learning_answer_correct else R.string.learning_answer_incorrect),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (correct) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                if (showExplanationFallback) {
                    Text(stringResource(R.string.learning_explanation_fallback), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                }
                Text(question.explanation, style = MaterialTheme.typography.bodyMedium)
                if (question.references.isNotEmpty()) {
                    Text(stringResource(R.string.learning_answer_references, question.references.joinToString()), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
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
