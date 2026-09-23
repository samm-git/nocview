package dev.sammgit.nocview.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sammgit.nocview.data.repo.StatusRepository
import dev.sammgit.nocview.data.settings.ServerSettings
import dev.sammgit.nocview.data.settings.SettingsStore
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: StatusRepository,
    settingsStore: SettingsStore,
) : ViewModel() {

    val settings: StateFlow<ServerSettings> = settingsStore.settings

    fun refresh() {
        viewModelScope.launch { repository.refresh() }
    }
}
