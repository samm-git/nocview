package dev.sammgit.nocview.ui.servicedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sammgit.nocview.data.repo.StatusRepository
import dev.sammgit.nocview.domain.model.Service
import dev.sammgit.nocview.ui.common.friendlyError
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ServiceDetailViewModel @Inject constructor(
    private val repository: StatusRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val hostName: String = savedStateHandle.get<String>("hostName").orEmpty()
    val serviceDescription: String =
        savedStateHandle.get<String>("serviceDescription").orEmpty()

    val isRefreshing = MutableStateFlow(false)
    val message = MutableStateFlow<String?>(null)

    val service: StateFlow<Service?> =
        repository.service(hostName, serviceDescription)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

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

    fun acknowledgeService(comment: String) {
        val service = service.value ?: return
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
