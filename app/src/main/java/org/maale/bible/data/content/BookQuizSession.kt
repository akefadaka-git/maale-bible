package org.maale.bible.data.content

/**
 * Immutable state for a one-question-at-a-time quiz attempt.
 *
 * Answers are locked after submission. Advancing is allowed only after the
 * current question has been submitted; restart clears the entire attempt.
 */
internal data class BookQuizSessionState(
    val currentIndex: Int = 0,
    val selectedAnswers: Map<String, String> = emptyMap(),
    val submittedAnswers: Set<String> = emptySet()
) {
    fun select(questionId: String, answer: String): BookQuizSessionState {
        if (questionId in submittedAnswers || answer.isBlank()) return this
        return copy(selectedAnswers = selectedAnswers + (questionId to answer))
    }

    fun toggleOption(questionId: String, optionId: String): BookQuizSessionState {
        if (questionId in submittedAnswers || optionId.isBlank()) return this
        val selected = selectedAnswers[questionId].orEmpty().split(",").filter { it.isNotBlank() }.toMutableSet()
        if (!selected.add(optionId)) selected.remove(optionId)
        val next = selected.sorted().joinToString(",")
        return copy(selectedAnswers = if (next.isBlank()) selectedAnswers - questionId else selectedAnswers + (questionId to next))
    }

    fun submit(questionId: String): BookQuizSessionState {
        if (questionId in submittedAnswers || selectedAnswers[questionId].isNullOrBlank()) return this
        return copy(submittedAnswers = submittedAnswers + questionId)
    }

    fun advance(currentQuestionId: String, questionCount: Int): BookQuizSessionState {
        if (currentQuestionId !in submittedAnswers || currentIndex >= questionCount) return this
        return copy(currentIndex = (currentIndex + 1).coerceAtMost(questionCount))
    }

    fun restart(): BookQuizSessionState = BookQuizSessionState()

    fun selectedAnswer(questionId: String): String? = selectedAnswers[questionId]

    fun isSubmitted(questionId: String): Boolean = questionId in submittedAnswers

    fun answeredCount(): Int = submittedAnswers.size

    fun correctCount(questions: List<BookQuizQuestion>): Int = questions.count { question ->
        question.id in submittedAnswers && question.isCorrect(selectedAnswers[question.id])
    }

    fun isComplete(questionCount: Int): Boolean = questionCount > 0 && currentIndex >= questionCount
}

private fun normalizeQuizAnswer(value: String): String =
    value.lowercase().trim().replace(Regex("[^\\p{L}\\p{N}]+"), " ").trim()

internal fun BookQuizQuestion.isCorrect(answer: String?): Boolean {
    if (answer == null) return false
    return when (type) {
        "true_false" -> correctAnswer?.toString() == answer
        "multiple_choice" -> correctOptionId == answer
        "multiple_select" -> answer.split(",").filter { it.isNotBlank() }.toSet() == correctOptionIds.toSet()
        "short_answer" -> acceptedAnswers.any { normalizeQuizAnswer(it) == normalizeQuizAnswer(answer) }
        else -> false
    }
}
