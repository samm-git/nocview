package dev.sammgit.nocview.ui.hosts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sammgit.nocview.data.repo.StatusRepository
import dev.sammgit.nocview.domain.model.Host
import dev.sammgit.nocview.domain.model.HostCounts
import dev.sammgit.nocview.domain.model.HostFilter
import dev.sammgit.nocview.domain.model.HostStatus
import dev.sammgit.nocview.domain.model.ServiceCounts
import dev.sammgit.nocview.domain.model.ServiceFilter
import dev.sammgit.nocview.ui.common.StatusFilterStore
import dev.sammgit.nocview.ui.common.friendlyError
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HostsUiState(
    val hosts: List<Host> = emptyList(),
    val hostCounts: HostCounts = HostCounts(),
    val serviceCounts: ServiceCounts = ServiceCounts(),
    val lastUpdated: Long? = null,
    val query: String = "",
    val filter: HostFilter = HostFilter.ALL,
    val serviceFilter: ServiceFilter = ServiceFilter.ALL,
)

@HiltViewModel
class HostsViewModel @Inject constructor(
    private val repository: StatusRepository,
    private val filterStore: StatusFilterStore,
) : ViewModel() {

    private val query = MutableStateFlow("")

    val isRefreshing = MutableStateFlow(false)
    val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HostsUiState> =
        combine(
            repository.hosts,
            repository.meta,
            query,
            filterStore.hostFilter,
            filterStore.serviceFilter,
        ) { hosts, meta, q, f, sf ->
            HostsUiState(
                hosts = applyFilter(hosts, q, f),
                hostCounts = meta?.let {
                    HostCounts(it.hostUp, it.hostDown, it.hostUnreachable, it.hostPending)
                } ?: HostCounts(),
                serviceCounts = meta?.let {
                    ServiceCounts(it.svcOk, it.svcWarning, it.svcCritical, it.svcUnknown, it.svcPending)
                } ?: ServiceCounts(),
                lastUpdated = meta?.lastUpdated,
                query = q,
                filter = f,
                serviceFilter = sf,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HostsUiState())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onFilterChange(value: HostFilter) {
        filterStore.selectHostFilter(value)
    }

    fun onServiceFilterChange(value: ServiceFilter) {
        filterStore.selectServiceFilter(value)
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            val result = repository.refresh()
            message.value = result.exceptionOrNull()?.let { friendlyError(it) }
            isRefreshing.value = false
        }
    }

    fun clearMessage() {
        message.value = null
    }

    private fun applyFilter(hosts: List<Host>, query: String, filter: HostFilter): List<Host> {
        val trimmed = query.trim()
        return hosts.filter { host ->
            val matchesQuery = trimmed.isEmpty() ||
                host.name.contains(trimmed, ignoreCase = true) ||
                host.pluginOutput.contains(trimmed, ignoreCase = true)
            val matchesFilter = when (filter) {
                HostFilter.ALL -> true
                HostFilter.UP -> host.status == HostStatus.UP
                HostFilter.DOWN -> host.status == HostStatus.DOWN
                HostFilter.UNREACHABLE -> host.status == HostStatus.UNREACHABLE
            }
            matchesQuery && matchesFilter
        }
    }
}
