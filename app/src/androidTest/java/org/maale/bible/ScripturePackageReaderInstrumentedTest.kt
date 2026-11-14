package org.maale.bible

import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.maale.bible.data.catalog.CanonicalBookCatalog
import org.maale.bible.data.scripture.ScripturePackageManager
import java.io.File

@RunWith(AndroidJUnit4::class)
class ScripturePackageReaderInstrumentedTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val packageFile = File(File(context.filesDir, "scripture"), "maale-mdytwf-2008.sqlite")

    @After
    fun cleanup() {
        packageFile.setWritable(true, true)
        packageFile.delete()
    }

    @Test
    fun opensImmutableReviewPackageAndReadsChapterText() {
        createSyntheticPackage(releaseEligible = "false")
        packageFile.setReadOnly()

        val reader = ScripturePackageManager(context).openInstalled()
        assertNotNull(reader)
        reader!!.use {
            assertEquals(66, it.metadata.bookCount)
            assertEquals(1_189, it.metadata.chapterCount)
            assertEquals(30_506, it.metadata.verseCount)
            assertEquals(19, it.chapterCount("GEN"))

            val chapter = it.loadChapter("GEN", 1)
            assertEquals("GEN", chapter.bookId)
            assertEquals(1, chapter.chapterNumber)
            assertEquals(26, chapter.verses.size)
            assertEquals("Synthetic Maale verse 1", chapter.verses.first().text)
        }
    }

    @Test
    fun rejectsPackageThatClaimsReleaseEligibilityWithoutTrustApproval() {
        createSyntheticPackage(releaseEligible = "true")
        packageFile.setReadOnly()

        val rejected = runCatching { ScripturePackageManager(context).openInstalled() }.isFailure
        assertTrue("Unsigned importer output must never be accepted as release eligible.", rejected)
    }

    private fun createSyntheticPackage(releaseEligible: String) {
        packageFile.parentFile?.mkdirs()
        packageFile.setWritable(true, true)
        packageFile.delete()

        val database = SQLiteDatabase.openOrCreateDatabase(packageFile, null)
        try {
            database.execSQL("PRAGMA application_id = ${ScripturePackageManager.APPLICATION_ID}")
            database.version = ScripturePackageManager.SCHEMA_VERSION
            database.execSQL("PRAGMA foreign_keys = ON")
            database.execSQL(
                "CREATE TABLE package_metadata(key TEXT PRIMARY KEY, value TEXT NOT NULL)"
            )
            database.execSQL(
                "CREATE TABLE books(edition_id TEXT NOT NULL, book_id TEXT NOT NULL, ordinal INTEGER NOT NULL, source_path TEXT NOT NULL, source_sha256 TEXT NOT NULL, PRIMARY KEY(edition_id, book_id), UNIQUE(edition_id, ordinal))"
            )
            database.execSQL(
                "CREATE TABLE chapters(edition_id TEXT NOT NULL, book_id TEXT NOT NULL, chapter_number INTEGER NOT NULL, PRIMARY KEY(edition_id, book_id, chapter_number), FOREIGN KEY(edition_id, book_id) REFERENCES books(edition_id, book_id))"
            )
            database.execSQL(
                "CREATE TABLE verses(edition_id TEXT NOT NULL, book_id TEXT NOT NULL, chapter_number INTEGER NOT NULL, verse_label TEXT NOT NULL, ordinal INTEGER NOT NULL, text TEXT NOT NULL, source_path TEXT NOT NULL, source_line INTEGER NOT NULL, PRIMARY KEY(edition_id, book_id, chapter_number, verse_label), FOREIGN KEY(edition_id, book_id, chapter_number) REFERENCES chapters(edition_id, book_id, chapter_number))"
            )
            database.execSQL(
                "CREATE TABLE headings(edition_id TEXT NOT NULL, book_id TEXT NOT NULL, chapter_number INTEGER NOT NULL, ordinal INTEGER NOT NULL, marker TEXT NOT NULL, text TEXT NOT NULL, source_path TEXT NOT NULL, source_line INTEGER NOT NULL, PRIMARY KEY(edition_id, book_id, ordinal), FOREIGN KEY(edition_id, book_id, chapter_number) REFERENCES chapters(edition_id, book_id, chapter_number))"
            )
            database.execSQL(
                "CREATE INDEX verses_lookup ON verses(edition_id, book_id, chapter_number, ordinal)"
            )

            val metadata = mapOf(
                "package_format" to ScripturePackageManager.PACKAGE_FORMAT,
                "schema_version" to ScripturePackageManager.SCHEMA_VERSION.toString(),
                "edition_id" to ScripturePackageManager.EDITION_ID,
                "language" to ScripturePackageManager.LANGUAGE,
                "source_archive_sha256" to ScripturePackageManager.SOURCE_ARCHIVE_SHA256,
                "source_archive_bytes" to ScripturePackageManager.SOURCE_ARCHIVE_BYTES.toString(),
                "license" to ScripturePackageManager.LICENSE,
                "attribution" to ScripturePackageManager.ATTRIBUTION,
                "signer_trust" to "UNDEFINED_IN_ISOLATED_KEYRING",
                "release_eligible" to releaseEligible,
                "content_is_immutable" to "true"
            )
            database.beginTransaction()
            try {
                metadata.forEach { (key, value) ->
                    database.execSQL(
                        "INSERT INTO package_metadata(key, value) VALUES (?, ?)",
                        arrayOf(key, value)
                    )
                }

                val bookStatement = database.compileStatement(
                    "INSERT INTO books(edition_id, book_id, ordinal, source_path, source_sha256) VALUES (?, ?, ?, ?, ?)"
                )
                val chapterStatement = database.compileStatement(
                    "INSERT INTO chapters(edition_id, book_id, chapter_number) VALUES (?, ?, ?)"
                )
                val verseStatement = database.compileStatement(
                    "INSERT INTO verses(edition_id, book_id, chapter_number, verse_label, ordinal, text, source_path, source_line) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
                )

                var globalChapterIndex = 0
                var totalVerses = 0
                CanonicalBookCatalog.books.forEachIndexed { bookIndex, book ->
                    val ordinal = bookIndex + 1
                    bookStatement.clearBindings()
                    bookStatement.bindString(1, ScripturePackageManager.EDITION_ID)
                    bookStatement.bindString(2, book.usfmId)
                    bookStatement.bindLong(3, ordinal.toLong())
                    bookStatement.bindString(4, "${book.usfmId}.usfm")
                    bookStatement.bindString(5, "a".repeat(64))
                    bookStatement.executeInsert()

                    val chapterCount = if (bookIndex == 0) 19 else 18
                    for (chapter in 1..chapterCount) {
                        chapterStatement.clearBindings()
                        chapterStatement.bindString(1, ScripturePackageManager.EDITION_ID)
                        chapterStatement.bindString(2, book.usfmId)
                        chapterStatement.bindLong(3, chapter.toLong())
                        chapterStatement.executeInsert()

                        val verseCount = if (globalChapterIndex < 781) 26 else 25
                        for (verse in 1..verseCount) {
                            verseStatement.clearBindings()
                            verseStatement.bindString(1, ScripturePackageManager.EDITION_ID)
                            verseStatement.bindString(2, book.usfmId)
                            verseStatement.bindLong(3, chapter.toLong())
                            verseStatement.bindString(4, verse.toString())
                            verseStatement.bindLong(5, verse.toLong())
                            verseStatement.bindString(6, "Synthetic Maale verse ${verse}")
                            verseStatement.bindString(7, "${book.usfmId}.usfm")
                            verseStatement.bindLong(8, (verse + 1).toLong())
                            verseStatement.executeInsert()
                            totalVerses++
                        }
                        globalChapterIndex++
                    }
                }
                assertEquals(30_506, totalVerses)
                database.setTransactionSuccessful()
            } finally {
                database.endTransaction()
                bookStatement.close()
                chapterStatement.close()
                verseStatement.close()
            }
        } finally {
            database.close()
        }
    }
}
