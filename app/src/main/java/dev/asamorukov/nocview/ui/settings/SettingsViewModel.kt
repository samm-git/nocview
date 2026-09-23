package dev.asamorukov.nocview.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.asamorukov.nocview.data.repo.StatusRepository
import dev.asamorukov.nocview.data.settings.ServerSettings
import dev.asamorukov.nocview.data.settings.SettingsStore
import dev.asamorukov.nocview.ui.common.friendlyError
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsStore: SettingsStore,
    private val repository: StatusRepository,
) : ViewModel() {

    val settings: StateFlow<ServerSettings> = settingsStore.settings

    private val initial = settingsStore.settings.value

    private val _baseUrl = MutableStateFlow(initial.baseUrl)
    val baseUrl: StateFlow<String> = _baseUrl.asStateFlow()

    private val _username = MutableStateFlow(initial.username)
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow(initial.password)
    val password: StateFlow<String> = _password.asStateFlow()

    private val _trustSelfSigned = MutableStateFlow(initial.trustSelfSigned)
    val trustSelfSigned: StateFlow<Boolean> = _trustSelfSigned.asStateFlow()

    private val _autoRefreshSeconds = MutableStateFlow(initial.autoRefreshSeconds)
    val autoRefreshSeconds: StateFlow<Int> = _autoRefreshSeconds.asStateFlow()

    private val _isTesting = MutableStateFlow(false)
    val isTesting: StateFlow<Boolean> = _isTesting.asStateFlow()

    private val _testResult = MutableStateFlow<String?>(null)
    val testResult: StateFlow<String?> = _testResult.asStateFlow()

    private val _testError = MutableStateFlow<String?>(null)
    val testError: StateFlow<String?> = _testError.asStateFlow()

    private val _savedMessage = MutableStateFlow<String?>(null)
    val savedMessage: StateFlow<String?> = _savedMessage.asStateFlow()

    fun onBaseUrlChange(value: String) { _baseUrl.value = value }
    fun onUsernameChange(value: String) { _username.value = value }
    fun onPasswordChange(value: String) { _password.value = value }
    fun onTrustSelfSignedChange(value: Boolean) { _trustSelfSigned.value = value }
    fun onAutoRefreshChange(value: Int) { _autoRefreshSeconds.value = value }

    private fun currentSettings(): ServerSettings = ServerSettings(
        baseUrl = _baseUrl.value.trim(),
        username = _username.value.trim(),
        password = _password.value,
        trustSelfSigned = _trustSelfSigned.value,
        autoRefreshSeconds = _autoRefreshSeconds.value,
    )

    fun save() {
        settingsStore.save(currentSettings())
        _savedMessage.value = "Settings saved."
    }

    fun testConnection() {
        settingsStore.save(currentSettings())
        viewModelScope.launch {
            _isTesting.value = true
            _testResult.value = null
            _testError.value = null
            repository.testConnection()
                .onSuccess { _testResult.value = it }
                .onFailure { _testError.value = friendlyError(it) }
            _isTesting.value = false
        }
    }

    fun clearMessages() {
        _testResult.value = null
        _testError.value = null
        _savedMessage.value = null
    }
}
