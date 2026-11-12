package org.maale.bible

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.maale.bible.data.repository.StudyDatabase
import org.maale.bible.domain.model.Bookmark

@RunWith(AndroidJUnit4::class)
class StudyDatabaseRestoreRollbackTest {
    @Test fun duplicatePrimaryKeysRollbackAllRestoreMutations() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(StudyDatabase.DB_NAME)
        val db = StudyDatabase(context)
        db.addBookmark(Bookmark("preserve-me", "EXO", 1, 1, 10L, "pre-restore"))

        val duplicateBackup = """{"format":"maale-bible-study-backup","version":1,"bookmarks":[
          {"id":"duplicate","bookId":"GEN","chapter":1,"verse":1,"createdAt":20,"label":"first"},
          {"id":"duplicate","bookId":"GEN","chapter":1,"verse":2,"createdAt":21,"label":"second"}
        ],"annotations":[],"plans":[]}"""

        val failure = runCatching { db.restoreBackup(duplicateBackup) }.exceptionOrNull()
        assertNotNull("Duplicate primary key must fail restore", failure)
        assertEquals(listOf("preserve-me"), db.getBookmarks("EXO").map { it.id })
        assertEquals(emptyList<String>(), db.getBookmarks("GEN").map { it.id })
        db.close()
        context.deleteDatabase(StudyDatabase.DB_NAME)
    }
}
