package org.maale.bible.domain.repository
import org.maale.bible.domain.model.*
interface StudyRepository {
 fun saveReadingPosition(position: ReadingPosition)
 fun getReadingPosition(): ReadingPosition?
 fun addBookmark(bookmark: Bookmark)
 fun removeBookmark(id: String)
 fun getBookmarks(bookId: String? = null): List<Bookmark>
 fun saveAnnotation(annotation: StudyAnnotation)
 fun deleteAnnotation(id: String)
 fun getAnnotations(bookId: String? = null): List<StudyAnnotation>
 fun savePlan(plan: ReadingPlan)
 fun getPlans(): List<ReadingPlan>
 fun exportBackup(): String
 fun restoreBackup(json: String)
}