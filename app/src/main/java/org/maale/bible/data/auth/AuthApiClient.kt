package org.maale.bible.data.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.maale.bible.BuildConfig
import java.io.IOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresInSeconds: Long,
    val accessExpiresAt: String,
    val refreshExpiresAt: String
)

data class RegistrationChallenge(
    val challengeId: String,
    val expiresAt: String
)

class AuthApiException(val statusCode: Int) : IOException("Authentication request failed.")

object AuthApiClient {
    val isConfigured: Boolean
        get() = runCatching { configuredBaseUrl(); true }.getOrDefault(false)

    suspend fun login(identifier: String, password: String): AuthTokens =
        withContext(Dispatchers.IO) {
            request(
                path = "/auth/login",
                method = "POST",
                body = JSONObject().put("identifier", identifier).put("password", password)
            ).toTokens()
        }

    suspend fun register(username: String, mobileNumber: String, password: String): RegistrationChallenge =
        withContext(Dispatchers.IO) {
            val response = request(
                path = "/auth/register",
                method = "POST",
                body = JSONObject().put("username", username)
                    .put("mobile_number", mobileNumber)
                    .put("password", password)
            )
            RegistrationChallenge(
                challengeId = response.getString("challenge_id"),
                expiresAt = response.getString("expires_at")
            )
        }

    suspend fun verifyRegistration(challengeId: String, otpCode: String): AuthTokens =
        withContext(Dispatchers.IO) {
            request(
                path = "/auth/register/verify",
                method = "POST",
                body = JSONObject().put("challenge_id", challengeId).put("otp_code", otpCode)
            ).toTokens()
        }

    suspend fun refresh(refreshToken: String): AuthTokens =
        withContext(Dispatchers.IO) {
            request(
                path = "/auth/refresh",
                method = "POST",
                body = JSONObject().put("refresh_token", refreshToken)
            ).toTokens()
        }

    suspend fun currentAccount(accessToken: String): String =
        withContext(Dispatchers.IO) {
            request(
                path = "/auth/me",
                method = "GET",
                bearerToken = accessToken
            ).getString("account_id")
        }

    suspend fun logout(accessToken: String) {
        withContext(Dispatchers.IO) {
            request(
                path = "/auth/logout",
                method = "POST",
                body = JSONObject(),
                bearerToken = accessToken
            )
        }
    }

    private fun AuthTokensResponseNotUsed() = Unit

    private fun JSONObject.toTokens() = AuthTokens(
        accessToken = getString("access_token"),
        refreshToken = getString("refresh_token"),
        expiresInSeconds = getLong("expires_in"),
        accessExpiresAt = getString("access_expires_at"),
        refreshExpiresAt = getString("refresh_expires_at")
    )

    private fun configuredBaseUrl(): String {
        val raw = BuildConfig.MAALE_BIBLE_API_BASE_URL.trim().trimEnd('/')
        require(raw.startsWith("https://", ignoreCase = true)) {
            "A production HTTPS API base URL is not configured."
        }
        val parsed = URL(raw)
        require(parsed.protocol.equals("https", ignoreCase = true))
        require(parsed.host.isNotBlank() && parsed.userInfo == null && parsed.query == null && parsed.ref == null)
        return raw
    }

    private fun request(
        path: String,
        method: String,
        body: JSONObject? = null,
        bearerToken: String? = null
    ): JSONObject {
        val endpoint = URL(configuredBaseUrl() + path)
        val connection = endpoint.openConnection() as? HttpsURLConnection
            ?: throw IOException("Authentication API must use HTTPS.")
        try {
            connection.requestMethod = method
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.instanceFollowRedirects = false
            connection.useCaches = false
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Cache-Control", "no-store")
            connection.setRequestProperty("Pragma", "no-cache")
            if (bearerToken != null) {
                connection.setRequestProperty("Authorization", "Bearer $bearerToken")
            }
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val responseBody = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) throw AuthApiException(status)
            return if (responseBody.isBlank()) JSONObject() else JSONObject(responseBody)
        } finally {
            connection.disconnect()
        }
    }
}
