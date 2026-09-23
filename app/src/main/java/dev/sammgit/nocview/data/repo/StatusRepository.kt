package dev.sammgit.nocview.data.repo

import androidx.room.withTransaction
import dev.sammgit.nocview.data.local.MetaEntity
import dev.sammgit.nocview.data.local.NocViewDatabase
import dev.sammgit.nocview.data.local.toDomain as entityToDomain
import dev.sammgit.nocview.data.local.toEntity
import dev.sammgit.nocview.data.remote.ApiProvider
import dev.sammgit.nocview.data.remote.toDomain as dtoToDomain
import dev.sammgit.nocview.data.remote.toHostCounts
import dev.sammgit.nocview.data.remote.toServiceCounts
import dev.sammgit.nocview.data.settings.SettingsStore
import dev.sammgit.nocview.domain.model.Host
import dev.sammgit.nocview.domain.model.Service
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.ResponseBody
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class StatusRepository @Inject constructor(
    private val apiProvider: ApiProvider,
    private val database: NocViewDatabase,
    private val settingsStore: SettingsStore,
) {
    private val dao = database.statusDao()

    val hosts: Flow<List<Host>> =
        dao.observeHosts().map { entities -> entities.map { it.entityToDomain() } }

    val services: Flow<List<Service>> =
        dao.observeServices().map { entities -> entities.map { it.entityToDomain() } }

    val meta: Flow<MetaEntity?> = dao.observeMeta()

    suspend fun metaSnapshot(): MetaEntity? = dao.metaNow()

    fun host(name: String): Flow<Host?> =
        dao.observeHost(name).map { it?.entityToDomain() }

    fun servicesForHost(hostName: String): Flow<List<Service>> =
        dao.observeServicesForHost(hostName).map { entities -> entities.map { it.entityToDomain() } }

    fun service(hostName: String, description: String): Flow<Service?> =
        dao.observeService(hostName, description).map { it?.entityToDomain() }

    suspend fun refresh(): Result<Unit> = runCatching {
        val api = apiProvider.api()
        coroutineScope {
            val hostCountDeferred = async { api.hostCount() }
            val serviceCountDeferred = async { api.serviceCount() }
            val hostListDeferred = async { api.hostList() }
            val serviceListDeferred = async { api.serviceList() }
            val programDeferred = async { api.programStatus() }

            val hostCounts = hostCountDeferred.await().data.toHostCounts()
            val serviceCounts = serviceCountDeferred.await().data.toServiceCounts()
            val hostList = hostListDeferred.await().data.hostlist
            val serviceList = serviceListDeferred.await().data.servicelist
            val program = programDeferred.await().data.programstatus

            val hostEntities = hostList.map { (name, dto) -> dto.dtoToDomain(name).toEntity() }
            val serviceEntities = serviceList.flatMap { (host, services) ->
                services.map { (description, dto) ->
                    dto.dtoToDomain(host, description).toEntity()
                }
            }

            val now = System.currentTimeMillis()
            database.withTransaction {
                dao.clearHosts()
                dao.clearServices()
                dao.upsertHosts(hostEntities)
                dao.upsertServices(serviceEntities)
                dao.upsertMeta(
                    MetaEntity(
                        id = 0,
                        lastUpdated = now,
                        programVersion = program.version,
                        hostUp = hostCounts.up,
                        hostDown = hostCounts.down,
                        hostUnreachable = hostCounts.unreachable,
                        hostPending = hostCounts.pending,
                        svcOk = serviceCounts.ok,
                        svcWarning = serviceCounts.warning,
                        svcCritical = serviceCounts.critical,
                        svcUnknown = serviceCounts.unknown,
                        svcPending = serviceCounts.pending,
                    ),
                )
            }
        }
    }

    suspend fun testConnection(): Result<String> = runCatching {
        val api = apiProvider.api()
        val response = api.hostCount()
        if (response.result.typeCode != 0) {
            error(response.result.message.ifBlank { response.result.typeText })
        }
        val version = api.programStatus().data.programstatus.version
        val up = response.data.count["up"] ?: 0
        val total = response.data.count.values.sum()
        "Connected to monitoring core $version — $up of $total hosts up."
    }

    suspend fun acknowledgeHost(name: String, comment: String): Result<Unit> = runCatching {
        submitCommand(cmdType = ACK_HOST, host = name, service = null) { formId ->
            apiProvider.api().acknowledgeHost(
                cmdType = ACK_HOST,
                cmdMod = COMMIT,
                nagFormId = formId,
                host = name,
                author = authorName(),
                comment = comment,
                sticky = 1,
                notify = 1,
                persistent = 1,
            )
        }
    }

    suspend fun acknowledgeService(host: String, service: String, comment: String): Result<Unit> =
        runCatching {
            submitCommand(cmdType = ACK_SERVICE, host = host, service = service) { formId ->
                apiProvider.api().acknowledgeService(
                    cmdType = ACK_SERVICE,
                    cmdMod = COMMIT,
                    nagFormId = formId,
                    host = host,
                    service = service,
                    author = authorName(),
                    comment = comment,
                    sticky = 1,
                    notify = 1,
                    persistent = 1,
                )
            }
        }

    private suspend fun submitCommand(
        cmdType: Int,
        host: String,
        service: String?,
        request: suspend (String) -> ResponseBody,
    ) {
        val api = apiProvider.api()
        api.commandForm(cmdType = cmdType, host = host, service = service).string()
        val formId = apiProvider.formId()
            ?: error("Could not obtain a command session. Check your account permissions.")
        val body = request(formId).string()
        if (!body.contains("successfully submitted", ignoreCase = true)) {
            error(parseCommandError(body))
        }
        refresh()
    }

    private fun authorName(): String =
        settingsStore.settings.value.username.ifBlank { "NOC View" }

    private fun parseCommandError(html: String): String {
        val lower = html.lowercase()
        if ("not authorized" in lower || "access denied" in lower) {
            return "Not authorized to submit commands. Check your account permissions."
        }
        val match = errorDivRegex.find(html)
        val raw = match?.groupValues?.getOrNull(1) ?: html
        val text = raw.replace(tagRegex, " ").replace(whitespaceRegex, " ").trim()
        return text.take(200).ifBlank { "Command failed." }
    }

    private companion object {
        const val ACK_HOST = 33
        const val ACK_SERVICE = 34
        const val COMMIT = 2
        val errorDivRegex = Regex(
            """<div class="error(?:Message)?">(.*?)</div>""",
            RegexOption.DOT_MATCHES_ALL,
        )
        val tagRegex = Regex("<[^>]+>")
        val whitespaceRegex = Regex("\\s+")
    }
}
