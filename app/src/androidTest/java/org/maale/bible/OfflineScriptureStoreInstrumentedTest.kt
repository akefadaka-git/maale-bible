package org.maale.bible

import android.content.Context
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.maale.bible.data.catalog.CanonicalBookCatalog
import org.maale.bible.data.content.OfflineContentDescriptor
import org.maale.bible.data.content.OfflineScriptureStore
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

@RunWith(AndroidJUnit4::class)
class OfflineScriptureStoreInstrumentedTest {
    private val sourceSha = "4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11"

    @Test
    fun installsVerifiesAndQueriesPrivateContentDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        clearInstalled(context)
        val (source, descriptor) = createSyntheticDatabase(context)

        OfflineScriptureStore.install(context, FileInputStream(source), descriptor)
        val store = requireNotNull(OfflineScriptureStore.openInstalled(context))
        try {
            val results = store.search("SYNTHETIC_ALPHA")
            assertEquals(1, results.size)
            assertEquals("GEN", results.single().bookId)
            assertEquals("1", results.single().verseRef)
            assertEquals("SYNTHETIC_ALPHA_BETA", results.single().text)
            assertEquals("SYNTHETIC_ALPHA_BETA", store.passage("GEN", 1, "1")?.text)
            assertTrue(store.search("   ").isEmpty())
            assertEquals(66, store.descriptor.bookCount)
        } finally {
            store.close()
            source.delete()
            clearInstalled(context)
        }
    }

    @Test
    fun installsAndKeepsMultipleEditionsIsolated() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        clearInstalled(context)
        val (firstFile, firstDescriptor) = createSyntheticDatabase(context, "Maale test edition")
        OfflineScriptureStore.install(context, FileInputStream(firstFile), firstDescriptor)
        val (secondFile, secondDescriptor) = createSyntheticDatabase(context, "English test edition")
        OfflineScriptureStore.install(context, FileInputStream(secondFile), secondDescriptor)

        val installed = OfflineScriptureStore.installedEditions(context).map { it.editionId }.toSet()
        assertTrue(installed.contains("Maale test edition"))
        assertTrue(installed.contains("English test edition"))

        val maale = requireNotNull(OfflineScriptureStore.openInstalled(context, "Maale test edition"))
        val english = requireNotNull(OfflineScriptureStore.openInstalled(context, "English test edition"))
        try {
            assertEquals("Maale test edition", maale.descriptor.editionId)
            assertEquals("English test edition", english.descriptor.editionId)
            assertEquals(1, maale.search("SYNTHETIC_ALPHA").size)
            assertEquals(1, english.search("SYNTHETIC_ALPHA").size)
        } finally {
            maale.close()
            english.close()
            firstFile.delete()
            secondFile.delete()
            clearInstalled(context)
        }
    }

    @Test
    fun failedHashValidationPreservesPreviouslyInstalledDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        clearInstalled(context)
        val (source, descriptor) = createSyntheticDatabase(context)
        OfflineScriptureStore.install(context, FileInputStream(source), descriptor)

        val invalid = descriptor.copy(databaseSha256 = "0".repeat(64))
        try {
            OfflineScriptureStore.install(context, FileInputStream(source), invalid)
            fail("Expected package hash validation to reject the import")
        } catch (_: IllegalArgumentException) {
            // Expected: the active directory must not be replaced.
        }

        val store = requireNotNull(OfflineScriptureStore.openInstalled(context))
        try {
            assertEquals(1, store.search("SYNTHETIC_ALPHA").size)
        } finally {
            store.close()
            source.delete()
            clearInstalled(context)
        }
    }

    @Test
    fun failedDatabaseMetadataValidationPreservesPreviouslyInstalledDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        clearInstalled(context)
        val (source, descriptor) = createSyntheticDatabase(context)
        OfflineScriptureStore.install(context, FileInputStream(source), descriptor)

        val invalid = descriptor.copy(sourceArchiveSha256 = "1".repeat(64))
        try {
            OfflineScriptureStore.install(context, FileInputStream(source), invalid)
            fail("Expected source metadata mismatch to reject the import")
        } catch (_: IllegalArgumentException) {
            // Expected: failed validation must leave the prior content active.
        }

        val store = requireNotNull(OfflineScriptureStore.openInstalled(context))
        try {
            assertEquals(1, store.search("SYNTHETIC_ALPHA").size)
        } finally {
            store.close()
            source.delete()
            clearInstalled(context)
        }
    }

    private fun createSyntheticDatabase(context: Context, editionId: String = "MDYTWF / mdyeth"): Pair<File, OfflineContentDescriptor> {
        val file = context.getDatabasePath("offline-content-source-fixture.db")
        file.parentFile?.mkdirs()
        file.delete()
        val db = BundledSQLiteDriver().open(file.absolutePath)
        try {
            db.execSQL("PRAGMA foreign_keys = ON")
            db.execSQL("CREATE TABLE metadata(key TEXT PRIMARY KEY, value TEXT NOT NULL)")
            db.execSQL("CREATE TABLE books(book_order INTEGER PRIMARY KEY, book_id TEXT NOT NULL UNIQUE, canonical_name TEXT NOT NULL)")
            db.execSQL("""CREATE TABLE verses(
                id INTEGER PRIMARY KEY,
                edition_id TEXT NOT NULL,
                book_id TEXT NOT NULL REFERENCES books(book_id),
                chapter INTEGER NOT NULL,
                verse_ref TEXT NOT NULL,
                source_path TEXT NOT NULL,
                source_sha256 TEXT NOT NULL,
                parser_version TEXT NOT NULL,
                display_text TEXT NOT NULL,
                search_text TEXT NOT NULL,
                UNIQUE(edition_id,book_id,chapter,verse_ref)
            )""")
            db.execSQL("CREATE VIRTUAL TABLE verse_fts USING fts5(search_text, content='verses', content_rowid='id', tokenize='unicode61 remove_diacritics 0')")

            val metadata = mapOf(
                "schema_version" to "1",
                "parser_version" to "0.1.0",
                "edition_id" to editionId,
                "source_archive_sha256" to sourceSha,
                "fts_tokenizer" to "unicode61 remove_diacritics 0",
                "tokenizer_qualification" to "ENGINEERING_BASELINE_NOT_LINGUISTICALLY_QUALIFIED",
            )
            db.prepare("INSERT INTO metadata(key,value) VALUES (?,?)").use { statement ->
                metadata.forEach { (key, value) ->
                    statement.bindText(1, key)
                    statement.bindText(2, value)
                    statement.step()
                    statement.reset()
                }
            }
            db.prepare("INSERT INTO books(book_order,book_id,canonical_name) VALUES (?,?,?)").use { statement ->
                CanonicalBookCatalog.books.forEach { book ->
                    statement.bindLong(1, book.order.toLong())
                    statement.bindText(2, book.usfmId)
                    statement.bindText(3, book.englishName)
                    statement.step()
                    statement.reset()
                }
            }
            db.prepare("""INSERT INTO verses(
                edition_id,book_id,chapter,verse_ref,source_path,source_sha256,
                parser_version,display_text,search_text
            ) VALUES (?,?,?,?,?,?,?,?,?)""").use { statement ->
                statement.bindText(1, editionId)
                statement.bindText(2, "GEN")
                statement.bindLong(3, 1)
                statement.bindText(4, "1")
                statement.bindText(5, "synthetic.usfm")
                statement.bindText(6, "a".repeat(64))
                statement.bindText(7, "0.1.0")
                statement.bindText(8, "SYNTHETIC_ALPHA_BETA")
                statement.bindText(9, "synthetic_alpha_beta")
                statement.step()
            }
            db.execSQL("INSERT INTO verse_fts(verse_fts) VALUES ('rebuild')")
            db.execSQL("PRAGMA user_version = 1")
        } finally {
            db.close()
        }
        val sha = sha256(file)
        return file to OfflineContentDescriptor(
            editionId = editionId,
            sourceArchiveSha256 = sourceSha,
            databaseSha256 = sha,
            databaseBytes = file.length(),
            schemaVersion = "1",
            parserVersion = "0.1.0",
            bookCount = 66,
            chapterCount = 1,
            verseCount = 1,
            ftsCount = 1,
            ftsTokenizer = "unicode61 remove_diacritics 0",
            tokenizerQualification = "ENGINEERING_BASELINE_NOT_LINGUISTICALLY_QUALIFIED",
        )
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun clearInstalled(context: Context) {
        File(context.filesDir, "offline-content").deleteRecursively()
    }
}
