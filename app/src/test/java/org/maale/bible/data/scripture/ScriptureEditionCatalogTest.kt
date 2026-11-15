package org.maale.bible.data.scripture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScriptureEditionCatalogTest {
    @Test
    fun catalogueContainsMaalePrimaryFourAmharicAndAtLeastFortyTwoFullBibleCandidates() {
        assertNotNull(ScriptureEditionCatalog.find(ScriptureEditionCatalog.PRIMARY_MAALE_ID))
        assertEquals(4, ScriptureEditionCatalog.amharicEditions.size)
        assertTrue(ScriptureEditionCatalog.englishEditions.count { it.fullBible } >= 42)
        assertTrue(ScriptureEditionCatalog.englishEditions.all { it.status == ScriptureEditionStatus.CATALOG_ONLY_RIGHTS_UNVERIFIED })
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
