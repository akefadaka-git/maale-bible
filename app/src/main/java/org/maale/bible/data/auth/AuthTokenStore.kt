package org.maale.bible.data.auth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Access and refresh tokens are encrypted with an app-scoped Android Keystore AES-GCM key. */
object AuthTokenStore {
    private const val PREFERENCES = "maale_bible_auth_v1"
    private const val KEY_ALIAS = "org.maale.bible.auth.tokens.v1"
    private const val CIPHERTEXT = "ciphertext"
    private const val IV = "iv"
    private val lock = Any()
    private val aad = "MaaleBible:AuthTokens:v1".toByteArray(Charsets.UTF_8)

    fun save(context: Context, tokens: AuthTokens) {
        val plaintext = JSONObject()
            .put("access_token", tokens.accessToken)
            .put("refresh_token", tokens.refreshToken)
            .put("expires_in", tokens.expiresInSeconds)
            .put("access_expires_at", tokens.accessExpiresAt)
            .put("refresh_expires_at", tokens.refreshExpiresAt)
            .toString()
            .toByteArray(Charsets.UTF_8)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        cipher.updateAAD(aad)
        val ciphertext = cipher.doFinal(plaintext)
        try {
            check(
                context.applicationContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
                    .edit()
                    .putString(IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
                    .putString(CIPHERTEXT, Base64.encodeToString(ciphertext, Base64.NO_WRAP))
                    .commit()
            ) { "Unable to persist encrypted account session." }
        } finally {
            plaintext.fill(0)
        }
    }

    fun load(context: Context): AuthTokens? {
        val preferences = context.applicationContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        val ivText = preferences.getString(IV, null) ?: return null
        val ciphertextText = preferences.getString(CIPHERTEXT, null) ?: run {
            clear(context)
            return null
        }
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(
                Cipher.DECRYPT_MODE,
                key(),
                GCMParameterSpec(128, Base64.decode(ivText, Base64.NO_WRAP))
            )
            cipher.updateAAD(aad)
            val plaintext = cipher.doFinal(Base64.decode(ciphertextText, Base64.NO_WRAP))
            try {
                val json = JSONObject(String(plaintext, Charsets.UTF_8))
                AuthTokens(
                    accessToken = json.getString("access_token"),
                    refreshToken = json.getString("refresh_token"),
                    expiresInSeconds = json.getLong("expires_in"),
                    accessExpiresAt = json.getString("access_expires_at"),
                    refreshExpiresAt = json.getString("refresh_expires_at")
                )
            } finally {
                plaintext.fill(0)
            }
        } catch (_: Exception) {
            clear(context)
            null
        }
    }

    fun clear(context: Context) {
        check(
            context.applicationContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
                .edit().clear().commit()
        ) { "Unable to clear stored account session." }
    }

    private fun key(): SecretKey = synchronized(lock) {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey) ?: run {
            val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            generator.init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
            generator.generateKey()
        }
    }
}
