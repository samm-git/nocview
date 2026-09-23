package dev.sammgit.nocview.ui.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sammgit.nocview.data.repo.StatusRepository
import dev.sammgit.nocview.domain.model.HostCounts
import dev.sammgit.nocview.domain.model.HostFilter
import dev.sammgit.nocview.domain.model.Service
import dev.sammgit.nocview.domain.model.ServiceCounts
import dev.sammgit.nocview.domain.model.ServiceFilter
import dev.sammgit.nocview.domain.model.ServiceStatus
import dev.sammgit.nocview.ui.common.StatusFilterStore
import dev.sammgit.nocview.ui.common.friendlyError
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ServicesUiState(
    val services: List<Service> = emptyList(),
    val hostCounts: HostCounts = HostCounts(),
    val serviceCounts: ServiceCounts = ServiceCounts(),
    val lastUpdated: Long? = null,
    val query: String = "",
    val filter: ServiceFilter = ServiceFilter.ALL,
    val hostFilter: HostFilter = HostFilter.ALL,
)

@HiltViewModel
class ServicesViewModel @Inject constructor(
    private val repository: StatusRepository,
    private val filterStore: StatusFilterStore,
) : ViewModel() {

    private val query = MutableStateFlow("")

    val isRefreshing = MutableStateFlow(false)
    val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ServicesUiState> =
        combine(
            repository.services,
            repository.meta,
            query,
            filterStore.serviceFilter,
            filterStore.hostFilter,
        ) { services, meta, q, f, hf ->
            ServicesUiState(
                services = applyFilter(services, q, f),
                hostCounts = meta?.let {
                    HostCounts(it.hostUp, it.hostDown, it.hostUnreachable, it.hostPending)
                } ?: HostCounts(),
                serviceCounts = meta?.let {
                    ServiceCounts(it.svcOk, it.svcWarning, it.svcCritical, it.svcUnknown, it.svcPending)
                } ?: ServiceCounts(),
                lastUpdated = meta?.lastUpdated,
                query = q,
                filter = f,
                hostFilter = hf,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ServicesUiState())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onFilterChange(value: ServiceFilter) {
        filterStore.selectServiceFilter(value)
    }

    fun onHostFilterChange(value: HostFilter) {
        filterStore.selectHostFilter(value)
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

    fun acknowledgeService(service: Service, comment: String) {
        viewModelScope.launch {
            message.value = repository
                .acknowledgeService(service.hostName, service.description, comment)
                .fold(
                    onSuccess = { "Acknowledgement submitted for ${service.description}." },
                    onFailure = { friendlyError(it) },
                )
        }
    }

    private fun applyFilter(
        services: List<Service>,
        query: String,
        filter: ServiceFilter,
    ): List<Service> {
        val trimmed = query.trim()
        return services.filter { service ->
            val matchesQuery = trimmed.isEmpty() ||
                service.description.contains(trimmed, ignoreCase = true) ||
                service.hostName.contains(trimmed, ignoreCase = true) ||
                service.pluginOutput.contains(trimmed, ignoreCase = true)
            val matchesFilter = when (filter) {
                ServiceFilter.ALL -> true
                ServiceFilter.OK -> service.status == ServiceStatus.OK
                ServiceFilter.WARNING -> service.status == ServiceStatus.WARNING
                ServiceFilter.CRITICAL -> service.status == ServiceStatus.CRITICAL
                ServiceFilter.UNKNOWN -> service.status == ServiceStatus.UNKNOWN
            }
            matchesQuery && matchesFilter
        }
    }
}
