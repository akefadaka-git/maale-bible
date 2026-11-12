package org.maale.bible

import android.database.sqlite.SQLiteDatabase as PlainSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.json.JSONObject
import org.maale.bible.data.repository.StudyDatabase
import org.maale.bible.data.security.PortableStudyBackup
import org.maale.bible.domain.model.Bookmark
import java.io.File

@RunWith(AndroidJUnit4::class)
class StudyEncryptionMigrationInstrumentedTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun legacyPlaintextDatabaseMigratesWithoutLosingRows() {
        context.deleteDatabase(StudyDatabase.DB_NAME)
        val file = context.getDatabasePath(StudyDatabase.DB_NAME)
        file.parentFile?.mkdirs()
        val legacy = PlainSQLiteDatabase.openOrCreateDatabase(file, null)
        legacy.execSQL("CREATE TABLE reading_position (singleton INTEGER PRIMARY KEY CHECK(singleton=1), book_id TEXT NOT NULL, chapter INTEGER NOT NULL, verse INTEGER NOT NULL, updated_at INTEGER NOT NULL)")
        legacy.execSQL("CREATE TABLE bookmarks (id TEXT PRIMARY KEY, book_id TEXT NOT NULL, chapter INTEGER NOT NULL, verse INTEGER NOT NULL, created_at INTEGER NOT NULL, label TEXT NOT NULL DEFAULT '')")
        legacy.execSQL("CREATE TABLE annotations (id TEXT PRIMARY KEY, book_id TEXT NOT NULL, chapter INTEGER NOT NULL, verse INTEGER NOT NULL, kind TEXT NOT NULL, body TEXT NOT NULL, color TEXT, created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL)")
        legacy.execSQL("CREATE TABLE reading_plans (id TEXT PRIMARY KEY, title TEXT NOT NULL, start_day INTEGER NOT NULL, duration_days INTEGER NOT NULL, completed_days TEXT NOT NULL, updated_at INTEGER NOT NULL)")
        legacy.execSQL("INSERT INTO bookmarks VALUES ('legacy-1','GEN',1,2,123,'preserved')")
        legacy.version = 2
        legacy.close()

        val migrated = StudyDatabase(context)
        try {
            assertEquals("preserved", migrated.getBookmarks("GEN").single().label)
            assertEquals("legacy-1", migrated.getBookmarks("GEN").single().id)
        } finally { migrated.close() }

        val header = ByteArray(16)
        file.inputStream().use { assertEquals(16, it.read(header)) }
        assertNotEquals("SQLite format 3\u0000", String(header, Charsets.US_ASCII))
        assertFalse(File(context.getDatabasePath("maale_bible_study.plaintext.rollback").path).exists())
        try {
            val plain = PlainSQLiteDatabase.openDatabase(file.absolutePath, null, PlainSQLiteDatabase.OPEN_READONLY)
            plain.close()
            fail("Framework SQLite unexpectedly opened SQLCipher database")
        } catch (_: Exception) { }
        context.deleteDatabase(StudyDatabase.DB_NAME)
    }

    @Test fun portableRecoveryRestoresOnCleanDatabaseWithoutOriginatingKeystore() {
        context.deleteDatabase(StudyDatabase.DB_NAME)
        val source = StudyDatabase(context)
        source.addBookmark(Bookmark("portable-1", "EXO", 3, 4, 500, "portable secret"))
        val recoveryKey = PortableStudyBackup.generateRecoveryKey()
        val envelope = PortableStudyBackup.export(context, source, recoveryKey)
        source.close()
        context.deleteDatabase(StudyDatabase.DB_NAME)

        PortableStudyBackup.restore(context, envelope, recoveryKey)
        val restored = StudyDatabase(context)
        try {
            assertEquals("portable secret", restored.getBookmarks("EXO").single().label)
        } finally { restored.close() }
        context.deleteDatabase(StudyDatabase.DB_NAME)
    }

    @Test fun wrongRecoveryKeyAndTamperedEnvelopeDoNotCreateDestinationDatabase() {
        context.deleteDatabase(StudyDatabase.DB_NAME)
        val source = StudyDatabase(context)
        source.addBookmark(Bookmark("tamper-1", "GEN", 1, 1, 1, "not public"))
        val key = PortableStudyBackup.generateRecoveryKey()
        val envelope = PortableStudyBackup.export(context, source, key)
        source.close()
        context.deleteDatabase(StudyDatabase.DB_NAME)

        assertThrows(Exception::class.java) { PortableStudyBackup.restore(context, envelope, PortableStudyBackup.generateRecoveryKey()) }
        assertFalse(context.getDatabasePath(StudyDatabase.DB_NAME).exists())

        val json = JSONObject(envelope)
        val payload = json.getString("payload")
        json.put("payload", (if (payload.first() == 'A') "B" else "A") + payload.drop(1))
        assertThrows(Exception::class.java) { PortableStudyBackup.restore(context, json.toString(), key) }
        assertFalse(context.getDatabasePath(StudyDatabase.DB_NAME).exists())
    }
}
