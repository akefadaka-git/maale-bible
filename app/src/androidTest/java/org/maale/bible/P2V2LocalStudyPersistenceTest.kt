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
            assertEquals(2L, db.userVersion())
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
        legacy.execSQL("PRAGMA foreign_keys = ON")
        legacy.execSQL("CREATE TABLE canonical_passages(id TEXT PRIMARY KEY NOT NULL, book_id TEXT NOT NULL, chapter INTEGER NOT NULL CHECK(chapter > 0), verse_start INTEGER NOT NULL CHECK(verse_start > 0), verse_end INTEGER NOT NULL CHECK(verse_end >= verse_start))")
        legacy.execSQL("CREATE TABLE editions(id TEXT PRIMARY KEY NOT NULL, language_tag TEXT NOT NULL, title TEXT NOT NULL)")
        legacy.execSQL("CREATE TABLE edition_passage_map(id INTEGER PRIMARY KEY AUTOINCREMENT, edition_id TEXT NOT NULL REFERENCES editions(id) ON DELETE CASCADE, edition_ref TEXT NOT NULL, canonical_id TEXT REFERENCES canonical_passages(id) ON DELETE CASCADE, mapping_status TEXT NOT NULL CHECK(mapping_status IN ('EXACT','SPLIT','MERGED','PARTIAL','UNMAPPED')), UNIQUE(edition_id, edition_ref))")
        legacy.execSQL("INSERT INTO canonical_passages VALUES('PS.1.1','PS',1,1,1)")
        legacy.execSQL("INSERT INTO editions VALUES('syn','en','Synthetic')")
        legacy.execSQL("INSERT INTO edition_passage_map(edition_id,edition_ref,canonical_id,mapping_status) VALUES('syn','PS 1:1','PS.1.1','EXACT')")
        legacy.execSQL("PRAGMA user_version = 1")
        legacy.close()

        LocalStudyDatabase.openFile(file).use { db ->
            assertEquals(2L, db.userVersion())
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
}
