package dev.asamorukov.nocview.ui.hostdetail

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import dev.asamorukov.nocview.domain.model.Host
import dev.asamorukov.nocview.domain.model.Service
import dev.asamorukov.nocview.ui.components.AckDialog
import dev.asamorukov.nocview.ui.components.EmptyState
import dev.asamorukov.nocview.ui.components.KeyValueRow
import dev.asamorukov.nocview.ui.components.OutputText
import dev.asamorukov.nocview.ui.components.ServiceRow
import dev.asamorukov.nocview.ui.components.StatusPill
import dev.asamorukov.nocview.ui.components.formatRelative
import dev.asamorukov.nocview.ui.components.formatTimestamp
import dev.asamorukov.nocview.ui.theme.StatusBlue
import dev.asamorukov.nocview.ui.theme.StatusGray
import dev.asamorukov.nocview.ui.theme.hostStatusColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostDetailScreen(
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onServiceClick: (String, String) -> Unit,
    viewModel: HostDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    var showHostAck by remember { mutableStateOf(false) }
    var ackServiceTarget by remember { mutableStateOf<Service?>(null) }

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
                    text = state.host?.name ?: viewModel.hostName,
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
            val host = state.host
            if (host == null) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item { EmptyState("Host not found in the local cache. Pull to refresh.") }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    item {
                        HostInfoCard(
                            host = host,
                            onAck = if (host.status.isProblem && !host.acknowledged) {
                                { showHostAck = true }
                            } else {
                                null
                            },
                        )
                    }
                    item {
                        Text(
                            text = "Services (${state.services.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                    if (state.services.isEmpty()) {
                        item { EmptyState("No services for this host.") }
                    } else {
                        items(state.services, key = { it.key }) { service ->
                            ServiceRow(
                                service = service,
                                showHost = false,
                                onClick = { onServiceClick(service.hostName, service.description) },
                                onAck = if (service.status.isProblem && !service.acknowledged) {
                                    { ackServiceTarget = service }
                                } else {
                                    null
                                },
                            )
                        }
                    }
                }
            }
        }

        val host = state.host
        if (showHostAck && host != null) {
            AckDialog(
                title = "Acknowledge ${host.name}",
                onDismiss = { showHostAck = false },
                onConfirm = { comment ->
                    viewModel.acknowledgeHost(comment)
                    showHostAck = false
                },
            )
        }
        ackServiceTarget?.let { service ->
            AckDialog(
                title = "Acknowledge ${service.description}",
                onDismiss = { ackServiceTarget = null },
                onConfirm = { comment ->
                    viewModel.acknowledgeService(service, comment)
                    ackServiceTarget = null
                },
            )
        }
    }
}

@Composable
private fun HostInfoCard(host: Host, onAck: (() -> Unit)? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusPill(host.status.name, hostStatusColor(host.status))
                Spacer(Modifier.width(8.dp))
                if (host.acknowledged) StatusPill("ACK", StatusBlue)
                if (host.inDowntime) {
                    Spacer(Modifier.width(8.dp))
                    StatusPill("DOWNTIME", StatusGray)
                }
                if (host.isFlapping) {
                    Spacer(Modifier.width(8.dp))
                    StatusPill("FLAPPING", StatusGray)
                }
            }
            Spacer(Modifier.height(8.dp))
            OutputText(host.pluginOutput)
            if (host.longPluginOutput.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                OutputText(host.longPluginOutput)
            }
            Spacer(Modifier.height(12.dp))
            KeyValueRow("State type", host.stateType.ifBlank { "—" })
            KeyValueRow("Attempts", "${host.currentAttempt}/${host.maxAttempts}")
            KeyValueRow("Last check", "${formatTimestamp(host.lastCheck)} (${formatRelative(host.lastCheck)})")
            KeyValueRow("Next check", formatTimestamp(host.nextCheck))
            KeyValueRow("Last state change", formatTimestamp(host.lastStateChange))
            KeyValueRow("Latency", "${host.latency ?: 0.0} s")
            KeyValueRow("Execution", "${host.executionTime ?: 0.0} s")
            KeyValueRow("Notifications", if (host.notificationsEnabled) "enabled" else "disabled")
            KeyValueRow("Checks", if (host.checksEnabled) "enabled" else "disabled")
            if (host.perfData.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Performance data",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutputText(host.perfData)
            }
            if (onAck != null) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onAck,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Acknowledge host")
                }
            }
        }
    }
}
