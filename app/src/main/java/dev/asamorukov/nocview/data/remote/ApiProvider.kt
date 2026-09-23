package dev.asamorukov.nocview.data.remote

import dev.asamorukov.nocview.data.settings.ServerSettings
import dev.asamorukov.nocview.data.settings.SettingsStore
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.Credentials
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Singleton
class ApiProvider @Inject constructor(
    private val settingsStore: SettingsStore,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    private data class ClientHandle(
        val settings: ServerSettings,
        val api: NagiosApi,
        val cookies: InMemoryCookieJar,
    )

    @Volatile
    private var cache: ClientHandle? = null

    fun api(): NagiosApi = handle().api

    fun formId(): String? = handle().cookies.value("NagFormId")

    private fun handle(): ClientHandle {
        val current = settingsStore.settings.value
        cache?.let { if (it.settings == current) return it }
        return synchronized(this) {
            cache?.let { if (it.settings == current) return it }
            build(current).also { cache = it }
        }
    }

    private fun build(settings: ServerSettings): ClientHandle {
        val cookies = InMemoryCookieJar()
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .cookieJar(cookies)
            .addInterceptor(BasicAuthInterceptor(settings.username, settings.password))
            .apply { if (settings.trustSelfSigned) Tls.applyInsecure(this) }
            .build()

        val api = Retrofit.Builder()
            .baseUrl(normalizeBaseUrl(settings.baseUrl))
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(NagiosApi::class.java)

        return ClientHandle(settings, api, cookies)
    }

    companion object {
        fun normalizeBaseUrl(raw: String): String {
            var value = raw.trim()
            if (value.isEmpty()) return "http://localhost/"
            if (!value.startsWith("http://") && !value.startsWith("https://")) {
                value = "http://$value"
            }
            if (!value.endsWith("/")) value += "/"
            return value
        }
    }
}

private class InMemoryCookieJar : CookieJar {
    private val store = ConcurrentHashMap<String, Cookie>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookies.forEach { store[it.name] = it }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> = store.values.toList()

    fun value(name: String): String? = store[name]?.value
}

private class BasicAuthInterceptor(
    private val username: String,
    private val password: String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (username.isEmpty() && password.isEmpty()) return chain.proceed(request)
        val token = Credentials.basic(username, password, Charsets.UTF_8)
        return chain.proceed(
            request.newBuilder().header("Authorization", token).build(),
        )
    }
}
