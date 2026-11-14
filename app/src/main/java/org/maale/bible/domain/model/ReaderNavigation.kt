package org.maale.bible.domain.model

/** Coordinates only: chapter and verse bounds must come from an approved content source. */
data class ReaderNavigation(val bookId: String, val chapter: Int = 1, val verse: Int = 1) {
    init { require(bookId.matches(Regex("[A-Z0-9]{3}"))); require(chapter > 0 && verse > 0) }
    fun previousVerse() = if (verse > 1) copy(verse = verse - 1) else this
    fun nextVerse() = copy(verse = verse + 1)
    fun previousChapter() = if (chapter > 1) copy(chapter = chapter - 1, verse = 1) else this
    fun nextChapter() = copy(chapter = chapter + 1, verse = 1)
}