package org.maale.bible.data.scripture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScriptureEditionCrossVersionTest {
    private fun chapter(edition: String, book: String = "GEN", number: Int = 1, verses: List<ScriptureVerse>) =
        ScriptureChapter(edition, book, number, emptyList(), verses)

    @Test
    fun comparisonAlignsOnlySharedVerseLabelsWithinSameReference() {
        val first = chapter("EDITION-A", verses = listOf(
            ScriptureVerse("1", "In the beginning"),
            ScriptureVerse("2", "The earth was formless")
        ))
        val second = chapter("EDITION-B", verses = listOf(
            ScriptureVerse("1", "At the beginning"),
            ScriptureVerse("3", "A different verse")
        ))

        val result = compareScriptureChapters(first, second)

        assertEquals(listOf("1"), result.map { it.label })
        assertEquals("In the beginning", result.single().firstText)
        assertEquals("At the beginning", result.single().secondText)
    }

    @Test
    fun comparisonRejectsSameEditionAndDifferentReferences() {
        val first = chapter("EDITION-A", verses = listOf(ScriptureVerse("1", "One")))
        assertTrue(compareScriptureChapters(first, first.copy(editionId = "EDITION-A")).isEmpty())
        assertTrue(compareScriptureChapters(first, first.copy(editionId = "EDITION-B", bookId = "EXO")).isEmpty())
        assertTrue(compareScriptureChapters(first, first.copy(editionId = "EDITION-B", chapterNumber = 2)).isEmpty())
    }

    @Test
    fun searchReturnsEditionAndReferenceForEachMatchingPackage() {
        val maale = chapter("MAALE", verses = listOf(ScriptureVerse("1", "Light was created")))
        val english = chapter("ENGLISH", verses = listOf(ScriptureVerse("1", "Let there be light")))

        val results = searchScriptureChapters(listOf(maale, english), "light")

        assertEquals(setOf("MAALE", "ENGLISH"), results.map { it.editionId }.toSet())
        assertEquals(setOf("1"), results.map { it.verseLabel }.toSet())
        assertTrue(searchScriptureChapters(listOf(maale), "  ").isEmpty())
    }

    @Test
    fun sourceRegistryCoversEveryCatalogueEntryAndBlocksUnlicensedCandidates() {
        assertEquals(
            ScriptureEditionCatalog.editions.map { it.id }.toSet(),
            ScriptureEditionSourceRegistry.records.map { it.editionId }.toSet()
        )
        assertEquals(
            ScriptureEditionSourceRegistry.records.size,
            ScriptureEditionSourceRegistry.records.map { it.editionId }.toSet().size
        )
        assertFalse(ScriptureEditionSourceRegistry.canImport("AMH-HAILE-SELASSIE-1962"))
        assertFalse(ScriptureEditionSourceRegistry.canImport("ENG-NIV"))
        assertFalse(ScriptureEditionSourceRegistry.records.any { it.releaseEligible })
    }

    @Test
    fun openEnglishBibleIsNotCountedAsCompleteBible() {
        assertFalse(ScriptureEditionCatalog.find("ENG-OEB")!!.fullBible)
        assertFalse(ScriptureEditionCatalog.find("ENG-MOUNCE")!!.fullBible)
        assertFalse(ScriptureEditionCatalog.find("ENG-WEY")!!.fullBible)
    }

    @Test
    fun genericPackageFilenameIsDerivedFromEditionRegistryWhenAuthorized() {
        val maale = ScriptureEditionSourceRegistry.find(ScriptureEditionCatalog.PRIMARY_MAALE_ID)!!
        assertEquals("maale-mdytwf-2008.sqlite", expectedScripturePackageFileName(maale))
        assertNull(expectedScripturePackageFileName(ScriptureEditionSourceRegistry.find("ENG-NIV")!!))
    }
}
