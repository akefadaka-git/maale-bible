package org.maale.bible.domain.model

data class ReadingPosition(val bookId: String, val chapter: Int, val verse: Int, val updatedAtEpochMs: Long) {
    init { require(bookId.matches(Regex("[A-Z0-9]{3}"))); require(chapter > 0); require(verse > 0); require(updatedAtEpochMs >= 0) }
}
data class Bookmark(val id: String, val bookId: String, val chapter: Int, val verse: Int, val createdAtEpochMs: Long, val label: String = "") {
    init { require(id.isNotBlank()); require(bookId.matches(Regex("[A-Z0-9]{3}"))); require(chapter > 0 && verse > 0); require(createdAtEpochMs >= 0) }
}
enum class AnnotationKind { HIGHLIGHT, NOTE }
data class StudyAnnotation(val id: String, val bookId: String, val chapter: Int, val verse: Int, val kind: AnnotationKind, val body: String, val color: String?, val createdAtEpochMs: Long, val updatedAtEpochMs: Long) {
    init { require(id.isNotBlank()); require(bookId.matches(Regex("[A-Z0-9]{3}"))); require(chapter > 0 && verse > 0); require(body.length <= 10_000); require(createdAtEpochMs >= 0 && updatedAtEpochMs >= createdAtEpochMs) }
}
data class ReadingPlan(val id: String, val title: String, val startDateEpochDay: Long, val durationDays: Int, val completedDays: Set<Int>, val updatedAtEpochMs: Long) {
    init { require(id.isNotBlank()); require(title.isNotBlank()); require(durationDays in 1..3660); require(completedDays.all { it in 1..durationDays }); require(updatedAtEpochMs >= 0) }
}