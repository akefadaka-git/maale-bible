package org.maale.bible

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.maale.bible.data.backup.EncryptedBackup
import java.nio.charset.StandardCharsets

@RunWith(AndroidJUnit4::class)
// Synthetic-only security regression suite for the versioned portable backup envelope.
class P2V5EncryptedBackupTest {
    private val password = "synthetic-test-passphrase-2026".toCharArray()

    @Test fun roundTripAuthenticatesAndPreservesSyntheticEntries() {
        val input = mapOf(
            "notes/one.json" to """{"note":"synthetic only"}""".toByteArray(),
            "preferences/settings.json" to """{"fontScale":1.1}""".toByteArray()
        )
        val encrypted = EncryptedBackup.create(input, password)
        assertFalse(String(encrypted, StandardCharsets.ISO_8859_1).contains("synthetic only"))
        val restored = EncryptedBackup.restore(encrypted, password)
        assertEquals(input.keys, restored.keys)
        input.forEach { (path, bytes) -> assertArrayEquals(bytes, restored[path]) }
    }

    @Test fun wrongPasswordFailsAuthentication() {
        val encrypted = EncryptedBackup.create(mapOf("notes/a.json" to byteArrayOf(1, 2, 3)), password)
        try {
            EncryptedBackup.restore(encrypted, "wrong-passphrase-value".toCharArray())
            fail("wrong password must not restore")
        } catch (_: SecurityException) { }
    }

    @Test fun ciphertextTamperingFailsAuthentication() {
        val encrypted = EncryptedBackup.create(mapOf("notes/a.json" to "synthetic".toByteArray()), password)
        val changed = encrypted.copyOf().also { it[it.lastIndex] = (it.last().toInt() xor 1).toByte() }
        try {
            EncryptedBackup.restore(changed, password)
            fail("tampered ciphertext must not restore")
        } catch (_: SecurityException) { }
    }

    @Test fun unsupportedVersionAndKdfAndUnsafePathsAreRejected() {
        val encrypted = EncryptedBackup.create(mapOf("notes/a.json" to byteArrayOf(1)), password)
        val unsupportedVersion = encrypted.copyOf().also { it[4] = 99 }
        try {
            EncryptedBackup.restore(unsupportedVersion, password)
            fail("unsupported format must be rejected")
        } catch (_: IllegalArgumentException) { }
        val unsupportedKdf = encrypted.copyOf().also { it[5] = 99 }
        try {
            EncryptedBackup.restore(unsupportedKdf, password)
            fail("unsupported KDF must be rejected")
        } catch (_: IllegalArgumentException) { }
        try {
            EncryptedBackup.create(mapOf("../escape" to byteArrayOf(1)), password)
            fail("path traversal must be rejected")
        } catch (_: IllegalArgumentException) { }
    }

    @Test fun truncatedCiphertextFailsAuthentication() {
        val encrypted = EncryptedBackup.create(mapOf("notes/a.json" to byteArrayOf(1, 2, 3)), password)
        val truncated = encrypted.copyOfRange(0, encrypted.size - 1)
        try {
            EncryptedBackup.restore(truncated, password)
            fail("truncated ciphertext must not restore")
        } catch (_: SecurityException) { }
    }
}
