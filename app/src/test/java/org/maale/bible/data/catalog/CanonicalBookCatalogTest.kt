package org.maale.bible.data.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CanonicalBookCatalogTest {
    @Test
    fun catalogueContainsExpectedOldAndNewTestamentCounts() {
        assertEquals(66, CanonicalBookCatalog.books.size)
        assertEquals(39, CanonicalBookCatalog.books.count { it.order in 1..39 })
        assertEquals(27, CanonicalBookCatalog.books.count { it.order in 40..66 })
    }

    @Test
    fun canonicalBookLookupResolvesBothTestaments() {
        assertNotNull(CanonicalBookCatalog.find("GEN"))
        assertNotNull(CanonicalBookCatalog.find("REV"))
        assertEquals(1, CanonicalBookCatalog.find("GEN")?.order)
        assertEquals(66, CanonicalBookCatalog.find("REV")?.order)
    }
}
