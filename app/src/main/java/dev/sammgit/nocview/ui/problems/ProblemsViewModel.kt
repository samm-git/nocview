package dev.sammgit.nocview.ui.problems

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

data class ProblemsUiState(
    val problemHosts: List<Host> = emptyList(),
    val problemServices: List<Service> = emptyList(),
) {
    val total: Int get() = problemHosts.size + problemServices.size
}

@HiltViewModel
class ProblemsViewModel @Inject constructor(
    private val repository: StatusRepository,
) : ViewModel() {

    val isRefreshing = MutableStateFlow(false)
    val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ProblemsUiState> =
        combine(repository.hosts, repository.services) { hosts, services ->
            ProblemsUiState(
                problemHosts = hosts.filter { it.status.isProblem }.sortedBy { it.name.lowercase() },
                problemServices = services.filter { it.status.isProblem }
                    .sortedWith(compareBy({ it.hostName.lowercase() }, { it.description.lowercase() })),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProblemsUiState())

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

    fun acknowledgeHost(host: Host, comment: String) {
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
