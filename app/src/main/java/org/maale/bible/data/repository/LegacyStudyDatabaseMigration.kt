package org.maale.bible.data.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase as PlainSQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteDatabase as CipherSQLiteDatabase
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * One-time, crash-recoverable plaintext-to-SQLCipher cutover.
 * The legacy source is retained until the encrypted target passes integrity and row-count checks.
 */
internal object LegacyStudyDatabaseMigration {
    private const val TEMP = "maale_bible_study.enc.tmp"
    private const val LEGACY = "maale_bible_study.plaintext.rollback"

    fun migrate(context: Context, dek: ByteArray) {
        synchronized(this) {
            System.loadLibrary("sqlcipher")
            val dir = context.applicationContext.getDatabasePath(StudyDatabase.DB_NAME).parentFile!!
            val live = File(dir, StudyDatabase.DB_NAME)
            val temp = File(dir, TEMP)
            val rollback = File(dir, LEGACY)

            // Recover interrupted rename/cutover before attempting a new migration.
            if (rollback.exists()) {
                if (live.exists() && isEncryptedAndHealthy(live, dek)) {
                    rollback.delete()
                } else if (!live.exists() && temp.exists() && isEncryptedAndHealthy(temp, dek)) {
                    atomicMove(temp, live)
                    check(isEncryptedAndHealthy(live, dek)) { "Encrypted cutover verification failed" }
                    rollback.delete()
                } else {
                    if (live.exists()) deleteDatabaseFiles(live)
                    if (temp.exists()) deleteDatabaseFiles(temp)
                    atomicMove(rollback, live)
                    deleteSidecars(live)
                }
            }

            if (!live.exists()) {
                if (temp.exists()) deleteDatabaseFiles(temp)
                return
            }
            if (isEncryptedAndHealthy(live, dek)) return
            require(isPlainSqlite(live)) {
                "Existing database is neither valid SQLCipher nor a supported legacy SQLite database"
            }

            // Open/close legacy DB first to checkpoint any committed WAL content.
            val source = PlainSQLiteDatabase.openDatabase(live.absolutePath, null, PlainSQLiteDatabase.OPEN_READWRITE)
            try {
                runCatching { source.rawQuery("PRAGMA wal_checkpoint(FULL)", null).use { while (it.moveToNext()) Unit } }
                buildEncryptedCopy(source, temp, dek)
            } finally {
                source.close()
            }

            check(isEncryptedAndHealthy(temp, dek)) { "Encrypted temporary database failed integrity verification" }
            fsync(temp)
            atomicMove(live, rollback)
            deleteSidecars(live)
            try {
                atomicMove(temp, live)
                check(isEncryptedAndHealthy(live, dek)) { "Encrypted database failed post-cutover verification" }
                deleteDatabaseFiles(rollback)
            } catch (failure: Throwable) {
                if (live.exists()) deleteDatabaseFiles(live)
                if (rollback.exists()) atomicMove(rollback, live)
                throw failure
            }
        }
    }

    private fun buildEncryptedCopy(source: PlainSQLiteDatabase, target: File, dek: ByteArray) {
        deleteDatabaseFiles(target)
        val encrypted = CipherSQLiteDatabase.openOrCreateDatabase(target, dek, null, null)
        try {
            createSchema(encrypted)
            encrypted.beginTransaction()
            try {
                val expected = linkedMapOf<String, Int>()
                TABLES.forEach { table ->
                    check(tableExists(source, table)) { "Legacy database is missing required table: $table" }
                    expected[table] = copyTable(source, encrypted, table)
                }
                encrypted.setTransactionSuccessful()
                encrypted.endTransaction()
                encrypted.version = StudyDatabase.DB_VERSION
                TABLES.forEach { table ->
                    val actual = count(encrypted, table)
                    check(actual == expected.getValue(table)) { "Migration row-count mismatch for $table" }
                }
            } catch (failure: Throwable) {
                if (encrypted.inTransaction()) encrypted.endTransaction()
                throw failure
            }
        } finally {
            encrypted.close()
        }
    }

