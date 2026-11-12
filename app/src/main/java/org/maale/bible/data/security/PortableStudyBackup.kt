package org.maale.bible.data.security

import android.content.Context
import android.util.Base64
import net.zetetic.database.sqlcipher.SQLiteDatabase
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import org.json.JSONObject
import org.maale.bible.data.repository.StudyDatabase
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/** Portable study backup: Argon2id derives a KEK, AES-GCM wraps the SQLCipher DEK and payload. */
object PortableStudyBackup {
    private const val FORMAT = "MAALE-STUDY-ENC2"
    private const val VERSION = 2
    private const val SCHEMA = StudyDatabase.DB_VERSION
    private const val MEMORY_KIB = 65536
    private const val ITERATIONS = 3
    private const val PARALLELISM = 1
    private const val MAX_ENVELOPE_CHARS = 32 * 1024 * 1024
    private val random = SecureRandom()

    fun generateRecoveryKey(): String =
        Base64.encodeToString(ByteArray(32).also(random::nextBytes),
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)

    fun export(context: Context, database: StudyDatabase, recoveryKey: String): String {
        val dek = StudyDataKeyStore.load(context)
        val secret = decodeRecoveryKey(recoveryKey)
        val salt = ByteArray(16).also(random::nextBytes)
        val wrapNonce = ByteArray(12).also(random::nextBytes)
        val dataNonce = ByteArray(12).also(random::nextBytes)
        val saltText = b64(salt)
        val aad = "$FORMAT|$VERSION|$SCHEMA|argon2id|$MEMORY_KIB|$ITERATIONS|$PARALLELISM|$saltText"
        val kek = derive(secret, salt)
        try {
            val wrappedDek = aesGcm(Cipher.ENCRYPT_MODE, kek, wrapNonce, "$aad|dek", dek)
            val encryptedPayload = aesGcm(Cipher.ENCRYPT_MODE, dek, dataNonce, "$aad|payload",
                database.exportBackup().toByteArray(Charsets.UTF_8))
            return JSONObject().put("format", FORMAT).put("version", VERSION).put("schema", SCHEMA)
                .put("kdf", "argon2id").put("memoryKiB", MEMORY_KIB)
                .put("iterations", ITERATIONS).put("parallelism", PARALLELISM)
                .put("salt", saltText).put("wrapNonce", b64(wrapNonce))
                .put("wrappedDek", b64(wrappedDek)).put("dataNonce", b64(dataNonce))
                .put("payload", b64(encryptedPayload)).toString()
        } finally {
            secret.fill(0)
            kek.fill(0)
            dek.fill(0)
        }
    }

    /** Authenticate and decrypt completely before installing any key or touching the target DB. */
    fun restore(context: Context, envelope: String, recoveryKey: String) {
        require(envelope.length <= MAX_ENVELOPE_CHARS) { "Backup exceeds supported size" }
        val root = JSONObject(envelope)
        require(root.getString("format") == FORMAT && root.getInt("version") == VERSION &&
            root.getInt("schema") == SCHEMA) { "Unsupported portable backup version" }
        require(root.getString("kdf") == "argon2id" && root.getInt("memoryKiB") == MEMORY_KIB &&
            root.getInt("iterations") == ITERATIONS && root.getInt("parallelism") == PARALLELISM) {
            "Unsupported KDF parameters"
        }
        val salt = unb64(root.getString("salt"))
        val wrapNonce = unb64(root.getString("wrapNonce"))
        val dataNonce = unb64(root.getString("dataNonce"))
        val wrappedDek = unb64(root.getString("wrappedDek"))
        val payload = unb64(root.getString("payload"))
        require(salt.size == 16 && wrapNonce.size == 12 && dataNonce.size == 12)
        require(wrappedDek.size == 48 && payload.size >= 16)

        val secret = decodeRecoveryKey(recoveryKey)
        val aad = "$FORMAT|$VERSION|$SCHEMA|argon2id|$MEMORY_KIB|$ITERATIONS|$PARALLELISM|${b64(salt)}"
        val kek = derive(secret, salt)
        val dek: ByteArray
        val plaintext: String
        try {
            dek = aesGcm(Cipher.DECRYPT_MODE, kek, wrapNonce, "$aad|dek", wrappedDek)
            require(dek.size == 32) { "Invalid recovered database key" }
            plaintext = String(aesGcm(Cipher.DECRYPT_MODE, dek, dataNonce, "$aad|payload", payload), Charsets.UTF_8)
        } finally {
            secret.fill(0)
            kek.fill(0)
        }

        val app = context.applicationContext
        val dbFile = app.getDatabasePath(StudyDatabase.DB_NAME)
        require(!dbFile.exists()) { "Restore target is not empty; existing data was not changed" }
        StudyDataKeyStore.installRecovered(app, dek)
        try {
            val db = StudyDatabase(app)
            try { db.restoreBackup(plaintext) } finally { db.close() }
            val verify = StudyDatabase(app)
            try { check(verify.exportBackup().isNotBlank()) { "Restored database is unreadable" } }
            finally { verify.close() }
        } catch (failure: Throwable) {
            SQLiteDatabase.deleteDatabase(dbFile)
            throw failure
        } finally {
            dek.fill(0)
        }
    }

    private fun derive(secret: ByteArray, salt: ByteArray): ByteArray {
        val params = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withIterations(ITERATIONS).withMemoryAsKB(MEMORY_KIB)
            .withParallelism(PARALLELISM).withSalt(salt).build()
        return ByteArray(32).also { Argon2BytesGenerator().apply { init(params) }.generateBytes(secret, it) }
    }

    private fun aesGcm(mode: Int, key: ByteArray, nonce: ByteArray, aad: String, input: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(mode, SecretKeySpec(key, "AES"), GCMParameterSpec(128, nonce))
        cipher.updateAAD(aad.toByteArray(Charsets.UTF_8))
        return cipher.doFinal(input)
    }

    private fun decodeRecoveryKey(value: String): ByteArray =
        Base64.decode(value, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING).also {
            require(it.size == 32) { "Recovery key must be the generated 256-bit key" }
        }

    private fun b64(value: ByteArray): String = Base64.encodeToString(value, Base64.NO_WRAP or Base64.NO_PADDING)
    private fun unb64(value: String): ByteArray = Base64.decode(value, Base64.NO_WRAP or Base64.NO_PADDING)
}
