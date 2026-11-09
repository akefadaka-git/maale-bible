package org.maale.bible

import android.content.Context
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.maale.bible.data.LocalStudyDatabase
import java.io.File

private fun androidx.sqlite.SQLiteConnection.execute(sql: String) {
    prepare(sql).use { it.step() }
}

@RunWith(AndroidJUnit4::class)
class P2V2LocalStudyPersistenceTest {
    private fun tempDb(name: String): File {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val file = context.getDatabasePath(name)
        file.parentFile?.mkdirs()
        file.delete()
        return file
    }

    @Test fun freshDatabaseCreatesCurrentSchemaAndPersistsSyntheticRecords() {
        val file = tempDb("p2-v2-fresh.db")
        LocalStudyDatabase.openFile(file).use { db ->
            assertEquals(4L, db.userVersion())
            db.insertCanonical("GEN.1.1", "GEN", 1, 1, 1)
            db.insertEdition("synthetic-en", "en", "Synthetic English")
            db.mapEditionPassage("synthetic-en", "GEN 1:1", "GEN.1.1", "EXACT")
            db.addNote("note-1", "GEN.1.1", "Synthetic note only", 100L)
            assertEquals(1L, db.mappingCount())
            assertEquals(1L, db.noteCount())
        }
        file.delete()
    }

    @Test fun v1ToV2MigrationPreservesExistingPassagesAndMappings() {
        val file = tempDb("p2-v2-migration.db")
        val legacy = BundledSQLiteDriver().open(file.absolutePath)
        legacy.execute("PRAGMA foreign_keys = ON")
        legacy.execute("CREATE TABLE canonical_passages(id TEXT PRIMARY KEY NOT NULL, book_id TEXT NOT NULL, chapter INTEGER NOT NULL CHECK(chapter > 0), verse_start INTEGER NOT NULL CHECK(verse_start > 0), verse_end INTEGER NOT NULL CHECK(verse_end >= verse_start))")
        legacy.execute("CREATE TABLE editions(id TEXT PRIMARY KEY NOT NULL, language_tag TEXT NOT NULL, title TEXT NOT NULL)")
        legacy.execute("CREATE TABLE edition_passage_map(id INTEGER PRIMARY KEY AUTOINCREMENT, edition_id TEXT NOT NULL REFERENCES editions(id) ON DELETE CASCADE, edition_ref TEXT NOT NULL, canonical_id TEXT REFERENCES canonical_passages(id) ON DELETE CASCADE, mapping_status TEXT NOT NULL CHECK(mapping_status IN ('EXACT','SPLIT','MERGED','PARTIAL','UNMAPPED')), UNIQUE(edition_id, edition_ref))")
        legacy.execute("INSERT INTO canonical_passages VALUES('PS.1.1','PS',1,1,1)")
        legacy.execute("INSERT INTO editions VALUES('syn','en','Synthetic')")
        legacy.execute("INSERT INTO edition_passage_map(edition_id,edition_ref,canonical_id,mapping_status) VALUES('syn','PS 1:1','PS.1.1','EXACT')")
        legacy.execute("PRAGMA user_version = 1")
        legacy.close()

        LocalStudyDatabase.openFile(file).use { db ->
            assertEquals(3L, db.userVersion())
            assertEquals(1L, db.mappingCount())
            db.addNote("migrated-note", "PS.1.1", "Added after migration", 200L)
            assertEquals(1L, db.noteCount())
        }
        file.delete()
    }

    @Test fun foreignKeysRejectOrphanMappingsAndNotes() {
        val file = tempDb("p2-v2-fk.db")
        LocalStudyDatabase.openFile(file).use { db ->
            db.insertEdition("syn", "en", "Synthetic")
            try {
                db.mapEditionPassage("syn", "NO SUCH PASSAGE", "missing", "EXACT")
                fail("orphan mapping must be rejected")
            } catch (_: Exception) { }
            try {
                db.addNote("orphan-note", "missing", "Synthetic", 1L)
                fail("orphan note must be rejected")
            } catch (_: Exception) { }
            assertEquals(0L, db.mappingCount())
            assertEquals(0L, db.noteCount())
        }
        file.delete()
    }

    @Test fun duplicateEditionReferenceIsRejected() {
        val file = tempDb("p2-v2-unique.db")
        LocalStudyDatabase.openFile(file).use { db ->
            db.insertCanonical("EX.1.1", "EX", 1, 1, 1)
            db.insertEdition("syn", "en", "Synthetic")
            db.mapEditionPassage("syn", "EX 1:1", "EX.1.1", "EXACT")
            try {
                db.mapEditionPassage("syn", "EX 1:1", "EX.1.1", "EXACT")
                fail("duplicate edition reference must be rejected")
            } catch (_: Exception) { }
            assertEquals(1L, db.mappingCount())
        }
        file.delete()
    }

