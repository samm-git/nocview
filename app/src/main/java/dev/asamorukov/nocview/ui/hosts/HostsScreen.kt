package dev.asamorukov.nocview.ui.hosts

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.asamorukov.nocview.ui.components.EmptyState
import dev.asamorukov.nocview.ui.components.HostRow
import dev.asamorukov.nocview.ui.components.SearchField
import dev.asamorukov.nocview.ui.components.SummaryHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostsScreen(
    snackbarHostState: SnackbarHostState,
    onHostClick: (String) -> Unit,
    onServiceFilterClick: () -> Unit,
    viewModel: HostsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

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
                    hostFilter = state.filter,
                    serviceFilter = state.serviceFilter,
                    onHostStateClick = viewModel::onFilterChange,
                    onServiceStateClick = { filter ->
                        viewModel.onServiceFilterChange(filter)
                        onServiceFilterClick()
                    },
                    modifier = Modifier.padding(12.dp),
                )
            }
            item {
                SearchField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = "Search hosts",
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }
            if (state.hosts.isEmpty()) {
                item { EmptyState("No hosts match the current filter.") }
            } else {
                items(state.hosts, key = { it.name }) { host ->
                    HostRow(host = host, onClick = { onHostClick(host.name) })
                }
            }
        }
    }
}
