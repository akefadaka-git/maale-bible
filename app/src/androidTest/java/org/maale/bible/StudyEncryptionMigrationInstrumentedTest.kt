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
import org.maale.bible.data.security.StudyDataKeyStore
import org.maale.bible.domain.model.Bookmark
import org.maale.bible.domain.model.ReadingPosition
import org.maale.bible.domain.model.StudyAnnotation
import org.maale.bible.domain.model.AnnotationKind
import org.maale.bible.domain.model.ReadingPlan
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
        val protectedFiles = listOf(file, File(file.path + "-wal"), File(file.path + "-shm"), File(file.path + "-journal"))
        protectedFiles.filter { it.exists() }.forEach { protected ->
            val bytes = protected.readBytes()
            assertFalse(String(bytes, Charsets.ISO_8859_1).contains("preserved"))
        }
        try {
            val plain = PlainSQLiteDatabase.openDatabase(file.absolutePath, null, PlainSQLiteDatabase.OPEN_READONLY)
            try {
                plain.rawQuery("SELECT name FROM sqlite_master", null).use { it.moveToFirst() }
                fail("Framework SQLite unexpectedly read SQLCipher database pages")
            } finally { plain.close() }
        } catch (_: android.database.sqlite.SQLiteException) { }
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

    @Test fun failedRecoveryCleanupRemovesOrphanedLocalKeyWrapper() {
        context.deleteDatabase(StudyDatabase.DB_NAME)
        val recovered = ByteArray(32).also { java.security.SecureRandom().nextBytes(it) }
        StudyDataKeyStore.installRecovered(context, recovered)
        assertArrayEquals(recovered, StudyDataKeyStore.load(context))
        StudyDataKeyStore.clearRecoveredAfterFailedRestore(context)
        val replacement = StudyDataKeyStore.getOrCreate(context)
        assertFalse(recovered.contentEquals(replacement))
        recovered.fill(0)
        replacement.fill(0)
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


    @Test fun migrationPreservesEveryStudyRecordField() {
        context.deleteDatabase(StudyDatabase.DB_NAME)
        val file = context.getDatabasePath(StudyDatabase.DB_NAME)
        file.parentFile?.mkdirs()
        val legacy = PlainSQLiteDatabase.openOrCreateDatabase(file, null)
        legacy.execSQL("CREATE TABLE reading_position (singleton INTEGER PRIMARY KEY CHECK(singleton=1), book_id TEXT NOT NULL, chapter INTEGER NOT NULL, verse INTEGER NOT NULL, updated_at INTEGER NOT NULL)")
        legacy.execSQL("CREATE TABLE bookmarks (id TEXT PRIMARY KEY, book_id TEXT NOT NULL, chapter INTEGER NOT NULL, verse INTEGER NOT NULL, created_at INTEGER NOT NULL, label TEXT NOT NULL DEFAULT '')")
        legacy.execSQL("CREATE TABLE annotations (id TEXT PRIMARY KEY, book_id TEXT NOT NULL, chapter INTEGER NOT NULL, verse INTEGER NOT NULL, kind TEXT NOT NULL, body TEXT NOT NULL, color TEXT, created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL)")
        legacy.execSQL("CREATE TABLE reading_plans (id TEXT PRIMARY KEY, title TEXT NOT NULL, start_day INTEGER NOT NULL, duration_days INTEGER NOT NULL, completed_days TEXT NOT NULL, updated_at INTEGER NOT NULL)")
        legacy.execSQL("INSERT INTO reading_position VALUES (1,'GEN',7,19,1700000000123)")
        legacy.execSQL("INSERT INTO bookmarks VALUES ('b-1','GEN',7,19,1700000000456,'bookmark label')")
        legacy.execSQL("INSERT INTO annotations VALUES ('a-1','GEN',7,19,'NOTE','private note',NULL,1700000000500,1700000000600)")
        legacy.execSQL("INSERT INTO reading_plans VALUES ('p-1','Genesis plan',20000,7,'1,3,7',1700000000700)")
        legacy.version = 2
        legacy.close()

        val migrated = StudyDatabase(context)
        try {
            assertEquals(ReadingPosition("GEN",7,19,1700000000123), migrated.getReadingPosition())
            assertEquals(Bookmark("b-1","GEN",7,19,1700000000456,"bookmark label"), migrated.getBookmarks("GEN").single())
            assertEquals(StudyAnnotation("a-1","GEN",7,19,AnnotationKind.NOTE,"private note",null,1700000000500,1700000000600), migrated.getAnnotations("GEN").single())
            assertEquals(ReadingPlan("p-1","Genesis plan",20000,7,setOf(1,3,7),1700000000700), migrated.getPlans().single())
        } finally { migrated.close(); context.deleteDatabase(StudyDatabase.DB_NAME) }
    }

    @Test fun invalidLegacyRecordAbortsMigrationAndPreservesOriginalDatabase() {
        context.deleteDatabase(StudyDatabase.DB_NAME)
        val file = context.getDatabasePath(StudyDatabase.DB_NAME)
        file.parentFile?.mkdirs()
        val legacy = PlainSQLiteDatabase.openOrCreateDatabase(file, null)
        legacy.execSQL("CREATE TABLE bookmarks (id TEXT PRIMARY KEY, book_id TEXT NOT NULL, chapter INTEGER NOT NULL, verse INTEGER NOT NULL, created_at INTEGER NOT NULL, label TEXT NOT NULL DEFAULT '')")
        legacy.execSQL("INSERT INTO bookmarks VALUES ('bad-1','GEN',0,2,10,'must survive')")
        legacy.version = 2
        legacy.close()

        assertThrows(Exception::class.java) { StudyDatabase(context) }
        assertTrue(file.exists())
        val stillPlain = PlainSQLiteDatabase.openDatabase(file.absolutePath, null, PlainSQLiteDatabase.OPEN_READONLY)
        try {
            stillPlain.rawQuery("SELECT label FROM bookmarks WHERE id='bad-1'", null).use {
                assertTrue(it.moveToFirst())
                assertEquals("must survive", it.getString(0))
            }
        } finally { stillPlain.close(); context.deleteDatabase(StudyDatabase.DB_NAME) }
    }

    @Test fun failedTransactionalBackupRestoreLeavesExistingStudyDataIntact() {
        context.deleteDatabase(StudyDatabase.DB_NAME)
        val database = StudyDatabase(context)
        try {
            database.addBookmark(Bookmark("existing","GEN",1,1,100,"keep this"))
            val invalid = JSONObject()
                .put("format","maale-bible-study-backup")
                .put("version",1)
                .put("bookmarks", org.json.JSONArray()
                    .put(JSONObject().put("id","duplicate").put("bookId","GEN").put("chapter",1).put("verse",1).put("createdAt",1).put("label","first"))
                    .put(JSONObject().put("id","duplicate").put("bookId","EXO").put("chapter",2).put("verse",3).put("createdAt",2).put("label","second")))
            assertThrows(Exception::class.java) { database.restoreBackup(invalid.toString()) }
            assertEquals(listOf(Bookmark("existing","GEN",1,1,100,"keep this")), database.getBookmarks())
        } finally { database.close(); context.deleteDatabase(StudyDatabase.DB_NAME) }
    }

}
