package dev.sammgit.nocview.ui.hostdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sammgit.nocview.data.repo.StatusRepository
import dev.sammgit.nocview.domain.model.Host
import dev.sammgit.nocview.domain.model.Service
import dev.sammgit.nocview.ui.common.friendlyError
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HostDetailUiState(
    val host: Host? = null,
    val services: List<Service> = emptyList(),
)

@HiltViewModel
class HostDetailViewModel @Inject constructor(
    private val repository: StatusRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val hostName: String = savedStateHandle.get<String>("hostName").orEmpty()

    val isRefreshing = MutableStateFlow(false)
    val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HostDetailUiState> =
        combine(
            repository.host(hostName),
            repository.servicesForHost(hostName),
        ) { host, services ->
            HostDetailUiState(host = host, services = services)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HostDetailUiState())

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

    fun acknowledgeHost(comment: String) {
        val host = uiState.value.host ?: return
        viewModelScope.launch {
            message.value = repository.acknowledgeHost(host.name, comment).fold(
                onSuccess = { "Acknowledgement submitted for ${host.name}." },
                onFailure = { friendlyError(it) },
            )
        }
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
}
