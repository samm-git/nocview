package dev.asamorukov.nocview.data.settings

data class ServerSettings(
    val baseUrl: String = DEFAULT_BASE_URL,
    val username: String = "",
    val password: String = "",
    val trustSelfSigned: Boolean = false,
    val autoRefreshSeconds: Int = 0,
) {
    val isConfigured: Boolean get() = baseUrl.isNotBlank()

    companion object {
        const val DEFAULT_BASE_URL = "http://example.com/nagios"
    }
}
