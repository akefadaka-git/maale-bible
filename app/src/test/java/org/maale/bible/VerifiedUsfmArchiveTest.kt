package org.maale.bible

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.maale.bible.data.source.ExpectedUsfmFile
import org.maale.bible.data.source.SourceArchiveExpectation
import org.maale.bible.data.source.VerifiedUsfmArchive
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class VerifiedUsfmArchiveTest {
    @Test fun verifiesArchiveAndAllSixtySixSyntheticFilesBeforeReturning() {
        val entries = syntheticFiles()
        val archive = zip(entries)
        val expectation = expectation(entries, archive)
        val verified = VerifiedUsfmArchive.verify(ByteArrayInputStream(archive), expectation)
        assertEquals(66, verified.files.size)
        assertEquals((1..66).toList(), verified.files.map { it.canonicalOrder })
        assertEquals("B01", verified.files.first().bookId)
        assertEquals("synthetic-usfm-66", verified.files.last().bytes.decodeToString())
    }

    @Test fun rejectsUnsafeZipEntryPathsWithoutExtracting() {
        val entries = syntheticFiles() + ( "../escape.txt" to "nope".encodeToByteArray() )
        val archive = zip(entries)
        assertThrows(IllegalArgumentException::class.java) {
            VerifiedUsfmArchive.verify(
                ByteArrayInputStream(archive),
                expectation(syntheticFiles(), archive)
            )
        }
    }

    @Test fun rejectsArchiveDigestMismatch() {
        val entries = syntheticFiles()
        val archive = zip(entries)
        val good = expectation(entries, archive)
        val bad = good.copy(archiveSha256 = "0".repeat(64))
        assertThrows(IllegalArgumentException::class.java) {
            VerifiedUsfmArchive.verify(ByteArrayInputStream(archive), bad)
        }
    }

    @Test fun rejectsMissingExpectedBook() {
        val entries = syntheticFiles().dropLast(1)
        val archive = zip(entries)
        assertThrows(IllegalArgumentException::class.java) {
            VerifiedUsfmArchive.verify(
                ByteArrayInputStream(archive),
                expectation(syntheticFiles(), archive)
            )
        }
    }

    private fun syntheticFiles(): List<Pair<String, ByteArray>> =
        (1..66).map { order ->
            val id = "B" + order.toString().padStart(2, '0')
            val path = order.toString().padStart(2, '0') + "-$id.usfm"
            path to "synthetic-usfm-$order".encodeToByteArray()
        }

    private fun expectation(
        entries: List<Pair<String, ByteArray>>,
        archive: ByteArray
    ): SourceArchiveExpectation {
        val expected = entries.filter { it.first.endsWith(".usfm") }.mapIndexed { index, item ->
            ExpectedUsfmFile(
                index + 1,
                "B" + (index + 1).toString().padStart(2, '0'),
                item.first,
                item.second.size.toLong(),
                sha256(item.second)
            )
        }
        return SourceArchiveExpectation(archive.size.toLong(), sha256(archive), expected)
    }

    private fun zip(entries: List<Pair<String, ByteArray>>): ByteArray {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            entries.forEach { (name, bytes) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
        return output.toByteArray()
    }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}