    @Test fun deletingCanonicalPassageCascadesMappingsAndPersonalNotes() {
        val file = tempDb("p2-v2-cascade.db")
        LocalStudyDatabase.openFile(file).use { db ->
            db.insertCanonical("MAT.1.1", "MAT", 1, 1, 1)
            db.insertEdition("syn", "en", "Synthetic")
            db.mapEditionPassage("syn", "MAT 1:1", "MAT.1.1", "EXACT")
            db.addNote("note", "MAT.1.1", "Synthetic private note", 300L)
            db.deleteCanonical("MAT.1.1")
            assertEquals(0L, db.mappingCount())
            assertEquals(0L, db.noteCount())
        }
        file.delete()
    }

    @Test fun frozenCanonicalRegistrySeedsExactlySixtySixOrderedBooks() {
        val file = tempDb("p3-canon-registry.db")
        LocalStudyDatabase.openFile(file).use { db ->
            assertEquals(3L, db.userVersion())
            assertEquals(1L, db.canonRegistryCount())
            assertEquals(66L, db.canonicalBookCount())
            assertTrue(db.canonicalBookExists("gen"))
            assertTrue(db.canonicalBookExists("REV"))
            assertFalse(db.canonicalBookExists("TOB"))
            assertEquals(
                "maale-bible:canon:66-v1/GEN/1/1",
                db.canonicalKey("gen", 1, 1)
            )
            assertEquals(
                "maale-bible:canon:66-v1/GEN/1/1/a",
                db.canonicalKey("GEN", 1, 1, "a")
            )
        }
        file.delete()
    }

    @Test fun frozenRegistryRejectsAdditionalRegistryAndBookRows() {
        val file = tempDb("p3-canon-frozen-insert.db")
        LocalStudyDatabase.openFile(file).close()
        val db = BundledSQLiteDriver().open(file.absolutePath)
        try {
            db.execute("PRAGMA foreign_keys = ON")
            try {
                db.execute("INSERT INTO canon_registry(canon_id,version,status,book_count,created_by) VALUES('other:canon:66-v1',1,'FROZEN',66,'test')")
                fail("a second canonical registry must be rejected")
            } catch (_: Exception) { }
            try {
                db.execute("INSERT INTO canonical_book(canon_id,book_id,canonical_order,canonical_name,testament) VALUES('maale-bible:canon:66-v1','TOB',67,'Tobit','OT')")
                fail("a frozen registry must reject additional books")
            } catch (_: Exception) { }
            try {
                db.execute("UPDATE canonical_book SET canonical_name='Changed' WHERE book_id='GEN'")
                fail("a frozen registry must reject book mutation")
            } catch (_: Exception) { }
            try {
                db.execute("DELETE FROM canonical_book WHERE book_id='GEN'")
                fail("a frozen registry must reject book deletion")
            } catch (_: Exception) { }
        } finally {
            db.close()
            file.delete()
        }
    }

    @Test fun reviewedExactMappingRequiresOneSourceAndOneTarget() {
        val file = tempDb("p3-mapping-cardinality.db")
        LocalStudyDatabase.openFile(file).close()
        val db = BundledSQLiteDriver().open(file.absolutePath)
        try {
            db.execute("PRAGMA foreign_keys = ON")
            db.execute("INSERT INTO edition(edition_id,language_tag,title,versification_profile) VALUES('syn','en','Synthetic','test-v1')")
            db.execute("INSERT INTO edition_passage(edition_passage_id,edition_id,source_book_id,source_chapter,source_verse,source_file,source_file_sha256,source_marker,parser_version) VALUES('src-1','syn','GEN','1','1','GEN.usfm','0000000000000000000000000000000000000000000000000000000000000000','','test-parser')")
            db.execute("INSERT INTO passage_mapping(mapping_id,mapping_status,provenance,review_status,mapping_version) VALUES('map-1','EXACT','synthetic-test','UNREVIEWED','v1')")
            db.execute("INSERT INTO passage_mapping_source(mapping_id,edition_passage_id,source_order) VALUES('map-1','src-1',1)")
            try {
                db.execute("UPDATE passage_mapping SET review_status='REVIEWED' WHERE mapping_id='map-1'")
                fail("EXACT mapping without a target must not be reviewable")
            } catch (_: Exception) { }
            db.execute("INSERT INTO canonical_passage(canon_id,book_id,chapter_number,verse_number,segment,canonical_key) VALUES('maale-bible:canon:66-v1','GEN',1,1,'','maale-bible:canon:66-v1/GEN/1/1')")
            db.execute("INSERT INTO passage_mapping_target(mapping_id,canon_id,book_id,chapter_number,verse_number,segment,target_order) VALUES('map-1','maale-bible:canon:66-v1','GEN',1,1,'',1)")
            db.execute("UPDATE passage_mapping SET review_status='REVIEWED' WHERE mapping_id='map-1'")
            try {
                db.execute("DELETE FROM passage_mapping_target WHERE mapping_id='map-1'")
                fail("reviewed mapping targets must be immutable")
            } catch (_: Exception) { }
        } finally {
            db.close()
            file.delete()
        }
    }

