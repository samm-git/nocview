package dev.asamorukov.nocview.data.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences = createPrefs(context)

    private val _settings = MutableStateFlow(read())
    val settings: StateFlow<ServerSettings> = _settings.asStateFlow()

    fun save(settings: ServerSettings) {
        prefs.edit()
            .putString(KEY_BASE_URL, settings.baseUrl)
            .putString(KEY_USERNAME, settings.username)
            .putString(KEY_PASSWORD, settings.password)
            .putBoolean(KEY_TRUST_SELF_SIGNED, settings.trustSelfSigned)
            .putInt(KEY_AUTO_REFRESH, settings.autoRefreshSeconds)
            .apply()
        _settings.value = settings
    }

    private fun read(): ServerSettings = ServerSettings(
        baseUrl = prefs.getString(KEY_BASE_URL, ServerSettings.DEFAULT_BASE_URL)
            ?: ServerSettings.DEFAULT_BASE_URL,
        username = prefs.getString(KEY_USERNAME, "") ?: "",
        password = prefs.getString(KEY_PASSWORD, "") ?: "",
        trustSelfSigned = prefs.getBoolean(KEY_TRUST_SELF_SIGNED, false),
        autoRefreshSeconds = prefs.getInt(KEY_AUTO_REFRESH, 0),
    )

    private fun createPrefs(context: Context): SharedPreferences = runCatching {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }.getOrElse {
        context.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE)
    }

    private companion object {
        const val PREFS_NAME = "nocview_secure_prefs"
        const val FALLBACK_PREFS_NAME = "nocview_prefs"
        const val KEY_BASE_URL = "base_url"
        const val KEY_USERNAME = "username"
        const val KEY_PASSWORD = "password"
        const val KEY_TRUST_SELF_SIGNED = "trust_self_signed"
        const val KEY_AUTO_REFRESH = "auto_refresh_seconds"
    }
}
