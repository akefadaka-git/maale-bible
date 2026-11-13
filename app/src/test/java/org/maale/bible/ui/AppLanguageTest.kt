package org.maale.bible.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {
    @Test fun languageTagsAreStableAndMaaleIsTheDefaultChoice() {
        assertEquals("mdy", AppLanguage.MAALE.tag)
        assertEquals("am", AppLanguage.AMHARIC.tag)
        assertEquals("en", AppLanguage.ENGLISH.tag)
        assertEquals(AppLanguage.MAALE, AppLanguage.entries.first())
    }
}
