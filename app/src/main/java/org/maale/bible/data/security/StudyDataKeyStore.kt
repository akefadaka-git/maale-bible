package org.maale.bible.data.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.File
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Device-bound wrapping for the random 256-bit SQLCipher DEK. */
object StudyDataKeyStore {
    private const val PREFS = "study_database_key_v1"
    private const val WRAPPED = "wrapped_dek"
    private const val IV = "wrap_iv"
    private const val ALIAS = "maale_bible_study_dek_wrap_v1"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val TAG_BITS = 128
    private val aad = "maale-bible-study-database-dek-v1".toByteArray(Charsets.UTF_8)

    @Synchronized fun getOrCreate(context: Context): ByteArray {
        val app = context.applicationContext
        val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.contains(WRAPPED)) return unwrap(app)
        val db = app.getDatabasePath("maale_bible_study.db")
        if (db.exists() && !isLegacySqlite(db)) {
            throw IllegalStateException("Database exists but its encryption key is missing; refusing destructive re-key")
        }
        return ByteArray(32).also { SecureRandom().nextBytes(it); wrap(app, it) }
    }

    @Synchronized fun load(context: Context): ByteArray = unwrap(context.applicationContext)

    @Synchronized fun installRecovered(context: Context, dek: ByteArray) {
        require(dek.size == 32) { "Recovered DEK must be 256 bits" }
        val app = context.applicationContext
        require(!app.getDatabasePath("maale_bible_study.db").exists()) {
            "Recovery target must be empty; existing data is never overwritten"
        }
        val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        check(prefs.edit().clear().commit()) { "Could not clear previous local key wrapper before recovery" }
        keyStore().let { if (it.containsAlias(ALIAS)) it.deleteEntry(ALIAS) }
        wrap(app, dek)
    }

    /**
     * Remove a newly installed recovery wrapper only after the failed restore has removed
     * every database file. This prevents the next normal app open from creating an empty
     * database under a key whose restore operation did not complete.
     */
    @Synchronized fun clearRecoveredAfterFailedRestore(context: Context) {
        val app = context.applicationContext
        val db = app.getDatabasePath("maale_bible_study.db")
        val remnants = listOf(db, File(db.path + "-wal"), File(db.path + "-shm"), File(db.path + "-journal"))
        check(remnants.none(File::exists)) { "Cannot clear recovered key while database files remain" }
        val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        check(prefs.edit().clear().commit()) { "Could not clear failed recovery key wrapper" }
        val store = keyStore()
        if (store.containsAlias(ALIAS)) {
            store.deleteEntry(ALIAS)
            check(!store.containsAlias(ALIAS)) { "Could not clear failed recovery Keystore key" }
        }
    }

    private fun wrap(context: Context, dek: ByteArray) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, wrappingKey())
        cipher.updateAAD(aad)
        val ok = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .putString(WRAPPED, Base64.encodeToString(cipher.doFinal(dek), Base64.NO_WRAP))
            .commit()
        check(ok) { "Could not persist wrapped database key" }
    }

    private fun unwrap(context: Context): ByteArray {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val iv = Base64.decode(requireNotNull(prefs.getString(IV, null)), Base64.NO_WRAP)
        val ciphertext = Base64.decode(requireNotNull(prefs.getString(WRAPPED, null)), Base64.NO_WRAP)
        require(iv.size == 12 && ciphertext.size == 48) { "Invalid wrapped database key" }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, wrappingKey(), GCMParameterSpec(TAG_BITS, iv))
        cipher.updateAAD(aad)
        return cipher.doFinal(ciphertext).also { require(it.size == 32) }
    }

    private fun wrappingKey(): SecretKey {
        val store = keyStore()
        (store.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        check(!store.containsAlias(ALIAS)) { "Unexpected Keystore entry for database key" }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setKeySize(256).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true).build())
        return generator.generateKey()
    }

    private fun keyStore(): KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    private fun isLegacySqlite(file: File): Boolean = runCatching {
        file.inputStream().use { input ->
            val header = ByteArray(16)
            input.read(header) == 16 && String(header, Charsets.US_ASCII) == "SQLite format 3\u0000"
        }
    }.getOrDefault(false)
}
