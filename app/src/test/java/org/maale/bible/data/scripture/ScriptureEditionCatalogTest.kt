package org.maale.bible.data.scripture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScriptureEditionCatalogTest {
    @Test
    fun catalogueContainsMaalePrimaryFourAmharicAndAtLeastFortyTwoEnglishCandidates() {
        assertNotNull(ScriptureEditionCatalog.find(ScriptureEditionCatalog.PRIMARY_MAALE_ID))
        assertEquals(4, ScriptureEditionCatalog.amharicEditions.size)
        assertTrue(ScriptureEditionCatalog.englishEditions.size >= 42)
    }

    @Test
    fun everyEditionHasUniqueIdAndExplicitLanguageAndRightsStatus() {
        assertEquals(
            ScriptureEditionCatalog.editions.size,
            ScriptureEditionCatalog.editions.map { it.id }.toSet().size
        )
        ScriptureEditionCatalog.editions.forEach { edition ->
            assertTrue(edition.id.isNotBlank())
            assertTrue(edition.languageTag in setOf("mdy", "am", "en"))
            assertNotNull(edition.status)
            assertTrue(edition.notes.isNotBlank())
        }
    }

    @Test
    fun catalogueCandidatesAreNotMarkedAsReleaseReady() {
        ScriptureEditionCatalog.editions
            .filter { it.id != ScriptureEditionCatalog.PRIMARY_MAALE_ID }
            .forEach { edition ->
                assertTrue(edition.status != ScriptureEditionStatus.PRIMARY_SOURCE)
            }
    }
}
