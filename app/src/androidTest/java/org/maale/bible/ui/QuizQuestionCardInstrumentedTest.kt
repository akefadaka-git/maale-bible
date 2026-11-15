package org.maale.bible.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.maale.bible.data.content.BookQuizQuestion

@RunWith(AndroidJUnit4::class)
class QuizQuestionCardInstrumentedTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun multipleSelectQuestionRendersAndReturnsSelectedOptionIds() {
        var selected = ""
        val question = BookQuizQuestion(
            id = "test-multi",
            type = "multiple_select",
            difficulty = "medium",
            prompt = "Select all valid details",
            options = listOf("First", "Second", "Third", "Fourth"),
            correctOptionId = null,
            correctOptionIds = listOf("A", "C"),
            correctAnswer = null,
            explanation = "The first and third details are supported.",
            explanationLocale = "en",
            references = listOf("GEN 9:8-17")
        )
        compose.setContent {
            MaterialTheme {
                QuizQuestionCard(
                    question = question,
                    selectedAnswer = selected,
                    submitted = false,
                    showExplanationFallback = false,
                    onSelect = { selected = it },
                    onSubmit = {}
                )
            }
        }
        compose.onNodeWithText("Select every correct option").assertExists()
        compose.onNodeWithTag("learning_quiz_option_A").performClick()
        assertEquals("A", selected)
    }

    @Test
    fun shortAnswerQuestionAcceptsTypedText() {
        var typed = ""
        val question = BookQuizQuestion(
            id = "test-short",
            type = "short_answer",
            difficulty = "medium",
            prompt = "Name the second son",
            options = emptyList(),
            correctOptionId = null,
            acceptedAnswers = listOf("Ephraim"),
            correctAnswer = null,
            explanation = "Ephraim is the second son.",
            explanationLocale = "en",
            references = listOf("GEN 41:50-52")
        )
        compose.setContent {
            MaterialTheme {
                QuizQuestionCard(
                    question = question,
                    selectedAnswer = typed,
                    submitted = false,
                    showExplanationFallback = false,
                    onSelect = { typed = it },
                    onSubmit = {}
                )
            }
        }
        compose.onNodeWithTag("learning_quiz_short_answer").performTextInput("Ephraim")
        assertEquals("Ephraim", typed)
    }
}
