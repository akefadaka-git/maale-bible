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

internal fun expectedScripturePackageFileName(record: ScriptureEditionSourceRecord): String? =
    record.packageFileName ?: record.takeIf { ScriptureEditionSourceRegistry.canImport(it.editionId) }
        ?.let { "${it.editionId.lowercase().replace(Regex("[^a-z0-9-]"), "-")}.sqlite" }

/**
 * Read-only, edition-neutral SQLite reader. Every package carries the same normalized schema
 * (package_metadata, books, chapters, headings, verses); all edition-specific provenance and
 * rights expectations are resolved from ScriptureEditionSourceRegistry, never hard-coded to
 * the Maale edition. Package ingestion remains a separate trusted pipeline.
 */
object ScripturePackageRepository {
    private const val EXPECTED_SCHEMA_VERSION = 1
    private const val EXPECTED_APPLICATION_ID = 1296189762

    fun loadChapter(
        context: Context,
        bookId: String,
        chapterNumber: Int,
        editionId: String = ScriptureEditionCatalog.PRIMARY_MAALE_ID
    ): ScriptureChapterResult {
        if (!bookId.matches(Regex("[A-Z0-9]{3}")) || chapterNumber < 1) {
            return ScriptureChapterResult.InvalidPackage
        }
        val edition = ScriptureEditionCatalog.find(editionId)
            ?: return ScriptureChapterResult.InvalidPackage
        val source = ScriptureEditionSourceRegistry.find(editionId)
            ?: return ScriptureChapterResult.InvalidPackage
        val packageName = expectedScripturePackageFileName(source)
            ?: return ScriptureChapterResult.PackageNotInstalled
        val packageFile = File(File(context.filesDir, "scripture"), packageName)
        if (!packageFile.isFile || packageFile.length() <= 0L) {
            return ScriptureChapterResult.PackageNotInstalled
        }

        val allowDebugPreview = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        return try {
            SQLiteDatabase.openDatabase(packageFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY).use { db ->
                val metadata = mutableMapOf<String, String>()
                db.rawQuery("SELECT key, value FROM package_metadata", null).use { cursor ->
                    while (cursor.moveToNext()) metadata[cursor.getString(0)] = cursor.getString(1)
                }

                val expectedFormat = if (editionId == ScriptureEditionCatalog.PRIMARY_MAALE_ID) {
                    "MAALE-SCRIPTURE-SQLITE-1"
                } else {
                    "SCRIPTURE-SQLITE-1"
                }
                if (metadata["package_format"] != expectedFormat ||
                    metadata["schema_version"] != EXPECTED_SCHEMA_VERSION.toString() ||
                    metadata["edition_id"] != edition.id ||
                    metadata["language"] != edition.languageTag ||
                    metadata["source_archive_sha256"] != source.sourceArchiveSha256 ||
                    metadata["license"] != source.license ||
                    metadata["attribution"] != source.attribution ||
                    metadata["content_is_immutable"] != "true" ||
                    metadata["signer_fingerprint"] != source.signerFingerprint
                ) return@use ScriptureChapterResult.InvalidPackage

                val userVersion = db.rawQuery("PRAGMA user_version", null).use {
                    if (it.moveToFirst()) it.getInt(0) else -1
                }
                val applicationId = db.rawQuery("PRAGMA application_id", null).use {
                    if (it.moveToFirst()) it.getInt(0) else -1
                }
                if (userVersion != EXPECTED_SCHEMA_VERSION || applicationId != EXPECTED_APPLICATION_ID) {
                    return@use ScriptureChapterResult.InvalidPackage
                }

                val releaseEligible = metadata["release_eligible"] == "true" &&
                    source.releaseEligible && ScriptureEditionSourceRegistry.canImport(editionId)
                if (!canDisplayScripturePackage(releaseEligible, allowDebugPreview)) {
                    return@use ScriptureChapterResult.PreviewOnly
                }

                val bookExists = db.rawQuery(
                    "SELECT 1 FROM books WHERE edition_id=? AND book_id=?",
                    arrayOf(editionId, bookId)
                ).use { it.moveToFirst() }
                if (!bookExists) return@use ScriptureChapterResult.ChapterUnavailable

                val chapterExists = db.rawQuery(
                    "SELECT 1 FROM chapters WHERE edition_id=? AND book_id=? AND chapter_number=?",
                    arrayOf(editionId, bookId, chapterNumber.toString())
                ).use { it.moveToFirst() }
                if (!chapterExists) return@use ScriptureChapterResult.ChapterUnavailable

                val headings = mutableListOf<ScriptureHeading>()
                db.rawQuery(
                    "SELECT marker, text FROM headings WHERE edition_id=? AND book_id=? AND chapter_number=? ORDER BY ordinal",
                    arrayOf(editionId, bookId, chapterNumber.toString())
                ).use { cursor ->
                    while (cursor.moveToNext()) {
                        headings += ScriptureHeading(cursor.getString(0), cursor.getString(1))
                    }
                }

                val verses = mutableListOf<ScriptureVerse>()
                db.rawQuery(
                    "SELECT verse_label, text FROM verses WHERE edition_id=? AND book_id=? AND chapter_number=? ORDER BY ordinal",
                    arrayOf(editionId, bookId, chapterNumber.toString())
                ).use { cursor ->
                    while (cursor.moveToNext()) {
                        verses += ScriptureVerse(cursor.getString(0), cursor.getString(1))
                    }
                }
                if (verses.isEmpty()) ScriptureChapterResult.InvalidPackage
                else ScriptureChapterResult.Available(
                    ScriptureChapter(editionId, bookId, chapterNumber, headings, verses)
                )
            }
        } catch (_: Exception) {
            ScriptureChapterResult.InvalidPackage
        }
    }
}
