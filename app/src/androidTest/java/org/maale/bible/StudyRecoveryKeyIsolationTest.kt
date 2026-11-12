package org.maale.bible

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.maale.bible.data.repository.StudyDatabase
import org.maale.bible.data.security.PortableStudyBackup
import org.maale.bible.domain.model.Bookmark

@RunWith(AndroidJUnit4::class)
class StudyRecoveryKeyIsolationTest {
    @Test fun wrongRecoveryKeyCannotReplaceAnExistingDestinationDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(StudyDatabase.DB_NAME)
        val source = StudyDatabase(context)
        source.addBookmark(Bookmark("source", "GEN", 1, 1, 10L, "encrypted source"))
        val recoveryKey = PortableStudyBackup.generateRecoveryKey()
        val envelope = PortableStudyBackup.export(context, source, recoveryKey)
        source.close()
        context.deleteDatabase(StudyDatabase.DB_NAME)

        val destination = StudyDatabase(context)
        destination.addBookmark(Bookmark("destination", "EXO", 2, 3, 20L, "must remain"))
        destination.close()

        val failure = runCatching {
            PortableStudyBackup.restore(context, envelope, PortableStudyBackup.generateRecoveryKey())
        }.exceptionOrNull()
        assertNotNull("Wrong recovery key must be rejected", failure)

        val reopened = StudyDatabase(context)
        try {
            assertEquals(listOf("destination"), reopened.getBookmarks("EXO").map { it.id })
            assertEquals(emptyList<String>(), reopened.getBookmarks("GEN").map { it.id })
        } finally {
            reopened.close()
            context.deleteDatabase(StudyDatabase.DB_NAME)
        }
    }
}
