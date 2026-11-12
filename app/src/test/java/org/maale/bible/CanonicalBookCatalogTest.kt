package org.maale.bible

import org.junit.Assert.*
import org.junit.Test
import org.maale.bible.data.catalog.CanonicalBookCatalog

class CanonicalBookCatalogTest {
    @Test fun containsExactlySixtySixUniqueBooks() {
        val books = CanonicalBookCatalog.books
        assertEquals(66, books.size)
        assertEquals(66, books.map { it.usfmId }.toSet().size)
    }

    @Test fun bookOrderIsContiguousAndStartsAtOne() {
        assertEquals((1..66).toList(), CanonicalBookCatalog.books.map { it.order })
    }

    @Test fun knownBooksResolveAndUnknownBooksDoNot() {
        assertNotNull(CanonicalBookCatalog.find("GEN"))
        assertNotNull(CanonicalBookCatalog.find("REV"))
        assertNull(CanonicalBookCatalog.find("XYZ"))
    }

    @Test fun blankSearchReturnsTheEntireCanonicalCatalogue() {
        assertEquals(CanonicalBookCatalog.books, CanonicalBookCatalog.search("  "))
    }

    @Test fun searchMatchesEnglishNamesCaseInsensitively() {
        val results = CanonicalBookCatalog.search("jOhN")
        assertEquals(listOf("JHN", "1JN", "2JN", "3JN"), results.map { it.usfmId })
    }

    @Test fun searchMatchesUsfmCodesCaseInsensitivelyAndTrimsInput() {
        assertEquals(listOf("GEN"), CanonicalBookCatalog.search(" gen ").map { it.usfmId })
        assertEquals(listOf("REV"), CanonicalBookCatalog.search("rev").map { it.usfmId })
    }

    @Test fun searchPreservesCanonicalOrderForPartialMatches() {
        assertEquals(listOf("1SA", "2SA"), CanonicalBookCatalog.search("sam").map { it.usfmId })
    }

    @Test fun unknownSearchReturnsNoResults() {
        assertTrue(CanonicalBookCatalog.search("not-a-bible-book").isEmpty())
    }
}
