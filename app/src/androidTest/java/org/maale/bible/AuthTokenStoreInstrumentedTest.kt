package org.maale.bible

import androidx.test.core.app.ApplicationProvider
import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.maale.bible.data.auth.AuthTokenStore
import org.maale.bible.data.auth.AuthTokens

class AuthTokenStoreInstrumentedTest {
    @Test
    fun tokensAreEncryptedAtRestAndCanBeCleared() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        AuthTokenStore.clear(context)
        val tokens = AuthTokens(
            accessToken = "opaque-access-token-synthetic",
            refreshToken = "opaque-refresh-token-synthetic",
            expiresInSeconds = 900,
            accessExpiresAt = "2026-11-27T08:00:00+00:00",
            refreshExpiresAt = "2026-11-28T07:45:00+00:00"
        )
        AuthTokenStore.save(context, tokens)
        assertEquals(tokens, AuthTokenStore.load(context))
        val stored = context.getSharedPreferences("maale_bible_auth_v1", Context.MODE_PRIVATE)
        assertFalse(stored.getString("ciphertext", "").orEmpty().contains(tokens.accessToken))
        assertFalse(stored.getString("ciphertext", "").orEmpty().contains(tokens.refreshToken))
        AuthTokenStore.clear(context)
        assertNull(AuthTokenStore.load(context))
    }
}
