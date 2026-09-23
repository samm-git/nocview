package dev.asamorukov.nocview.ui.services

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.asamorukov.nocview.domain.model.Service
import dev.asamorukov.nocview.ui.components.AckDialog
import dev.asamorukov.nocview.ui.components.EmptyState
import dev.asamorukov.nocview.ui.components.SearchField
import dev.asamorukov.nocview.ui.components.ServiceRow
import dev.asamorukov.nocview.ui.components.SummaryHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    snackbarHostState: SnackbarHostState,
    onServiceClick: (String, String) -> Unit,
    onHostFilterClick: () -> Unit,
    viewModel: ServicesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

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
            item {
                SummaryHeader(
                    hostCounts = state.hostCounts,
                    serviceCounts = state.serviceCounts,
                    lastUpdated = state.lastUpdated,
                    hostFilter = state.hostFilter,
                    serviceFilter = state.filter,
                    onHostStateClick = { filter ->
                        viewModel.onHostFilterChange(filter)
                        onHostFilterClick()
                    },
                    onServiceStateClick = viewModel::onFilterChange,
                    modifier = Modifier.padding(12.dp),
                )
            }
            item {
                SearchField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = "Search services",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
            if (state.services.isEmpty()) {
                item { EmptyState("No services match the current filter.") }
            } else {
                items(state.services, key = { it.key }) { service ->
                    ServiceRow(
                        service = service,
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
