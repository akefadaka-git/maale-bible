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
}
