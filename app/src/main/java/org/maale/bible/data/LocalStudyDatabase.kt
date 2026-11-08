package org.maale.bible.data

import android.content.Context
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

private fun SQLiteConnection.execute(sql: String) {
    prepare(sql).use { it.step() }
}

/**
 * Offline study persistence boundary.
 *
 * Schema v3 adds the frozen, versioned canonical identity registry and normalized
 * edition/source/mapping tables. Existing v1/v2 study tables are retained so
 * personal notes and prior synthetic records are not discarded.
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
    fun canonicalBookCount(): Long = scalarLong(
        "SELECT count(*) FROM canonical_book WHERE canon_id='maale-bible:canon:66-v1'"
    )
    fun canonRegistryCount(): Long = scalarLong(
        "SELECT count(*) FROM canon_registry WHERE canon_id='maale-bible:canon:66-v1'"
    )
    fun canonicalBookExists(bookId: String): Boolean {
        connection.prepare(
            "SELECT count(*) FROM canonical_book WHERE canon_id=? AND book_id=?"
        ).use {
            it.bindText(1, CanonicalCanonRegistry.CANON_ID)
            it.bindText(2, bookId.uppercase())
            it.step()
            return it.getLong(0) == 1L
        }
    }

    fun canonicalKey(bookId: String, chapter: Int, verse: Int, segment: String = ""): String {
        require(CanonicalCanonRegistry.contains(bookId)) { "Unknown canonical book: $bookId" }
        require(chapter > 0 && verse > 0)
        require(segment.isEmpty() || segment.matches(Regex("[A-Za-z0-9_-]{1,16}")))
        return "${CanonicalCanonRegistry.CANON_ID}/${bookId.uppercase()}/$chapter/$verse" +
            if (segment.isEmpty()) "" else "/$segment"
    }

    fun deleteCanonical(id: String) {
        connection.prepare("DELETE FROM canonical_passages WHERE id=?").use {
            it.bindText(1, id); it.step()
        }
    }

    private fun scalarLong(sql: String): Long =
        connection.prepare(sql).use { it.step(); it.getLong(0) }

    companion object {
        const val SCHEMA_VERSION = 3

        fun open(context: Context, name: String = "maale-study.db"): LocalStudyDatabase {
            val file = context.getDatabasePath(name)
            file.parentFile?.mkdirs()
            return openFile(file)
        }

        internal fun openFile(file: File): LocalStudyDatabase {
            file.parentFile?.mkdirs()
            val connection = BundledSQLiteDriver().open(file.absolutePath)
            connection.execute("PRAGMA foreign_keys = ON")
            val version = connection.prepare("PRAGMA user_version").use { it.step(); it.getLong(0) }
            when (version) {
                0L -> {
                    createV1(connection)
                    migrateV1ToV2(connection)
                    migrateV2ToV3(connection)
                }
                1L -> {
                    migrateV1ToV2(connection)
                    migrateV2ToV3(connection)
                }
                2L -> migrateV2ToV3(connection)
                SCHEMA_VERSION.toLong() -> Unit
                else -> {
                    connection.close()
                    error("Unsupported study database schema version: $version")
                }
            }
            return LocalStudyDatabase(connection)
        }

        private fun createV1(db: SQLiteConnection) {
            db.execute("BEGIN IMMEDIATE")
            try {
                db.execute("""CREATE TABLE canonical_passages(
                    id TEXT PRIMARY KEY NOT NULL,
                    book_id TEXT NOT NULL,
                    chapter INTEGER NOT NULL CHECK(chapter > 0),
                    verse_start INTEGER NOT NULL CHECK(verse_start > 0),
                    verse_end INTEGER NOT NULL CHECK(verse_end >= verse_start)
                )""")
                db.execute("""CREATE TABLE editions(
                    id TEXT PRIMARY KEY NOT NULL,
                    language_tag TEXT NOT NULL,
                    title TEXT NOT NULL
                )""")
                db.execute("""CREATE TABLE edition_passage_map(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    edition_id TEXT NOT NULL REFERENCES editions(id) ON DELETE CASCADE,
                    edition_ref TEXT NOT NULL,
                    canonical_id TEXT REFERENCES canonical_passages(id) ON DELETE CASCADE,
                    mapping_status TEXT NOT NULL CHECK(mapping_status IN ('EXACT','SPLIT','MERGED','PARTIAL','UNMAPPED')),
                    UNIQUE(edition_id, edition_ref)
                )""")
                db.execute("CREATE INDEX index_mapping_canonical ON edition_passage_map(canonical_id)")
                db.execute("PRAGMA user_version = 1")
                db.execute("COMMIT")
            } catch (t: Throwable) {
                db.execute("ROLLBACK")
                throw t
            }
        }

        private fun migrateV1ToV2(db: SQLiteConnection) {
            db.execute("BEGIN IMMEDIATE")
            try {
                db.execute("""CREATE TABLE study_notes(
                    id TEXT PRIMARY KEY NOT NULL,
                    canonical_id TEXT NOT NULL REFERENCES canonical_passages(id) ON DELETE CASCADE,
                    body TEXT NOT NULL CHECK(length(body) > 0),
                    created_at INTEGER NOT NULL CHECK(created_at >= 0)
                )""")
                db.execute("CREATE INDEX index_notes_canonical ON study_notes(canonical_id)")
                db.execute("PRAGMA user_version = 2")
                db.execute("COMMIT")
            } catch (t: Throwable) {
                db.execute("ROLLBACK")
                throw t
            }
        }

        private fun migrateV2ToV3(db: SQLiteConnection) {
            db.execute("BEGIN IMMEDIATE")
            try {
                db.execute("""CREATE TABLE canon_registry(
                    canon_id TEXT PRIMARY KEY NOT NULL,
                    version INTEGER NOT NULL CHECK(version > 0),
                    status TEXT NOT NULL CHECK(status IN ('FROZEN')),
                    book_count INTEGER NOT NULL CHECK(book_count = 66),
                    created_by TEXT NOT NULL,
                    UNIQUE(canon_id, version)
                )""")
                db.execute("""CREATE TABLE canonical_book(
                    canon_id TEXT NOT NULL,
                    book_id TEXT NOT NULL CHECK(length(book_id)=3 AND book_id=upper(book_id)),
                    canonical_order INTEGER NOT NULL CHECK(canonical_order BETWEEN 1 AND 66),
                    canonical_name TEXT NOT NULL,
                    testament TEXT NOT NULL CHECK(testament IN ('OT','NT')),
                    PRIMARY KEY(canon_id, book_id),
                    UNIQUE(canon_id, canonical_order),
                    FOREIGN KEY(canon_id) REFERENCES canon_registry(canon_id)
                        ON UPDATE RESTRICT ON DELETE RESTRICT
                )""")
                db.execute("""CREATE TABLE canonical_passage(
                    canon_id TEXT NOT NULL,
                    book_id TEXT NOT NULL,
                    chapter_number INTEGER NOT NULL CHECK(chapter_number > 0),
                    verse_number INTEGER NOT NULL CHECK(verse_number > 0),
                    segment TEXT NOT NULL DEFAULT '' CHECK(segment='' OR
                        (length(segment)<=16 AND segment NOT GLOB '*[^A-Za-z0-9_-]*')),
                    canonical_key TEXT NOT NULL UNIQUE,
                    PRIMARY KEY(canon_id,book_id,chapter_number,verse_number,segment),
                    FOREIGN KEY(canon_id,book_id) REFERENCES canonical_book(canon_id,book_id)
                        ON UPDATE RESTRICT ON DELETE RESTRICT,
                    CHECK(canonical_key = canon_id || '/' || book_id || '/' ||
                        chapter_number || '/' || verse_number ||
                        CASE WHEN segment='' THEN '' ELSE '/' || segment END)
                )""")
                db.execute("""CREATE TABLE edition(
                    edition_id TEXT PRIMARY KEY NOT NULL,
                    language_tag TEXT NOT NULL,
                    title TEXT NOT NULL,
                    publisher TEXT NOT NULL DEFAULT '',
                    rights_statement TEXT NOT NULL DEFAULT '',
                    attribution TEXT NOT NULL DEFAULT '',
                    source_uri TEXT NOT NULL DEFAULT '',
                    source_sha256 TEXT NOT NULL DEFAULT '' CHECK(source_sha256='' OR
                        (length(source_sha256)=64 AND source_sha256 NOT GLOB '*[^0-9a-f]*')),
                    versification_profile TEXT NOT NULL,
                    metadata_json TEXT NOT NULL DEFAULT '{}'
                )""")
                db.execute("""CREATE TABLE edition_passage(
                    edition_passage_id TEXT PRIMARY KEY NOT NULL,
                    edition_id TEXT NOT NULL,
                    source_book_id TEXT NOT NULL,
                    source_chapter TEXT NOT NULL,
                    source_verse TEXT NOT NULL,
                    source_file TEXT NOT NULL,
                    source_file_sha256 TEXT NOT NULL CHECK(length(source_file_sha256)=64 AND
                        source_file_sha256 NOT GLOB '*[^0-9a-f]*'),
                    source_marker TEXT NOT NULL DEFAULT '',
                    parser_version TEXT NOT NULL,
                    UNIQUE(edition_id,source_book_id,source_chapter,source_verse),
                    FOREIGN KEY(edition_id) REFERENCES edition(edition_id)
                        ON UPDATE RESTRICT ON DELETE CASCADE
                )""")
                db.execute("""CREATE TABLE passage_mapping(
                    mapping_id TEXT PRIMARY KEY NOT NULL,
                    mapping_status TEXT NOT NULL CHECK(mapping_status IN
                        ('EXACT','SPLIT','MERGED','PARTIAL','UNMAPPED')),
                    provenance TEXT NOT NULL,
                    review_status TEXT NOT NULL CHECK(review_status IN
                        ('UNREVIEWED','REVIEWED','APPROVED','REJECTED')),
                    mapping_version TEXT NOT NULL,
                    notes TEXT NOT NULL DEFAULT ''
                )""")
                db.execute("""CREATE TABLE passage_mapping_source(
                    mapping_id TEXT NOT NULL,
                    edition_passage_id TEXT NOT NULL,
                    source_order INTEGER NOT NULL CHECK(source_order > 0),
                    PRIMARY KEY(mapping_id,source_order),
                    UNIQUE(mapping_id,edition_passage_id),
                    FOREIGN KEY(mapping_id) REFERENCES passage_mapping(mapping_id)
                        ON UPDATE RESTRICT ON DELETE CASCADE,
                    FOREIGN KEY(edition_passage_id) REFERENCES edition_passage(edition_passage_id)
                        ON UPDATE RESTRICT ON DELETE CASCADE
                )""")
                db.execute("""CREATE TABLE passage_mapping_target(
                    mapping_id TEXT NOT NULL,
                    canon_id TEXT NOT NULL,
                    book_id TEXT NOT NULL,
                    chapter_number INTEGER NOT NULL,
                    verse_number INTEGER NOT NULL,
                    segment TEXT NOT NULL DEFAULT '',
                    target_order INTEGER NOT NULL CHECK(target_order>0),
                    PRIMARY KEY(mapping_id,target_order),
                    UNIQUE(mapping_id,canon_id,book_id,chapter_number,verse_number,segment),
                    FOREIGN KEY(mapping_id) REFERENCES passage_mapping(mapping_id)
                        ON UPDATE RESTRICT ON DELETE CASCADE,
                    FOREIGN KEY(canon_id,book_id,chapter_number,verse_number,segment)
                        REFERENCES canonical_passage(canon_id,book_id,chapter_number,verse_number,segment)
                        ON UPDATE RESTRICT ON DELETE RESTRICT
                )""")
                db.execute("CREATE INDEX index_canonical_passage_book_chapter ON canonical_passage(canon_id,book_id,chapter_number,verse_number)")
                db.execute("CREATE INDEX index_edition_passage_edition ON edition_passage(edition_id,source_book_id,source_chapter)")
                db.execute("CREATE INDEX index_mapping_source_edition_passage ON passage_mapping_source(edition_passage_id)")
                db.execute("CREATE INDEX index_mapping_target_canonical ON passage_mapping_target(canon_id,book_id,chapter_number,verse_number)")
                db.execute("CREATE TRIGGER canonical_book_no_update BEFORE UPDATE ON canonical_book BEGIN SELECT RAISE(ABORT,'canonical book registry is immutable'); END")
                db.execute("CREATE TRIGGER canonical_book_no_delete BEFORE DELETE ON canonical_book BEGIN SELECT RAISE(ABORT,'canonical book registry is immutable'); END")
                db.execute("CREATE TRIGGER canon_registry_no_update BEFORE UPDATE ON canon_registry BEGIN SELECT RAISE(ABORT,'canon registry is immutable'); END")
                db.execute("CREATE TRIGGER canon_registry_no_delete BEFORE DELETE ON canon_registry BEGIN SELECT RAISE(ABORT,'canon registry is immutable'); END")

                db.prepare("INSERT INTO canon_registry(canon_id,version,status,book_count,created_by) VALUES(?,?,'FROZEN',66,'owner-approved-adr-a4-001')").use {
                    it.bindText(1, CanonicalCanonRegistry.CANON_ID)
                    it.bindLong(2, CanonicalCanonRegistry.VERSION.toLong())
                    it.step()
                }
                db.prepare("INSERT INTO canonical_book(canon_id,book_id,canonical_order,canonical_name,testament) VALUES(?,?,?,?,?)").use { statement ->
                    CanonicalCanonRegistry.books.forEach { book ->
                        statement.bindText(1, CanonicalCanonRegistry.CANON_ID)
                        statement.bindText(2, book.usfmId)
                        statement.bindLong(3, book.order.toLong())
                        statement.bindText(4, book.canonicalName)
                        statement.bindText(5, book.testament)
                        statement.step()
                        statement.reset()
                    }
                }
                // Freeze insertion only after the canonical registry has been seeded.
                // The registry is immutable as a set, not merely immutable per existing row.
                db.execute("CREATE TRIGGER canon_registry_no_insert BEFORE INSERT ON canon_registry BEGIN SELECT RAISE(ABORT,'canon registry is frozen'); END")
                db.execute("CREATE TRIGGER canonical_book_no_insert BEFORE INSERT ON canonical_book BEGIN SELECT RAISE(ABORT,'canonical book registry is frozen'); END")
                db.execute("PRAGMA user_version = 3")
                db.execute("COMMIT")
            } catch (t: Throwable) {
                db.execute("ROLLBACK")
                throw t
            }
        }
    }
}
