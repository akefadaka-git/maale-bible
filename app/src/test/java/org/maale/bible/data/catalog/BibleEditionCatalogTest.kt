package org.maale.bible.data.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BibleEditionCatalogTest {
    @Test
    fun exposesOpenEnglishSourcesWithEditionSpecificLicenses() {
        val english = BibleEditionCatalog.eligibleForPackage("en")
        assertTrue(english.any { it.id == "eng-asv" && it.license == "Public domain" })
        assertTrue(english.any { it.id == "eng-fbv" && it.license == "CC BY-SA 4.0" })
        assertTrue(english.any { it.id == "eng-oeb" && !it.fullBible })
    }

    @Test
    fun doesNotMarkAmharicDraftOrPermissionLimitedTextAsOpenReady() {
        val amharic = BibleEditionCatalog.eligibleForPackage("am")
        assertTrue(amharic.isEmpty())
        val pending = BibleEditionCatalog.awaitingReview("am")
        assertTrue(pending.any { it.id == "amh-open-word" })
        assertTrue(pending.any { it.id == "amh-ubs-2003-nt" })
        assertFalse(pending.any { it.readiness == EditionReadiness.READY_FOR_PACKAGE_INTEGRATION })
    }

    @Test
    fun languageFilterDoesNotLeakOtherEditions() {
        assertTrue(BibleEditionCatalog.eligibleForPackage("am").all { it.languageCode == "am" })
        assertTrue(BibleEditionCatalog.eligibleForPackage("en").all { it.languageCode == "en" })
        assertEquals(0, BibleEditionCatalog.eligibleForPackage("xx").size)
    }
}
