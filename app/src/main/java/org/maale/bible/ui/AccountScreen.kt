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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.maale.bible.R

@OptIn(ExperimentalMaterial3Api::class)
/** UI-only account entry. It never claims authentication without a live server-issued session. */
@Composable
fun AccountScreen(onContinueAsGuest: () -> Unit, modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var registrationPassword by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }

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
            Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.account_login))
            }
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
            Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.account_request_otp))
            }
            OutlinedTextField(
                value = otp, onValueChange = { otp = it.filter(Char::isDigit).take(8) },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                label = { Text(stringResource(R.string.account_otp)) }
            )
            Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.account_verify_register))
            }
            Text(stringResource(R.string.account_otp_policy),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(4.dp))
        Button(onClick = onContinueAsGuest, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.account_continue_guest))
        }
    }
}
