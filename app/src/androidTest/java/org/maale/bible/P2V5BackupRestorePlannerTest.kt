package org.maale.bible

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.maale.bible.data.backup.BackupRestoreMode
import org.maale.bible.data.backup.BackupRestorePlanner

@RunWith(AndroidJUnit4::class)
class P2V5BackupRestorePlannerTest {
    @Test fun previewClassifiesAdditionsConflictsAndUnchangedEntries() {
        val existing = mapOf(
            "notes/a.ndjson" to "local".toByteArray(),
            "notes/b.ndjson" to "same".toByteArray()
        )
        val incoming = mapOf(
            "notes/a.ndjson" to "remote".toByteArray(),
            "notes/b.ndjson" to "same".toByteArray(),
            "plans/c.ndjson" to "new".toByteArray()
        )
        val preview = BackupRestorePlanner.preview(existing, incoming)
        assertEquals(setOf("plans/c.ndjson"), preview.additions)
        assertEquals(setOf("notes/a.ndjson"), preview.conflicts)
        assertEquals(setOf("notes/b.ndjson"), preview.unchanged)
    }

    @Test fun defaultMergeAddsNewRecordsWithoutOverwritingConflicts() {
        val local = "local".toByteArray()
        val merged = BackupRestorePlanner.plan(
            mapOf("notes/a.ndjson" to local),
            mapOf(
                "notes/a.ndjson" to "remote".toByteArray(),
                "notes/b.ndjson" to "new".toByteArray()
            )
        )
        assertArrayEquals(local, merged.getValue("notes/a.ndjson"))
        assertArrayEquals("new".toByteArray(), merged.getValue("notes/b.ndjson"))
    }

    @Test fun replaceRequiresExplicitConfirmation() {
        try {
            BackupRestorePlanner.plan(
                mapOf("notes/a.ndjson" to "local".toByteArray()),
                mapOf("notes/a.ndjson" to "remote".toByteArray()),
                BackupRestoreMode.REPLACE
            )
            fail("replace must require explicit confirmation")
        } catch (_: IllegalArgumentException) { }
        val replaced = BackupRestorePlanner.plan(
            mapOf("notes/a.ndjson" to "local".toByteArray()),
            mapOf("notes/a.ndjson" to "remote".toByteArray()),
            BackupRestoreMode.REPLACE,
            confirmReplace = true
        )
        assertArrayEquals("remote".toByteArray(), replaced.getValue("notes/a.ndjson"))
    }
}
