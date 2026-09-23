package dev.sammgit.nocview.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sammgit.nocview.ui.components.FilterRow
import dev.sammgit.nocview.ui.theme.StatusGreen
import dev.sammgit.nocview.ui.theme.StatusRed

private val refreshOptions = listOf(0, 15, 30, 60, 300)

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val baseUrl by viewModel.baseUrl.collectAsStateWithLifecycle()
    val username by viewModel.username.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val trustSelfSigned by viewModel.trustSelfSigned.collectAsStateWithLifecycle()
    val autoRefreshSeconds by viewModel.autoRefreshSeconds.collectAsStateWithLifecycle()
    val isTesting by viewModel.isTesting.collectAsStateWithLifecycle()
    val testResult by viewModel.testResult.collectAsStateWithLifecycle()
    val testError by viewModel.testError.collectAsStateWithLifecycle()
    val savedMessage by viewModel.savedMessage.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Connection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = baseUrl,
            onValueChange = viewModel::onBaseUrlChange,
            label = { Text("Monitoring web root URL") },
            supportingText = { Text("Example: http://example.com/nagios") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = username,
            onValueChange = viewModel::onUsernameChange,
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Switch(
                checked = trustSelfSigned,
                onCheckedChange = viewModel::onTrustSelfSignedChange,
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Trust self-signed / untrusted certificates",
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Text("Auto-refresh", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        FilterRow(
            filters = refreshOptions,
            selected = autoRefreshSeconds,
            onSelected = viewModel::onAutoRefreshChange,
            label = { if (it == 0) "Off" else "${it}s" },
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = viewModel::save,
                modifier = Modifier.weight(1f),
            ) {
                Text("Save")
            }
            OutlinedButton(
                onClick = viewModel::testConnection,
                enabled = !isTesting,
                modifier = Modifier.weight(1f),
            ) {
                Text(if (isTesting) "Testing…" else "Test connection")
            }
        }

        if (savedMessage != null) {
            Text(
                text = savedMessage.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        testResult?.let { result ->
            Card(colors = CardDefaults.cardColors(containerColor = StatusGreen.copy(alpha = 0.15f))) {
                Text(
                    text = result,
                    color = StatusGreen,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        testError?.let { error ->
            Card(colors = CardDefaults.cardColors(containerColor = StatusRed.copy(alpha = 0.15f))) {
                Text(
                    text = error,
                    color = StatusRed,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Spacer(Modifier.width(4.dp))
        Text(
            text = "NOC View reads host and service state from the monitoring server's JSON CGI " +
                "endpoint (statusjson.cgi) and can acknowledge hosts and services through the " +
                "external command interface (cmd.cgi).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
