package dev.asamorukov.nocview.ui.servicedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.asamorukov.nocview.domain.model.Service
import dev.asamorukov.nocview.ui.components.AckDialog
import dev.asamorukov.nocview.ui.components.EmptyState
import dev.asamorukov.nocview.ui.components.KeyValueRow
import dev.asamorukov.nocview.ui.components.OutputText
import dev.asamorukov.nocview.ui.components.StatusPill
import dev.asamorukov.nocview.ui.components.formatRelative
import dev.asamorukov.nocview.ui.components.formatTimestamp
import dev.asamorukov.nocview.ui.theme.StatusBlue
import dev.asamorukov.nocview.ui.theme.StatusGray
import dev.asamorukov.nocview.ui.theme.serviceStatusColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onHostClick: (String) -> Unit,
    viewModel: ServiceDetailViewModel = hiltViewModel(),
) {
    val service by viewModel.service.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    var showAck by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = service?.description ?: viewModel.serviceDescription,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
        )

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            val current = service
            if (current == null) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item { EmptyState("Service not found in the local cache. Pull to refresh.") }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    item {
                        ServiceInfoCard(
                            service = current,
                            onAck = if (current.status.isProblem && !current.acknowledged) {
                                { showAck = true }
                            } else {
                                null
                            },
                            onHostClick = { onHostClick(current.hostName) },
                        )
                    }
                }
            }
        }

        val current = service
        if (showAck && current != null) {
            AckDialog(
                title = "Acknowledge ${current.description}",
                onDismiss = { showAck = false },
                onConfirm = { comment ->
                    viewModel.acknowledgeService(comment)
                    showAck = false
                },
            )
        }
    }
}

@Composable
private fun ServiceInfoCard(
    service: Service,
    onAck: (() -> Unit)?,
    onHostClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusPill(service.status.name, serviceStatusColor(service.status))
                Spacer(Modifier.width(8.dp))
                if (service.acknowledged) StatusPill("ACK", StatusBlue)
                if (service.inDowntime) {
                    Spacer(Modifier.width(8.dp))
                    StatusPill("DOWNTIME", StatusGray)
                }
                if (service.isFlapping) {
                    Spacer(Modifier.width(8.dp))
                    StatusPill("FLAPPING", StatusGray)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = service.hostName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            OutputText(service.pluginOutput)
            if (service.longPluginOutput.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                OutputText(service.longPluginOutput)
            }
            Spacer(Modifier.height(12.dp))
            KeyValueRow("State type", service.stateType.ifBlank { "—" })
            KeyValueRow("Attempts", "${service.currentAttempt}/${service.maxAttempts}")
            KeyValueRow(
                "Last check",
                "${formatTimestamp(service.lastCheck)} (${formatRelative(service.lastCheck)})",
            )
            KeyValueRow("Next check", formatTimestamp(service.nextCheck))
            KeyValueRow("Last state change", formatTimestamp(service.lastStateChange))
            KeyValueRow("Latency", "${service.latency ?: 0.0} s")
            KeyValueRow("Execution", "${service.executionTime ?: 0.0} s")
            KeyValueRow("Notifications", if (service.notificationsEnabled) "enabled" else "disabled")
            KeyValueRow("Checks", if (service.checksEnabled) "enabled" else "disabled")
            KeyValueRow("Last OK", formatTimestamp(service.lastTimeOk))
            KeyValueRow("Last Warning", formatTimestamp(service.lastTimeWarning))
            KeyValueRow("Last Critical", formatTimestamp(service.lastTimeCritical))
            KeyValueRow("Last Unknown", formatTimestamp(service.lastTimeUnknown))
            if (service.perfData.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Performance data",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutputText(service.perfData)
            }
            if (onAck != null) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onAck,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Acknowledge service")
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onHostClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("View host")
            }
        }
    }
}
