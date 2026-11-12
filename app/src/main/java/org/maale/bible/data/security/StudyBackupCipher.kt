package org.maale.bible.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Encrypts portable study backups with an AES-256-GCM key held by Android Keystore.
 * The key is non-exportable; ciphertext is authenticated and bound to this backup format.
 * This protects exported backups, not the current SQLite database.
 */
class StudyBackupCipher {
    fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        cipher.updateAAD(AAD)
        val encrypted = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val payload = cipher.iv + encrypted
        return PREFIX + Base64.encodeToString(payload, Base64.NO_WRAP)
    }

    fun decrypt(envelope: String): String {
        require(envelope.startsWith(PREFIX)) { "Unsupported encrypted backup format" }
        val payload = Base64.decode(envelope.removePrefix(PREFIX), Base64.NO_WRAP)
        require(payload.size > IV_BYTES + TAG_BYTES) { "Encrypted backup is truncated" }
        val iv = payload.copyOfRange(0, IV_BYTES)
        val ciphertext = payload.copyOfRange(IV_BYTES, payload.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(TAG_BITS, iv))
        cipher.updateAAD(AAD)
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }

    private fun getOrCreateKey(): SecretKey {
        val store = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setKeySize(256)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    companion object {
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "maale_bible_study_backup_v1"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val PREFIX = "MAALE-BIBLE-ENC1:"
        private const val IV_BYTES = 12
        private const val TAG_BITS = 128
        private const val TAG_BYTES = 16
        private val AAD = "maale-bible-study-backup-v1".toByteArray(Charsets.UTF_8)
    }
}

/** Portable encrypted backup helpers. Store or share only the returned ciphertext. */
fun org.maale.bible.domain.repository.StudyRepository.exportEncryptedBackup(
    cipher: StudyBackupCipher = StudyBackupCipher()
): String = cipher.encrypt(exportBackup())

fun org.maale.bible.domain.repository.StudyRepository.restoreEncryptedBackup(
    envelope: String,
    cipher: StudyBackupCipher = StudyBackupCipher()
) {
    restoreBackup(cipher.decrypt(envelope))
}