    private fun createSchema(db: CipherSQLiteDatabase) {
        db.execSQL("CREATE TABLE reading_position (singleton INTEGER PRIMARY KEY CHECK(singleton=1), book_id TEXT NOT NULL, chapter INTEGER NOT NULL CHECK(chapter>0), verse INTEGER NOT NULL CHECK(verse>0), updated_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE bookmarks (id TEXT PRIMARY KEY, book_id TEXT NOT NULL, chapter INTEGER NOT NULL CHECK(chapter>0), verse INTEGER NOT NULL CHECK(verse>0), created_at INTEGER NOT NULL, label TEXT NOT NULL DEFAULT '')")
        db.execSQL("CREATE INDEX idx_bookmarks_ref ON bookmarks(book_id,chapter,verse)")
        db.execSQL("CREATE TABLE annotations (id TEXT PRIMARY KEY, book_id TEXT NOT NULL, chapter INTEGER NOT NULL CHECK(chapter>0), verse INTEGER NOT NULL CHECK(verse>0), kind TEXT NOT NULL CHECK(kind IN ('HIGHLIGHT','NOTE')), body TEXT NOT NULL, color TEXT, created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL)")
        db.execSQL("CREATE INDEX idx_annotations_ref ON annotations(book_id,chapter,verse)")
        db.execSQL("CREATE TABLE reading_plans (id TEXT PRIMARY KEY, title TEXT NOT NULL, start_day INTEGER NOT NULL, duration_days INTEGER NOT NULL, completed_days TEXT NOT NULL, updated_at INTEGER NOT NULL)")
        db.execSQL("CREATE INDEX idx_plans_updated ON reading_plans(updated_at)")
    }

    private fun copyTable(source: PlainSQLiteDatabase, target: CipherSQLiteDatabase, table: String): Int {
        var copied = 0
        source.rawQuery("SELECT * FROM $table", null).use { cursor ->
            while (cursor.moveToNext()) {
                val values = cursor.toContentValues()
                when (table) {
                    "reading_position", "bookmarks", "annotations" -> {
                        check(values.getAsInteger("chapter") > 0 && values.getAsInteger("verse") > 0) {
                            "Invalid reference coordinates in legacy $table"
                        }
                    }
                    "reading_plans" -> check(values.getAsInteger("duration_days") > 0) {
                        "Invalid duration in legacy reading plan"
                    }
                }
                if (table == "annotations") {
                    check(values.getAsString("kind") in setOf("NOTE", "HIGHLIGHT")) {
                        "Unknown annotation kind in legacy database"
                    }
                }
                target.insertOrThrow(table, null, values)
                copied++
            }
        }
        return copied
    }

    private fun Cursor.toContentValues(): ContentValues = ContentValues().also { values ->
        for (i in 0 until columnCount) {
            val name = getColumnName(i)
            if (isNull(i)) values.putNull(name) else when (getType(i)) {
                Cursor.FIELD_TYPE_INTEGER -> values.put(name, getLong(i))
                Cursor.FIELD_TYPE_FLOAT -> values.put(name, getDouble(i))
                Cursor.FIELD_TYPE_BLOB -> values.put(name, getBlob(i))
                else -> values.put(name, getString(i))
            }
        }
    }

    private fun tableExists(db: PlainSQLiteDatabase, table: String): Boolean =
        db.rawQuery("SELECT 1 FROM sqlite_master WHERE type='table' AND name=?", arrayOf(table)).use { it.moveToFirst() }

    private fun count(db: CipherSQLiteDatabase, table: String): Int =
        db.rawQuery("SELECT COUNT(*) FROM $table", null).use { if (it.moveToFirst()) it.getInt(0) else 0 }

    private fun isEncryptedAndHealthy(file: File, dek: ByteArray): Boolean = runCatching {
        val db = CipherSQLiteDatabase.openDatabase(file.absolutePath, dek, null, CipherSQLiteDatabase.OPEN_READONLY, null)
        try {
            db.rawQuery("PRAGMA cipher_integrity_check", null).use { c ->
                c.moveToFirst() && c.getString(0).equals("ok", true)
            }
        } finally { db.close() }
    }.getOrDefault(false)

    private fun isPlainSqlite(file: File): Boolean = runCatching {
        file.inputStream().use { input ->
            val header = ByteArray(16)
            input.read(header) == 16 && String(header, Charsets.US_ASCII) == "SQLite format 3\u0000"
        }
    }.getOrDefault(false)

    private fun fsync(file: File) {
        FileOutputStream(file, true).use { it.fd.sync() }
    }

    private fun atomicMove(from: File, to: File) {
        Files.move(from.toPath(), to.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        FileOutputStream(to.parentFile, true).use { runCatching { it.fd.sync() } }
    }

    private fun deleteSidecars(file: File) {
        listOf("-wal", "-shm", "-journal").forEach { File(file.path + it).delete() }
    }

    private fun deleteDatabaseFiles(file: File) {
        CipherSQLiteDatabase.deleteDatabase(file)
        PlainSQLiteDatabase.deleteDatabase(file)
        deleteSidecars(file)
    }

    private val TABLES = listOf("reading_position", "bookmarks", "annotations", "reading_plans")
}