    @Test fun canonicalKeyRejectsUnknownBooksAndInvalidCoordinates() {
        val file = tempDb("p3-canon-key-validation.db")
        LocalStudyDatabase.openFile(file).use { db ->
            try {
                db.canonicalKey("TOB", 1, 1)
                fail("book outside frozen 66-book registry must be rejected")
            } catch (_: IllegalArgumentException) { }
            try {
                db.canonicalKey("GEN", 0, 1)
                fail("chapter zero must be rejected")
            } catch (_: IllegalArgumentException) { }
            try {
                db.canonicalKey("GEN", 1, 1, "bad/segment")
                fail("segment containing slash must be rejected")
            } catch (_: IllegalArgumentException) { }
        }
        file.delete()
    }

    @Test fun invalidPassageRangesAreRejectedBeforePersistence() {
        val file = tempDb("p2-v2-range.db")
        LocalStudyDatabase.openFile(file).use { db ->
            try {
                db.insertCanonical("BAD", "GEN", 1, 5, 2)
                fail("reversed verse range must be rejected")
            } catch (_: IllegalArgumentException) { }
        }
        file.delete()
    }

    @Test fun journalNotesPersistUpdateAndDeleteWithoutPassageReferences() {
        val file = tempDb("p4-journal-crud.db")
        LocalStudyDatabase.openFile(file).use { db ->
            assertEquals(4L, db.userVersion())
            db.addJournalNote("journal-1", "My private reflection", 100L)
            assertEquals(1, db.journalNotes().size)
            assertNull(db.journalNotes().first().canonicalId)
            assertEquals("My private reflection", db.journalNotes().first().body)
        }
        LocalStudyDatabase.openFile(file).use { db ->
            assertEquals("My private reflection", db.journalNotes().single().body)
            db.updateJournalNote("journal-1", "Updated reflection", 250L)
            val updated = db.journalNotes().single()
            assertEquals("Updated reflection", updated.body)
            assertEquals(100L, updated.createdAt)
            assertEquals(250L, updated.updatedAt)
            assertTrue(db.deleteJournalNote("journal-1"))
            assertFalse(db.deleteJournalNote("journal-1"))
            assertTrue(db.journalNotes().isEmpty())
        }
        file.delete()
    }

    @Test fun v3MigrationPreservesPassageNotesAndAddsJournalSupport() {
        val file = tempDb("p4-v3-migration.db")
        val legacy = BundledSQLiteDriver().open(file.absolutePath)
        legacy.execute("PRAGMA foreign_keys = ON")
        legacy.execute("CREATE TABLE canonical_passages(id TEXT PRIMARY KEY NOT NULL, book_id TEXT NOT NULL, chapter INTEGER NOT NULL, verse_start INTEGER NOT NULL, verse_end INTEGER NOT NULL)")
        legacy.execute("CREATE TABLE study_notes(id TEXT PRIMARY KEY NOT NULL, canonical_id TEXT NOT NULL REFERENCES canonical_passages(id) ON DELETE CASCADE, body TEXT NOT NULL, created_at INTEGER NOT NULL)")
        legacy.execute("INSERT INTO canonical_passages VALUES('GEN.1.1','GEN',1,1,1)")
        legacy.execute("INSERT INTO study_notes VALUES('legacy-note','GEN.1.1','Preserve me',50)")
        legacy.execute("PRAGMA user_version = 3")
        legacy.close()

        LocalStudyDatabase.openFile(file).use { db ->
            assertEquals(4L, db.userVersion())
            assertEquals(1L, db.noteCount())
            db.addJournalNote("journal-v4", "Independent journal", 75L)
            assertEquals(1, db.journalNotes().size)
            assertEquals("Preserve me", db.journalNotes().firstOrNull { it.id == "legacy-note" }?.body ?: "legacy note remains passage-linked")
        }
        file.delete()
    }

}
