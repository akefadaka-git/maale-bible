package org.maale.bible.data

import android.content.Context
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

/**
 * Offline study persistence boundary.
 *
 * Schema v2 stores canonical passage identity, edition mappings and local notes.
 * The database contains metadata and user-authored records only; it never imports
 * scripture text or licensed reference content.
 */
class LocalStudyDatabase private constructor(
    private val connection: SQLiteConnection,
) : AutoCloseable {
    override fun close() = connection.close()

    fun insertCanonical(id: String, bookId: String, chapter: Int, verseStart: Int, verseEnd: Int) {
        require(id.isNotBlank() && bookId.isNotBlank())
        require(chapter > 0 && verseStart > 0 && verseEnd >= verseStart)
        connection.prepare(
            "INSERT INTO canonical_passages(id, book_id, chapter, verse_start, verse_end) VALUES(?,?,?,?,?)"
        ).use {
            it.bindText(1, id); it.bindText(2, bookId); it.bindLong(3, chapter.toLong())
            it.bindLong(4, verseStart.toLong()); it.bindLong(5, verseEnd.toLong())
            it.step()
        }
    }

    fun insertEdition(id: String, languageTag: String, title: String) {
        connection.prepare("INSERT INTO editions(id, language_tag, title) VALUES(?,?,?)").use {
            it.bindText(1, id); it.bindText(2, languageTag); it.bindText(3, title); it.step()
        }
    }

    fun mapEditionPassage(editionId: String, editionRef: String, canonicalId: String, status: String) {
        require(status in setOf("EXACT", "SPLIT", "MERGED", "PARTIAL", "UNMAPPED"))
        connection.prepare(
            "INSERT INTO edition_passage_map(edition_id, edition_ref, canonical_id, mapping_status) VALUES(?,?,?,?)"
        ).use {
            it.bindText(1, editionId); it.bindText(2, editionRef); it.bindText(3, canonicalId)
            it.bindText(4, status); it.step()
        }
    }

    fun addNote(id: String, canonicalId: String, body: String, createdAt: Long) {
        require(id.isNotBlank() && body.isNotBlank() && createdAt >= 0)
        connection.prepare("INSERT INTO study_notes(id, canonical_id, body, created_at) VALUES(?,?,?,?)").use {
            it.bindText(1, id); it.bindText(2, canonicalId); it.bindText(3, body)
            it.bindLong(4, createdAt); it.step()
        }
    }

    fun noteCount(): Long = scalarLong("SELECT count(*) FROM study_notes")
    fun mappingCount(): Long = scalarLong("SELECT count(*) FROM edition_passage_map")
    fun userVersion(): Long = scalarLong("PRAGMA user_version")

    fun deleteCanonical(id: String) {
        connection.prepare("DELETE FROM canonical_passages WHERE id=?").use {
            it.bindText(1, id); it.step()
        }
    }

    private fun scalarLong(sql: String): Long =
        connection.prepare(sql).use { it.step(); it.getLong(0) }

    companion object {
        const val SCHEMA_VERSION = 2

        fun open(context: Context, name: String = "maale-study.db"): LocalStudyDatabase {
            val file = context.getDatabasePath(name)
            file.parentFile?.mkdirs()
            return openFile(file)
        }

        internal fun openFile(file: File): LocalStudyDatabase {
            file.parentFile?.mkdirs()
            val connection = BundledSQLiteDriver().open(file.absolutePath)
            connection.execSQL("PRAGMA foreign_keys = ON")
            val version = connection.prepare("PRAGMA user_version").use { it.step(); it.getLong(0) }
            when (version) {
                0L -> {
                    createV1(connection)
                    migrateV1ToV2(connection)
                }
                1L -> migrateV1ToV2(connection)
                SCHEMA_VERSION.toLong() -> Unit
                else -> {
                    connection.close()
                    error("Unsupported study database schema version: $version")
                }
            }
            return LocalStudyDatabase(connection)
        }

        private fun createV1(db: SQLiteConnection) {
            db.execSQL("BEGIN IMMEDIATE")
            try {
                db.execSQL("""CREATE TABLE canonical_passages(
                    id TEXT PRIMARY KEY NOT NULL,
                    book_id TEXT NOT NULL,
                    chapter INTEGER NOT NULL CHECK(chapter > 0),
                    verse_start INTEGER NOT NULL CHECK(verse_start > 0),
                    verse_end INTEGER NOT NULL CHECK(verse_end >= verse_start)
                )""")
                db.execSQL("""CREATE TABLE editions(
                    id TEXT PRIMARY KEY NOT NULL,
                    language_tag TEXT NOT NULL,
                    title TEXT NOT NULL
                )""")
                db.execSQL("""CREATE TABLE edition_passage_map(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    edition_id TEXT NOT NULL REFERENCES editions(id) ON DELETE CASCADE,
                    edition_ref TEXT NOT NULL,
                    canonical_id TEXT REFERENCES canonical_passages(id) ON DELETE CASCADE,
                    mapping_status TEXT NOT NULL CHECK(mapping_status IN ('EXACT','SPLIT','MERGED','PARTIAL','UNMAPPED')),
                    UNIQUE(edition_id, edition_ref)
                )""")
                db.execSQL("CREATE INDEX index_mapping_canonical ON edition_passage_map(canonical_id)")
                db.execSQL("PRAGMA user_version = 1")
                db.execSQL("COMMIT")
            } catch (t: Throwable) {
                db.execSQL("ROLLBACK")
                throw t
            }
        }

        private fun migrateV1ToV2(db: SQLiteConnection) {
            db.execSQL("BEGIN IMMEDIATE")
            try {
                db.execSQL("""CREATE TABLE study_notes(
                    id TEXT PRIMARY KEY NOT NULL,
                    canonical_id TEXT NOT NULL REFERENCES canonical_passages(id) ON DELETE CASCADE,
                    body TEXT NOT NULL CHECK(length(body) > 0),
                    created_at INTEGER NOT NULL CHECK(created_at >= 0)
                )""")
                db.execSQL("CREATE INDEX index_notes_canonical ON study_notes(canonical_id)")
                db.execSQL("PRAGMA user_version = 2")
                db.execSQL("COMMIT")
            } catch (t: Throwable) {
                db.execSQL("ROLLBACK")
                throw t
            }
        }
    }
}
