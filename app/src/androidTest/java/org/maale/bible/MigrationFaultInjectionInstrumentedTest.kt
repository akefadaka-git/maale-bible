package org.maale.bible

import android.database.sqlite.SQLiteDatabase as PlainSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.maale.bible.data.repository.StudyDatabase
import org.maale.bible.data.repository.StudyMigrationFaultInjection
import java.io.File

@RunWith(AndroidJUnit4::class)
class MigrationFaultInjectionInstrumentedTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun createLegacy(file: File, journalMode: String = "DELETE") {
        context.deleteDatabase(StudyDatabase.DB_NAME)
        file.parentFile?.mkdirs()
        val db = PlainSQLiteDatabase.openOrCreateDatabase(file, null)
        try {
            db.execSQL("PRAGMA journal_mode=$journalMode")
            db.execSQL("CREATE TABLE reading_position (singleton INTEGER PRIMARY KEY CHECK(singleton=1), book_id TEXT NOT NULL, chapter INTEGER NOT NULL, verse INTEGER NOT NULL, updated_at INTEGER NOT NULL)")
            db.execSQL("CREATE TABLE bookmarks (id TEXT PRIMARY KEY, book_id TEXT NOT NULL, chapter INTEGER NOT NULL, verse INTEGER NOT NULL, created_at INTEGER NOT NULL, label TEXT NOT NULL DEFAULT '')")
            db.execSQL("CREATE TABLE annotations (id TEXT PRIMARY KEY, book_id TEXT NOT NULL, chapter INTEGER NOT NULL, verse INTEGER NOT NULL, kind TEXT NOT NULL, body TEXT NOT NULL, color TEXT, created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL)")
            db.execSQL("CREATE TABLE reading_plans (id TEXT PRIMARY KEY, title TEXT NOT NULL, start_day INTEGER NOT NULL, duration_days INTEGER NOT NULL, completed_days TEXT NOT NULL, updated_at INTEGER NOT NULL)")
            db.execSQL("INSERT INTO bookmarks VALUES ('fault-1','GEN',2,3,123,'preserve me')")
            db.version = StudyDatabase.DB_VERSION
        } finally { db.close() }
    }

    @Test fun walCheckpointFailureIsFailClosedAndLeavesLegacyRowsReadable() {
        val file = context.getDatabasePath(StudyDatabase.DB_NAME)
        createLegacy(file, "WAL")
        StudyMigrationFaultInjection.failAt = "BEFORE_WAL_CHECKPOINT"
        try {
            assertThrows(Exception::class.java) { StudyDatabase(context) }
            assertTrue(file.exists())
            PlainSQLiteDatabase.openDatabase(file.absolutePath, null, PlainSQLiteDatabase.OPEN_READONLY).use { db ->
                db.rawQuery("SELECT label FROM bookmarks WHERE id='fault-1'", null).use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals("preserve me", cursor.getString(0))
                }
            }
        } finally {
            StudyMigrationFaultInjection.failAt = null
            context.deleteDatabase(StudyDatabase.DB_NAME)
        }
    }

    @Test fun interruptionAfterLegacyRenameRecoversEncryptedDatabaseOnNextOpen() {
        val file = context.getDatabasePath(StudyDatabase.DB_NAME)
        createLegacy(file)
        StudyMigrationFaultInjection.failAt = "AFTER_LEGACY_RENAME"
        try {
            assertThrows(Exception::class.java) { StudyDatabase(context) }
            val rollback = File(file.parentFile, "maale_bible_study.plaintext.rollback")
            val temp = File(file.parentFile, "maale_bible_study.enc.tmp")
            assertTrue("Legacy rollback must survive interrupted cutover", rollback.exists())
            assertTrue("Verified encrypted temporary database must survive", temp.exists())
            val recovered = StudyDatabase(context)
            try {
                assertEquals("preserve me", recovered.getBookmarks("GEN").single().label)
                assertFalse(rollback.exists())
                assertFalse(temp.exists())
            } finally { recovered.close() }
        } finally {
            StudyMigrationFaultInjection.failAt = null
            context.deleteDatabase(StudyDatabase.DB_NAME)
        }
    }

    @Test fun interruptionAfterEncryptedRenameRollsBackWithoutLosingLegacyData() {
        val file = context.getDatabasePath(StudyDatabase.DB_NAME)
        createLegacy(file)
        StudyMigrationFaultInjection.failAt = "AFTER_ENCRYPTED_RENAME"
        try {
            assertThrows(Exception::class.java) { StudyDatabase(context) }
            assertTrue("Rollback must restore the plaintext source after a failed post-rename check", file.exists())
            val restored = PlainSQLiteDatabase.openDatabase(file.absolutePath, null, PlainSQLiteDatabase.OPEN_READONLY)
            try {
                restored.rawQuery("SELECT label FROM bookmarks WHERE id='fault-1'", null).use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals("preserve me", cursor.getString(0))
                }
            } finally { restored.close() }
            assertFalse(File(file.parentFile, "maale_bible_study.plaintext.rollback").exists())
        } finally {
            StudyMigrationFaultInjection.failAt = null
            context.deleteDatabase(StudyDatabase.DB_NAME)
        }
    }
}
