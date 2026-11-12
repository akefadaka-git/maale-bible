package org.maale.bible

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.text.Normalizer

@RunWith(AndroidJUnit4::class)
class P2V4UnicodeContractTest {
    @Test fun syntheticEthiopicAndMixedScriptTextIsPreserved() {
        val fixtures = listOf(
            "ሰላም — synthetic Ethiopic fixture 123",
            "ቤተ ክርስቲያን፣ ፡።፤፥",
            "Maale / Amharic / English የተቀላቀለ",
            "ሀለሐመሠረሰሸቀበተ"
        )
        fixtures.forEach { source ->
            val original = source.toByteArray(Charsets.UTF_8)
            val normalized = Normalizer.normalize(source, Normalizer.Form.NFC)
            assertEquals(normalized, Normalizer.normalize(normalized, Normalizer.Form.NFC))
            assertArrayEquals(original, source.toByteArray(Charsets.UTF_8))
            assertEquals(source, original.toString(Charsets.UTF_8))
        }
        assertTrue(fixtures[1].contains("፡"))
        assertTrue(fixtures[1].contains("።"))
    }

    @Test fun canonicallyEquivalentSyntheticLatinFormsNormalizeEqually() {
        val decomposed = "e\u0301"
        val composed = "é"
        assertEquals(
            Normalizer.normalize(decomposed, Normalizer.Form.NFC),
            Normalizer.normalize(composed, Normalizer.Form.NFC)
        )
    }
}
