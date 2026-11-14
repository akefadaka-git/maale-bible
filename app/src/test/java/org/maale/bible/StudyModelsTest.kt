package org.maale.bible

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.maale.bible.domain.model.*

class StudyModelsTest {
    @Test fun navigationMovesWithoutInventingChapterBounds() {
        val start = ReaderNavigation("GEN")
        assertEquals(ReaderNavigation("GEN",1,2), start.nextVerse())
        assertEquals(start, start.previousVerse())
        assertEquals(ReaderNavigation("GEN",2,1), start.nextChapter())
        assertEquals(start, start.previousChapter())
    }
    @Test fun chapterNavigationResetsVerse() {
        assertEquals(ReaderNavigation("MAT",3,1), ReaderNavigation("MAT",2,14).nextChapter())
        assertEquals(ReaderNavigation("MAT",1,1), ReaderNavigation("MAT",2,14).previousChapter())
    }
    @Test fun planRejectsProgressOutsideSchedule() {
        try { ReadingPlan("p","Plan",1,7,setOf(8),1); fail("expected validation") } catch (_: IllegalArgumentException) {}
    }
    @Test fun annotationRejectsInvalidReference() {
        try { StudyAnnotation("n","GEN",0,1,AnnotationKind.NOTE,"x",null,1,1); fail("expected validation") } catch (_: IllegalArgumentException) {}
    }
}