package org.maale.bible

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.maale.bible.data.security.StudyBackupCipher

@RunWith(AndroidJUnit4::class)
class StudyBackupCipherInstrumentedTest {
    @Test fun encryptedBackupRoundTripsWithoutExposingPlaintext() {
        val plaintext = """{"format":"maale-bible-study-backup","version":1,"privateNote":"personal"}"""
        val cipher = StudyBackupCipher()
        val encrypted = cipher.encrypt(plaintext)
        assertFalse(encrypted.contains("privateNote"))
        assertEquals(plaintext, cipher.decrypt(encrypted))
    }

    @Test fun tamperingIsRejected() {
        val cipher = StudyBackupCipher()
        val encrypted = cipher.encrypt("sensitive study data")
        val payload = encrypted.dropLast(2) + "AA"
        try {
            cipher.decrypt(payload)
            throw AssertionError("Tampered backup must not decrypt")
        } catch (_: Exception) { }
    }
}
