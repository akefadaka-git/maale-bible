package org.maale.bible

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase as PlainSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import net.zetetic.database.sqlcipher.SQLiteDatabase as CipherSQLiteDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.maale.bible.data.repository.LegacyStudyDatabaseMigration
import org.maale.bible.data.repository.StudyDatabase
import org.maale.bible.data.security.StudyDataKeyStore
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@RunWith(AndroidJUnit4::class)
class StudyMigrationCutoverRecoveryTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val live get() = context.getDatabasePath(StudyDatabase.DB_NAME)
    private val temp get() = File(live.parentFile, "maale_bible_study.enc.tmp")
    private val rollback get() = File(live.parentFile, "maale_bible_study.plaintext.rollback")

    @Test fun recoversAfterPlaintextRenameAndRemovesStaleSidecars() {
        cleanFiles()
        val dek = StudyDataKeyStore.getOrCreate(context)
        try {
            createPlainLegacy(live)
            Files.move(live.toPath(), rollback.toPath(), StandardCopyOption.ATOMIC_MOVE)
            File(live.path + "-wal").writeText("legacy plaintext WAL marker")
            File(live.path + "-shm").writeText("legacy plaintext SHM marker")
            createEncryptedFixture(temp, dek)

            LegacyStudyDatabaseMigration.migrate(context, dek)

            assertFalse("Plaintext rollback must be removed after verified promotion", rollback.exists())
            assertFalse("Stale plaintext WAL must not remain beside encrypted database", File(live.path + "-wal").exists())
            assertFalse("Stale plaintext SHM must not remain beside encrypted database", File(live.path + "-shm").exists())
            assertStoredBookmark()
        } finally {
            dek.fill(0)
            cleanFiles()
        }
    }

    @Test fun completesCleanupWhenEncryptedPromotionPrecededCrash() {
        cleanFiles()
        val dek = StudyDataKeyStore.getOrCreate(context)
        try {
            createPlainLegacy(live)
            Files.move(live.toPath(), rollback.toPath(), StandardCopyOption.ATOMIC_MOVE)
            createEncryptedFixture(live, dek)

            LegacyStudyDatabaseMigration.migrate(context, dek)

            assertFalse("Verified encrypted live database must not retain plaintext rollback", rollback.exists())
            assertStoredBookmark()
        } finally {
            dek.fill(0)
            cleanFiles()
        }
    }

    @Test fun restoresPlaintextRollbackAndRetriesWhenTemporaryCiphertextIsInvalid() {
        cleanFiles()
        val dek = StudyDataKeyStore.getOrCreate(context)
        try {
            createPlainLegacy(live)
            Files.move(live.toPath(), rollback.toPath(), StandardCopyOption.ATOMIC_MOVE)
            temp.parentFile?.mkdirs()
            temp.writeText("incomplete encrypted temporary file")

            LegacyStudyDatabaseMigration.migrate(context, dek)

            assertFalse("Recovered migration must remove the temporary file", temp.exists())
            assertFalse("Recovered migration must remove the plaintext rollback", rollback.exists())
            assertStoredBookmark()
        } finally {
            dek.fill(0)
            cleanFiles()
        }
    }

    private fun createPlainLegacy(file: File) {
        file.parentFile?.mkdirs()
        val db = PlainSQLiteDatabase.openOrCreateDatabase(file, null)
        try {
            db.execSQL("CREATE TABLE bookmarks (id TEXT PRIMARY KEY, book_id TEXT NOT NULL, chapter INTEGER NOT NULL, verse INTEGER NOT NULL, created_at INTEGER NOT NULL, label TEXT NOT NULL DEFAULT '')")
            db.execSQL("INSERT INTO bookmarks VALUES ('legacy-cutover', 'GEN', 1, 2, 123, 'preserve-cutover-row')")
            db.version = StudyDatabase.DB_VERSION
        } finally {
            db.close()
        }
    }

    private fun createEncryptedFixture(file: File, dek: ByteArray) {
        System.loadLibrary("sqlcipher")
        file.parentFile?.mkdirs()
        val db = CipherSQLiteDatabase.openOrCreateDatabase(file, dek, null, null)
        try {
            db.execSQL("CREATE TABLE reading_position (singleton INTEGER PRIMARY KEY CHECK(singleton=1), book_id TEXT NOT NULL, chapter INTEGER NOT NULL CHECK(chapter>0), verse INTEGER NOT NULL CHECK(verse>0), updated_at INTEGER NOT NULL)")
            db.execSQL("CREATE TABLE bookmarks (id TEXT PRIMARY KEY, book_id TEXT NOT NULL, chapter INTEGER NOT NULL CHECK(chapter>0), verse INTEGER NOT NULL CHECK(verse>0), created_at INTEGER NOT NULL, label TEXT NOT NULL DEFAULT '')")
            db.execSQL("CREATE INDEX idx_bookmarks_ref ON bookmarks(book_id,chapter,verse)")
            db.execSQL("CREATE TABLE annotations (id TEXT PRIMARY KEY, book_id TEXT NOT NULL, chapter INTEGER NOT NULL CHECK(chapter>0), verse INTEGER NOT NULL CHECK(verse>0), kind TEXT NOT NULL CHECK(kind IN ('HIGHLIGHT','NOTE')), body TEXT NOT NULL, color TEXT, created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL)")
            db.execSQL("CREATE INDEX idx_annotations_ref ON annotations(book_id,chapter,verse)")
            db.execSQL("CREATE TABLE reading_plans (id TEXT PRIMARY KEY, title TEXT NOT NULL, start_day INTEGER NOT NULL, duration_days INTEGER NOT NULL, completed_days TEXT NOT NULL, updated_at INTEGER NOT NULL)")
            db.execSQL("CREATE INDEX idx_plans_updated ON reading_plans(updated_at)")
            db.insertOrThrow("bookmarks", null, ContentValues().apply {
                put("id", "legacy-cutover")
                put("book_id", "GEN")
                put("chapter", 1)
                put("verse", 2)
                put("created_at", 123L)
                put("label", "preserve-cutover-row")
            })
            db.version = StudyDatabase.DB_VERSION
        } finally {
            db.close()
        }
    }

    private fun assertStoredBookmark() {
        val db = StudyDatabase(context)
        try {
            assertEquals("preserve-cutover-row", db.getBookmarks("GEN").single().label)
        } finally {
            db.close()
        }
    }

    private fun cleanFiles() {
        context.deleteDatabase(StudyDatabase.DB_NAME)
        listOf(
            temp, rollback, File(live.path + "-wal"),
            File(live.path + "-shm"), File(live.path + "-journal")
        ).forEach { it.delete() }
    }
}
