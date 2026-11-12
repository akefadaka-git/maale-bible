package org.maale.bible

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.maale.bible.data.repository.StudyDatabase
import org.maale.bible.domain.model.*

@RunWith(AndroidJUnit4::class)
class StudyDatabaseInstrumentedTest {
    @Test fun userDataSurvivesDatabaseCloseAndReopen() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(StudyDatabase.DB_NAME)
        val first=StudyDatabase(context)
        first.saveReadingPosition(ReadingPosition("GEN",2,3,100))
        first.addBookmark(Bookmark("bookmark-1","GEN",2,3,101,"test"))
        first.saveAnnotation(StudyAnnotation("note-1","GEN",2,3,AnnotationKind.NOTE,"synthetic note",null,102,102))
        first.savePlan(ReadingPlan("plan-1","Synthetic plan",1,7,setOf(1,2),103))
        first.close()

        val reopened=StudyDatabase(context)
        assertEquals(ReadingPosition("GEN",2,3,100),reopened.getReadingPosition())
        assertEquals(1,reopened.getBookmarks("GEN").size)
        assertEquals("synthetic note",reopened.getAnnotations("GEN").single().body)
        assertEquals(setOf(1,2),reopened.getPlans().single().completedDays)
        reopened.close()
        context.deleteDatabase(StudyDatabase.DB_NAME)
    }

    @Test fun backupRestoreRoundTripAndRejectInvalidPayloadWithoutErasing() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(StudyDatabase.DB_NAME)
        val db=StudyDatabase(context)
        db.addBookmark(Bookmark("saved","EXO",1,1,1))
        val backup=db.exportBackup()
        db.addBookmark(Bookmark("later","EXO",1,2,2))
        db.restoreBackup(backup)
        assertEquals(listOf("saved"),db.getBookmarks("EXO").map { it.id })
        try { db.restoreBackup("{invalid"); throw AssertionError("invalid backup accepted") } catch (_: org.json.JSONException) {}
        assertNotNull(db.getBookmarks("EXO").singleOrNull())
        db.close()
        context.deleteDatabase(StudyDatabase.DB_NAME)
    }
}
