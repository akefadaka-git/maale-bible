package org.maale.bible

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.maale.bible.data.repository.StudyDatabase
import org.maale.bible.data.security.PortableStudyBackup
import org.maale.bible.data.security.StudyDataKeyStore
import org.maale.bible.domain.model.Bookmark
import org.maale.bible.ui.PendingDocumentPickerState
import java.io.File

@RunWith(AndroidJUnit4::class)
class DocumentPickerEndToEndInstrumentedTest {
    private val authority = "content://org.maale.bible.test.documents/documents/"
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val target get() = instrumentation.targetContext
    private val testContext get() = instrumentation.context

    private fun cleanStudyStore() {
        target.deleteDatabase(StudyDatabase.DB_NAME)
        StudyDataKeyStore.clearRecoveredAfterFailedRestore(target)
    }

    @Test fun contentResolverExportAndImportRoundTripThroughDocumentUri() {
        cleanStudyStore()
        val uri = Uri.parse(authority + "backup.json")
        val db = StudyDatabase(target)
        db.addBookmark(Bookmark("picker-roundtrip", "GEN", 1, 1, 101, "document provider test"))
        val recoveryKey = PortableStudyBackup.generateRecoveryKey()

        PortableStudyBackup.exportToUri(target, db, recoveryKey, uri)
        db.close()
        assertNotNull(testContext.contentResolver.openInputStream(uri)?.use { it.read() })

        cleanStudyStore()
        PortableStudyBackup.restoreFromUri(target, uri, recoveryKey)
        val restored = StudyDatabase(target)
        assertEquals("picker-roundtrip", restored.getBookmarks("GEN").single().id)
        assertEquals("document provider test", restored.getBookmarks("GEN").single().label)
        restored.close()
        cleanStudyStore()
        testContext.contentResolver.delete(uri, null, null)
    }

    @Test fun invalidDocumentIsRejectedBeforeAnyRestoreDatabaseIsCreated() {
        cleanStudyStore()
        val uri = Uri.parse(authority + "invalid.json")
        testContext.contentResolver.openOutputStream(uri, "wt")!!.use {
            it.write("{ definitely not a backup".toByteArray(Charsets.UTF_8))
        }
        val dbFile = target.getDatabasePath(StudyDatabase.DB_NAME)
        try {
            PortableStudyBackup.restoreFromUri(target, uri, PortableStudyBackup.generateRecoveryKey())
            fail("Invalid document must be rejected")
        } catch (_: Exception) {
            assertFalse("Invalid input must not create a restore database", dbFile.exists())
        } finally {
            cleanStudyStore()
            testContext.contentResolver.delete(uri, null, null)
        }
    }

    @Test fun cancellationConsumesPendingExportAndRestoreKeysWithoutPersistingThem() {
        val state = PendingDocumentPickerState()
        state.storeExportKey("synthetic-export-key")
        // The Activity Result callback consumes the key even when the returned URI is null.
        assertEquals("synthetic-export-key", state.consumeExportKey())
        assertNull(state.consumeExportKey())

        state.storeRestoreKey("synthetic-restore-key")
        // The OpenDocument cancellation callback likewise consumes and clears its pending key.
        assertEquals("synthetic-restore-key", state.consumeRestoreKey())
        assertNull(state.consumeRestoreKey())
    }
}
