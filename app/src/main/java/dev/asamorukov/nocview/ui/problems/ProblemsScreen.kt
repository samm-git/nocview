package dev.asamorukov.nocview.ui.problems

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.asamorukov.nocview.domain.model.Host
import dev.asamorukov.nocview.domain.model.Service
import dev.asamorukov.nocview.ui.components.AckDialog
import dev.asamorukov.nocview.ui.components.EmptyState
import dev.asamorukov.nocview.ui.components.HostRow
import dev.asamorukov.nocview.ui.components.ServiceRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemsScreen(
    snackbarHostState: SnackbarHostState,
    onHostClick: (String) -> Unit,
    onServiceClick: (String, String) -> Unit,
    viewModel: ProblemsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    var ackHostTarget by remember { mutableStateOf<Host?>(null) }
    var ackServiceTarget by remember { mutableStateOf<Service?>(null) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            if (state.total == 0) {
                item { EmptyState("No active problems. Everything is OK.") }
            } else {
                if (state.problemHosts.isNotEmpty()) {
                    item {
                        SectionTitle("Hosts (${state.problemHosts.size})")
                    }
                    items(state.problemHosts, key = { "host-${it.name}" }) { host ->
                        HostRow(
                            host = host,
                            onClick = { onHostClick(host.name) },
                            onAck = if (!host.acknowledged) {
                                { ackHostTarget = host }
                            } else {
                                null
                            },
                        )
                    }
                }
                if (state.problemServices.isNotEmpty()) {
                    item {
                        SectionTitle("Services (${state.problemServices.size})")
                    }
                    items(state.problemServices, key = { "svc-${it.key}" }) { service ->
                        ServiceRow(
                            service = service,
                            onClick = { onServiceClick(service.hostName, service.description) },
                            onAck = if (!service.acknowledged) {
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

    ackHostTarget?.let { host ->
        AckDialog(
            title = "Acknowledge ${host.name}",
            onDismiss = { ackHostTarget = null },
            onConfirm = { comment ->
                viewModel.acknowledgeHost(host, comment)
                ackHostTarget = null
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

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
