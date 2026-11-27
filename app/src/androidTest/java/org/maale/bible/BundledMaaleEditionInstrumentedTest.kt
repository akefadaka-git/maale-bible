package org.maale.bible

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.maale.bible.data.content.OfflineScriptureStore

class BundledMaaleEditionInstrumentedTest {
    @Test
    fun verifiedMaaleEditionInstallsAndServesCanonicalGenesisChapter() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        File(context.filesDir, "offline-content").deleteRecursively()
        assertTrue(OfflineScriptureStore.hasBundledEdition(context, "mdyeth"))
        val installed = OfflineScriptureStore.installBundledEdition(context, "mdyeth")
        assertEquals("mdyeth", installed.editionId)
        assertEquals("4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11", installed.sourceArchiveSha256)
        val store = requireNotNull(OfflineScriptureStore.openInstalled(context, "mdyeth"))
        try {
            assertEquals(66, installed.bookCount)
            assertTrue(store.chapterCount("GEN") > 0)
            val chapter = store.chapter("GEN", 1)
            assertTrue(chapter.isNotEmpty())
            assertTrue(chapter.first().text.isNotBlank())
            assertEquals(chapter.first(), store.passage("GEN", 1, chapter.first().verseRef))
        } finally {
            store.close()
            File(context.filesDir, "offline-content").deleteRecursively()
        }
    }
}
