package dev.sammgit.nocview.ui.common

import dev.sammgit.nocview.domain.model.HostFilter
import dev.sammgit.nocview.domain.model.ServiceFilter
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class StatusFilterStore @Inject constructor() {

    private val _hostFilter = MutableStateFlow(HostFilter.ALL)
    val hostFilter: StateFlow<HostFilter> = _hostFilter.asStateFlow()

    private val _serviceFilter = MutableStateFlow(ServiceFilter.ALL)
    val serviceFilter: StateFlow<ServiceFilter> = _serviceFilter.asStateFlow()

    fun selectHostFilter(value: HostFilter) {
        _hostFilter.value = if (_hostFilter.value == value) HostFilter.ALL else value
    }

    fun selectServiceFilter(value: ServiceFilter) {
        _serviceFilter.value = if (_serviceFilter.value == value) ServiceFilter.ALL else value
    }
}
