package org.maale.bible

import android.database.sqlite.SQLiteDatabase as PlainSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.maale.bible.data.repository.StudyDatabase
import org.maale.bible.domain.model.*
import java.io.File

@RunWith(AndroidJUnit4::class)
class MigrationContentEquivalenceInstrumentedTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun createLegacyDatabase(file: File) {
        file.parentFile?.mkdirs()
        val db = PlainSQLiteDatabase.openOrCreateDatabase(file, null)
        try {
            db.execSQL("CREATE TABLE reading_position (singleton INTEGER PRIMARY KEY CHECK(singleton=1), book_id TEXT NOT NULL, chapter INTEGER NOT NULL, verse INTEGER NOT NULL, updated_at INTEGER NOT NULL)")
            db.execSQL("CREATE TABLE bookmarks (id TEXT PRIMARY KEY, book_id TEXT NOT NULL, chapter INTEGER NOT NULL, verse INTEGER NOT NULL, created_at INTEGER NOT NULL, label TEXT NOT NULL DEFAULT '')")
            db.execSQL("CREATE TABLE annotations (id TEXT PRIMARY KEY, book_id TEXT NOT NULL, chapter INTEGER NOT NULL, verse INTEGER NOT NULL, kind TEXT NOT NULL, body TEXT NOT NULL, color TEXT, created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL)")
            db.execSQL("CREATE TABLE reading_plans (id TEXT PRIMARY KEY, title TEXT NOT NULL, start_day INTEGER NOT NULL, duration_days INTEGER NOT NULL, completed_days TEXT NOT NULL, updated_at INTEGER NOT NULL)")
            db.execSQL("INSERT INTO reading_position VALUES (1,'GEN',12,34,1700000000123)")
            db.execSQL("INSERT INTO bookmarks VALUES ('b-1','GEN',12,34,1700000000456,'label with punctuation: — and UTF-8 ጌኤዦ')")
            db.execSQL("INSERT INTO annotations VALUES ('a-1','EXO',3,9,'NOTE','private note — preserve exactly',NULL,1700000000000,1700000000999)")
            db.execSQL("INSERT INTO annotations VALUES ('a-2','PSA',1,2,'HIGHLIGHT','', 'amber',1700000001000,1700000001000)")
            db.execSQL("INSERT INTO reading_plans VALUES ('p-1','Seven days',20000,7,'1,3,7',1700000002222)")
            db.version = StudyDatabase.DB_VERSION
        } finally { db.close() }
    }

    @Test fun migrationPreservesEveryPersistedFieldAcrossAllStudyTables() {
        context.deleteDatabase(StudyDatabase.DB_NAME)
        val dbFile = context.getDatabasePath(StudyDatabase.DB_NAME)
        createLegacyDatabase(dbFile)
        val migrated = StudyDatabase(context)
        try {
            assertEquals(ReadingPosition("GEN", 12, 34, 1700000000123), migrated.getReadingPosition())
            assertEquals(Bookmark("b-1", "GEN", 12, 34, 1700000000456, "label with punctuation: — and UTF-8 ጌኤዦ"), migrated.getBookmarks("GEN").single())
            assertEquals(StudyAnnotation("a-1", "EXO", 3, 9, AnnotationKind.NOTE, "private note — preserve exactly", null, 1700000000000, 1700000000999), migrated.getAnnotations("EXO").single())
            assertEquals(StudyAnnotation("a-2", "PSA", 1, 2, AnnotationKind.HIGHLIGHT, "", "amber", 1700000001000, 1700000001000), migrated.getAnnotations("PSA").single())
            assertEquals(ReadingPlan("p-1", "Seven days", 20000, 7, setOf(1, 3, 7), 1700000002222), migrated.getPlans().single())
        } finally { migrated.close() }
        context.deleteDatabase(StudyDatabase.DB_NAME)
    }

    @Test fun interruptedCutoverRestoresRollbackSourceBeforeRetryingMigration() {
        context.deleteDatabase(StudyDatabase.DB_NAME)
        val live = context.getDatabasePath(StudyDatabase.DB_NAME)
        createLegacyDatabase(live)
        val rollback = File(live.parentFile, "maale_bible_study.plaintext.rollback")
        val temp = File(live.parentFile, "maale_bible_study.enc.tmp")
        assertTrue(live.renameTo(rollback))
        temp.writeBytes("incomplete encrypted temporary file".toByteArray())
        val migrated = StudyDatabase(context)
        try {
            assertEquals("private note — preserve exactly", migrated.getAnnotations("EXO").single().body)
            assertEquals(setOf(1, 3, 7), migrated.getPlans().single().completedDays)
            assertFalse("Rollback copy should be retired only after verified cutover", rollback.exists())
            assertFalse("Invalid temporary file should be removed", temp.exists())
        } finally { migrated.close() }
        context.deleteDatabase(StudyDatabase.DB_NAME)
    }
}
