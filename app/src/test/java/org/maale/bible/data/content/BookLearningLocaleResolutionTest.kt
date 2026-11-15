package org.maale.bible.data.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BookLearningLocaleResolutionTest {
    @Test fun englishUsesEnglishWithoutFallbackNotice() {
        assertEquals(BookLearningLocaleResolution("en", false), resolveBookLearningLocale("en"))
    }

    @Test fun amharicUsesAmharicWhenAvailable() {
        assertEquals(BookLearningLocaleResolution("am", false), resolveBookLearningLocale("am", hasAmharic = true))
    }

    @Test fun maaleUsesEnglishButRequiresVisibleFallbackNotice() {
        val result = resolveBookLearningLocale("mdy")
        assertEquals("en", result.contentLocale)
        assertTrue(result.requiresFallbackNotice)
    }

    @Test fun missingAmharicPackageUsesEnglishWithNotice() {
        val result = resolveBookLearningLocale("am", hasAmharic = false)
        assertEquals("en", result.contentLocale)
        assertTrue(result.requiresFallbackNotice)
    }

    @Test fun unsupportedLocalesUseEnglishWithNotice() {
        val result = resolveBookLearningLocale("fr")
        assertEquals("en", result.contentLocale)
        assertTrue(result.requiresFallbackNotice)
    }
}
