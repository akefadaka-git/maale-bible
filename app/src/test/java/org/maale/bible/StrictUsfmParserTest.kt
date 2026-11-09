package org.maale.bible

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.maale.bible.data.source.StrictUsfmParseException
import org.maale.bible.data.source.StrictUsfmParser

class StrictUsfmParserTest {
    @Test fun preservesBookChapterVerseSegmentsRangesAndHeadings() {
        val source = """\id GEN Genesis
\h Genesis
\c 1
\s1 Heading retained
\p
\v 1 First verse \w word\w*
\v 1a Segment A
\v 1b Segment B
\v 2-3 Range
\c 2
\p
\v 1 Next chapter
"""
        val parsed = StrictUsfmParser.parse(source, "GEN")
        assertEquals("GEN", parsed.bookId)
        assertEquals(2, parsed.chapters)
        assertEquals(5, parsed.verseMarkers)
        assertTrue(parsed.records.any { it.marker == "s1" && it.payload == "Heading retained\n" })
        val refs = parsed.records.mapNotNull { it.verse }
        assertEquals(listOf("1", "1a", "1b", "2-3", "1"), refs.map { it.sourceVerse })
        assertEquals(1, refs[1].startVerse)
        assertEquals("a", refs[1].startSegment)
        assertEquals(3, refs[3].endVerse)
        assertNull(refs[3].endSegment)
        assertEquals(2, refs.last().chapter)
    }

    @Test fun rejectsWrongBookIdentity() {
        val source = "\\id EXO Exodus\n\\c 1\n\\v 1 Text"
        expectFailure { StrictUsfmParser.parse(source, "GEN") }
    }

    @Test fun rejectsNonContiguousChapters() {
        val source = "\\id GEN Genesis\n\\c 1\n\\v 1 Text\n\\c 3\n\\v 1 Text"
        expectFailure { StrictUsfmParser.parse(source, "GEN") }
    }

    @Test fun rejectsDuplicateOrOverlappingVerseCoordinates() {
        val source = "\\id GEN Genesis\n\\c 1\n\\v 1 Text\n\\v 1 More text"
        expectFailure { StrictUsfmParser.parse(source, "GEN") }
    }

    @Test fun rejectsMalformedVerseRangesAndMissingChapter() {
        expectFailure {
            StrictUsfmParser.parse("\\id GEN Genesis\n\\c 1\n\\v 1- Text", "GEN")
        }
        expectFailure {
            StrictUsfmParser.parse("\\id GEN Genesis\n\\v 1 Text", "GEN")
        }
    }

    @Test fun rejectsEmptyBookAndMalformedTrailingEscape() {
        expectFailure { StrictUsfmParser.parse("", "GEN") }
        expectFailure {
            StrictUsfmParser.parse("\\id GEN Genesis\n\\c 1\n\\v 1 Text\\", "GEN")
        }
    }

    private fun expectFailure(block: () -> Unit) {
        var failed = false
        try {
            block()
        } catch (_: StrictUsfmParseException) {
            failed = true
        } catch (_: IllegalArgumentException) {
            failed = true
        }
        assertTrue("Expected strict parser rejection", failed)
    }
}
