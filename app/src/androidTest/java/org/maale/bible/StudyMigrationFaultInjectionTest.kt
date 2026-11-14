package org.maale.bible

import android.database.sqlite.SQLiteDatabase as PlainSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.maale.bible.data.repository.StudyDatabase

@RunWith(AndroidJUnit4::class)
class StudyMigrationFaultInjectionTest {
    @Test fun invalidLegacyRowAbortsMigrationAndRetainsOriginalPlaintextSource() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(StudyDatabase.DB_NAME)
        val file = context.getDatabasePath(StudyDatabase.DB_NAME)
        file.parentFile?.mkdirs()
        val legacy = PlainSQLiteDatabase.openOrCreateDatabase(file, null)
        legacy.execSQL("CREATE TABLE reading_position (singleton INTEGER PRIMARY KEY CHECK(singleton=1), book_id TEXT NOT NULL, chapter INTEGER NOT NULL, verse INTEGER NOT NULL, updated_at INTEGER NOT NULL)")
        legacy.execSQL("CREATE TABLE bookmarks (id TEXT PRIMARY KEY, book_id TEXT NOT NULL, chapter INTEGER NOT NULL, verse INTEGER NOT NULL, created_at INTEGER NOT NULL, label TEXT NOT NULL DEFAULT '')")
        legacy.execSQL("CREATE TABLE annotations (id TEXT PRIMARY KEY, book_id TEXT NOT NULL, chapter INTEGER NOT NULL, verse INTEGER NOT NULL, kind TEXT NOT NULL, body TEXT NOT NULL, color TEXT, created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL)")
        legacy.execSQL("CREATE TABLE reading_plans (id TEXT PRIMARY KEY, title TEXT NOT NULL, start_day INTEGER NOT NULL, duration_days INTEGER NOT NULL, completed_days TEXT NOT NULL, updated_at INTEGER NOT NULL)")
        legacy.execSQL("INSERT INTO bookmarks VALUES ('bad-coordinate','GEN',0,1,99,'retain-source')")
        legacy.version = 2
        legacy.close()

        val migrationFailure = runCatching { StudyDatabase(context) }.exceptionOrNull()
        assertNotNull("Invalid legacy coordinates must stop migration", migrationFailure)
        val header = ByteArray(16)
        file.inputStream().use { assertEquals(16, it.read(header)) }
        assertEquals("SQLite format 3\u0000", String(header, Charsets.US_ASCII))
        val source = PlainSQLiteDatabase.openDatabase(file.absolutePath, null, PlainSQLiteDatabase.OPEN_READONLY)
        try {
            source.rawQuery("SELECT label FROM bookmarks WHERE id='bad-coordinate'", null).use {
                assertEquals(true, it.moveToFirst())
                assertEquals("retain-source", it.getString(0))
            }
        } finally { source.close() }
        context.deleteDatabase(StudyDatabase.DB_NAME)
    }
}
