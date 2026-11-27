package org.maale.bible.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.maale.bible.R
import org.maale.bible.data.auth.AuthApiClient
import org.maale.bible.data.auth.AuthApiException
import org.maale.bible.data.auth.AuthTokenStore
import org.maale.bible.data.auth.AuthTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    onContinueAsGuest: () -> Unit,
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    val apiConfigured = AuthApiClient.isConfigured
    val requestFailedText = stringResource(R.string.account_request_failed)
    val otpSentText = stringResource(R.string.account_otp_sent)
    val sessionPendingText = stringResource(R.string.account_session_check_pending)
    val workingText = stringResource(R.string.account_working)

    var selectedTab by remember { mutableIntStateOf(0) }
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var registrationPassword by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var challengeId by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var messageIsError by remember { mutableStateOf(false) }
    var sessionTokens by remember { mutableStateOf(AuthTokenStore.load(context)) }
    var accountId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(apiConfigured, sessionTokens) {
        val saved = sessionTokens ?: return@LaunchedEffect
        if (!apiConfigured) {
            accountId = null
            message = sessionPendingText
            messageIsError = false
            return@LaunchedEffect
        }
        try {
            accountId = AuthApiClient.currentAccount(saved.accessToken)
        } catch (failure: AuthApiException) {
            if (failure.statusCode == 401) {
                try {
                    val refreshed = AuthApiClient.refresh(saved.refreshToken)
                    AuthTokenStore.save(context, refreshed)
                    sessionTokens = refreshed
                    accountId = AuthApiClient.currentAccount(refreshed.accessToken)
                } catch (_: Exception) {
                    AuthTokenStore.clear(context)
                    sessionTokens = null
                    accountId = null
                }
            } else {
                message = requestFailedText
                messageIsError = true
            }
        } catch (_: Exception) {
            message = requestFailedText
            messageIsError = true
        }
    }

    fun runAuth(action: suspend () -> AuthTokens) {
        scope.launch {
            busy = true
            message = workingText
            messageIsError = false
            try {
                val tokens = action()
                AuthTokenStore.save(context, tokens)
                sessionTokens = tokens
                accountId = AuthApiClient.currentAccount(tokens.accessToken)
                onAuthenticated()
            } catch (_: Exception) {
                message = requestFailedText
                messageIsError = true
            } finally {
                busy = false
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(Modifier.widthIn(max = 600.dp).fillMaxWidth()) {
            Text(stringResource(R.string.account_title), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.account_guest_first), style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (!apiConfigured) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.account_service_unavailable_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text(stringResource(R.string.account_service_unavailable_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
        if (sessionTokens != null) {
            Text(
                stringResource(if (accountId != null) R.string.account_signed_in else R.string.account_session_check_pending),
                style = MaterialTheme.typography.titleMedium
            )
            Button(
                onClick = {
                    val saved = sessionTokens
                    scope.launch {
                        busy = true
                        if (saved != null && apiConfigured) runCatching { AuthApiClient.logout(saved.accessToken) }
                        AuthTokenStore.clear(context)
                        sessionTokens = null
                        accountId = null
                        busy = false
                    }
                },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth().testTag("account_logout")
            ) { Text(stringResource(R.string.account_sign_out)) }
        } else {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.account_login)) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.account_register)) })
            }
            if (selectedTab == 0) {
                OutlinedTextField(
                    value = identifier, onValueChange = { identifier = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    label = { Text(stringResource(R.string.account_identifier)) },
                    supportingText = { Text(stringResource(R.string.account_identifier_help)) }
                )
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    label = { Text(stringResource(R.string.account_password)) }
                )
                Button(
                    onClick = { runAuth { AuthApiClient.login(identifier.trim(), password) } },
                    enabled = apiConfigured && !busy && identifier.isNotBlank() && password.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().testTag("account_login_submit")
                ) { Text(stringResource(R.string.account_login)) }
            } else {
                OutlinedTextField(
                    value = username, onValueChange = { username = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    label = { Text(stringResource(R.string.account_username)) }
                )
                OutlinedTextField(
                    value = mobile, onValueChange = { mobile = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    label = { Text(stringResource(R.string.account_mobile)) },
                    supportingText = { Text(stringResource(R.string.account_mobile_help)) }
                )
                OutlinedTextField(
                    value = registrationPassword, onValueChange = { registrationPassword = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    label = { Text(stringResource(R.string.account_password)) }
                )
                Button(
                    onClick = {
                        scope.launch {
                            busy = true
                            message = workingText
                            messageIsError = false
                            try {
                                val challenge = AuthApiClient.register(username.trim(), mobile.trim(), registrationPassword)
                                challengeId = challenge.challengeId
                                message = otpSentText
                            } catch (_: Exception) {
                                message = requestFailedText
                                messageIsError = true
                            } finally {
                                busy = false
                            }
                        }
                    },
                    enabled = apiConfigured && !busy && username.isNotBlank() &&
                        mobile.isNotBlank() && registrationPassword.length >= 12,
                    modifier = Modifier.fillMaxWidth().testTag("account_register_request_otp")
                ) { Text(stringResource(R.string.account_request_otp)) }
                if (challengeId != null) {
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { otp = it.filter { char -> char in '0'..'9' }.take(6) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(stringResource(R.string.account_otp)) }
                    )
                    Button(
                        onClick = { runAuth { AuthApiClient.verifyRegistration(challengeId!!, otp) } },
                        enabled = apiConfigured && !busy && otp.length == 6,
                        modifier = Modifier.fillMaxWidth().testTag("account_verify_register")
                    ) { Text(stringResource(R.string.account_verify_register)) }
                }
                Text(stringResource(R.string.account_otp_policy),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (message != null) {
            Text(
                message.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = if (messageIsError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(4.dp))
        Button(onClick = onContinueAsGuest, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.account_continue_guest))
        }
    }
}
