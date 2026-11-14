package org.maale.bible.data.scripture

import android.content.Context
import android.content.pm.ApplicationInfo
import android.database.sqlite.SQLiteDatabase
import java.io.File

data class ScriptureVerse(val label: String, val text: String)
data class ScriptureHeading(val marker: String, val text: String)
data class ScriptureChapter(
    val editionId: String,
    val bookId: String,
    val chapterNumber: Int,
    val headings: List<ScriptureHeading>,
    val verses: List<ScriptureVerse>
)

sealed class ScriptureChapterResult {
    data class Available(val chapter: ScriptureChapter) : ScriptureChapterResult()
    data object PackageNotInstalled : ScriptureChapterResult()
    data object PreviewOnly : ScriptureChapterResult()
    data object ChapterUnavailable : ScriptureChapterResult()
    data object InvalidPackage : ScriptureChapterResult()
}

/** Release builds require an explicitly release-eligible package; debug builds may inspect a candidate. */
internal fun canDisplayScripturePackage(releaseEligible: Boolean, allowDebugPreview: Boolean): Boolean =
    releaseEligible || allowDebugPreview

/**
 * Opens the separately installed scripture database read-only. This database is deliberately
 * separate from the encrypted personal-study database and is never created or modified here.
 */
object ScripturePackageRepository {
    private const val PACKAGE_FILE = "maale-mdytwf-2008.sqlite"
    private const val EXPECTED_FORMAT = "MAALE-SCRIPTURE-SQLITE-1"
    private const val EXPECTED_EDITION = "MDYTWF-2008-MAALE-FULL"

    fun loadChapter(context: Context, bookId: String, chapterNumber: Int): ScriptureChapterResult {
        if (!bookId.matches(Regex("[A-Z0-9]{3}")) || chapterNumber < 1) {
            return ScriptureChapterResult.InvalidPackage
        }
        val packageFile = File(File(context.filesDir, "scripture"), PACKAGE_FILE)
        if (!packageFile.isFile || packageFile.length() <= 0L) return ScriptureChapterResult.PackageNotInstalled

        val allowDebugPreview = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        return try {
            SQLiteDatabase.openDatabase(packageFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY).use { db ->
                val metadata = mutableMapOf<String, String>()
                db.rawQuery("SELECT key, value FROM package_metadata", null).use { cursor ->
                    while (cursor.moveToNext()) metadata[cursor.getString(0)] = cursor.getString(1)
                }
                if (metadata["package_format"] != EXPECTED_FORMAT ||
                    metadata["schema_version"] != "1" ||
                    metadata["edition_id"] != EXPECTED_EDITION ||
                    metadata["language"] != "mdy"
                ) return@use ScriptureChapterResult.InvalidPackage

                val releaseEligible = metadata["release_eligible"] == "true"
                if (!canDisplayScripturePackage(releaseEligible, allowDebugPreview)) {
                    return@use ScriptureChapterResult.PreviewOnly
                }

                val bookExists = db.rawQuery(
                    "SELECT 1 FROM books WHERE edition_id=? AND book_id=?",
                    arrayOf(EXPECTED_EDITION, bookId)
                ).use { it.moveToFirst() }
                if (!bookExists) return@use ScriptureChapterResult.ChapterUnavailable

                val chapterExists = db.rawQuery(
                    "SELECT 1 FROM chapters WHERE edition_id=? AND book_id=? AND chapter_number=?",
                    arrayOf(EXPECTED_EDITION, bookId, chapterNumber.toString())
                ).use { it.moveToFirst() }
                if (!chapterExists) return@use ScriptureChapterResult.ChapterUnavailable

                val headings = mutableListOf<ScriptureHeading>()
                db.rawQuery(
                    "SELECT marker, text FROM headings WHERE edition_id=? AND book_id=? AND chapter_number=? ORDER BY ordinal",
                    arrayOf(EXPECTED_EDITION, bookId, chapterNumber.toString())
                ).use { cursor ->
                    while (cursor.moveToNext()) headings += ScriptureHeading(cursor.getString(0), cursor.getString(1))
                }

                val verses = mutableListOf<ScriptureVerse>()
                db.rawQuery(
                    "SELECT verse_label, text FROM verses WHERE edition_id=? AND book_id=? AND chapter_number=? ORDER BY ordinal",
                    arrayOf(EXPECTED_EDITION, bookId, chapterNumber.toString())
                ).use { cursor ->
                    while (cursor.moveToNext()) verses += ScriptureVerse(cursor.getString(0), cursor.getString(1))
                }
                if (verses.isEmpty()) ScriptureChapterResult.InvalidPackage
                else ScriptureChapterResult.Available(
                    ScriptureChapter(EXPECTED_EDITION, bookId, chapterNumber, headings, verses)
                )
            }
        } catch (_: Exception) {
            ScriptureChapterResult.InvalidPackage
        }
    }
}
