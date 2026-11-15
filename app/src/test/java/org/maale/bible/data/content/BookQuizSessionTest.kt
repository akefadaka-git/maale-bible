package org.maale.bible.data.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class BookQuizSessionTest {
    private fun question(id: String, answer: String) = BookQuizQuestion(
        id = id,
        type = if (answer == "true" || answer == "false") "true_false" else "multiple_choice",
        difficulty = "easy",
        prompt = "Question $id",
        options = emptyList(),
        correctOptionId = answer.takeUnless { it == "true" || it == "false" },
        correctAnswer = answer.toBooleanStrictOrNull(),
        explanation = "Explanation",
        explanationLocale = "en",
        references = listOf("GEN 1:1")
    )

    private fun multiQuestion() = BookQuizQuestion(
        id = "multi", type = "multiple_select", difficulty = "medium", prompt = "Select all",
        options = listOf("A", "B", "C", "D"), correctOptionId = null, correctOptionIds = listOf("A", "C"), correctAnswer = null,
        explanation = "Explanation", explanationLocale = "en", references = listOf("GEN 9:8-17")
    )

    private fun shortQuestion() = BookQuizQuestion(
        id = "short", type = "short_answer", difficulty = "medium", prompt = "Name the son",
        options = emptyList(), correctOptionId = null, acceptedAnswers = listOf("Ephraim"), correctAnswer = null,
        explanation = "Explanation", explanationLocale = "en", references = listOf("GEN 41:50-52")
    )

    @Test fun selectionCanBeChangedUntilSubmission() {
        val selected = BookQuizSessionState().select("q1", "A").select("q1", "B")
        assertEquals("B", selected.selectedAnswer("q1"))
        val submitted = selected.submit("q1")
        assertEquals("B", submitted.selectedAnswer("q1"))
        assertSame(submitted, submitted.select("q1", "A"))
    }

    @Test fun submissionRequiresAnAnswerAndLocksIt() {
        val empty = BookQuizSessionState()
        assertSame(empty, empty.submit("q1"))
        val submitted = empty.select("q1", "A").submit("q1")
        assertTrue(submitted.isSubmitted("q1"))
        assertSame(submitted, submitted.submit("q1"))
        assertSame(submitted, submitted.select("q1", "B"))
    }

    @Test fun cannotAdvanceUntilCurrentAnswerIsSubmitted() {
        val untouched = BookQuizSessionState().select("q1", "A")
        assertEquals(0, untouched.advance("q1", 2).currentIndex)
        val submitted = untouched.submit("q1")
        assertEquals(1, submitted.advance("q1", 2).currentIndex)
    }

    @Test fun finalAdvanceMovesToResultsState() {
        val state = BookQuizSessionState().select("q1", "A").submit("q1").advance("q1", 1)
        assertTrue(state.isComplete(1))
        assertEquals(1, state.currentIndex)
        assertEquals(state, state.advance("q1", 1))
    }

    @Test fun scoreCountsOnlySubmittedCorrectAnswers() {
        val questions = listOf(question("q1", "A"), question("q2", "false"), question("q3", "C"))
        val state = BookQuizSessionState()
            .select("q1", "A").submit("q1")
            .select("q2", "true").submit("q2")
            .select("q3", "C")
        assertEquals(2, state.answeredCount())
        assertEquals(1, state.correctCount(questions))
    }

    @Test fun multiSelectTogglesAndStoresStableSortedOptionIds() {
        val selected = BookQuizSessionState().toggleOption("multi", "C").toggleOption("multi", "A")
        assertEquals("A,C", selected.selectedAnswer("multi"))
        assertTrue(selected.submit("multi").isSubmitted("multi"))
        assertTrue(multiQuestion().isCorrect(selected.selectedAnswer("multi")))
        assertFalse(multiQuestion().isCorrect("A"))
        val toggledOff = selected.toggleOption("multi", "C")
        assertEquals("A", toggledOff.selectedAnswer("multi"))
    }

    @Test fun shortAnswerIsTrimmedAndCaseInsensitive() {
        assertTrue(shortQuestion().isCorrect("  ephraim  "))
        assertFalse(shortQuestion().isCorrect("Manasseh"))
        val state = BookQuizSessionState().select("short", "Ephraim").submit("short")
        assertTrue(state.isSubmitted("short"))
        assertEquals(1, state.correctCount(listOf(shortQuestion())))
    }

    @Test fun retryClearsAnswersScoreAndProgress() {
        val state = BookQuizSessionState().select("q1", "A").submit("q1").advance("q1", 2).select("q2", "B")
        val retry = state.restart()
        assertEquals(0, retry.currentIndex)
        assertTrue(retry.selectedAnswers.isEmpty())
        assertTrue(retry.submittedAnswers.isEmpty())
        assertEquals(0, retry.answeredCount())
        assertFalse(retry.isComplete(2))
    }
}
