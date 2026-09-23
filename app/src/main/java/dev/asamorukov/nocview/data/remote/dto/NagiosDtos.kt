package dev.asamorukov.nocview.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Envelope<T>(
    @SerialName("format_version") val formatVersion: Int = 0,
    val result: ResultInfo = ResultInfo(),
    val data: T,
)

@Serializable
data class ResultInfo(
    @SerialName("query_time") val queryTime: Long = 0,
    val query: String = "",
    @SerialName("type_code") val typeCode: Int = 0,
    @SerialName("type_text") val typeText: String = "",
    val message: String = "",
    @SerialName("last_data_update") val lastDataUpdate: Long = 0,
)

@Serializable
data class CountData(
    val count: Map<String, Int> = emptyMap(),
)

@Serializable
data class HostListData(
    val hostlist: Map<String, HostDto> = emptyMap(),
)

@Serializable
data class ServiceListData(
    val servicelist: Map<String, Map<String, ServiceDto>> = emptyMap(),
)

@Serializable
data class HostData(
    val host: HostDto = HostDto(),
)

@Serializable
data class ServiceData(
    val service: ServiceDto = ServiceDto(),
)

@Serializable
data class ProgramStatusData(
    val programstatus: ProgramStatusDto = ProgramStatusDto(),
)

@Serializable
data class HostDto(
    val name: String = "",
    @SerialName("plugin_output") val pluginOutput: String = "",
    @SerialName("long_plugin_output") val longPluginOutput: String = "",
    @SerialName("perf_data") val perfData: String = "",
    val status: String = "",
    @SerialName("state_type") val stateType: String = "",
    @SerialName("check_type") val checkType: String = "",
    @SerialName("last_check") val lastCheck: Long = 0,
    @SerialName("next_check") val nextCheck: Long = 0,
    @SerialName("last_state_change") val lastStateChange: Long = 0,
    @SerialName("current_attempt") val currentAttempt: Int = 0,
    @SerialName("max_attempts") val maxAttempts: Int = 0,
    @SerialName("problem_has_been_acknowledged") val acknowledged: Boolean = false,
    @SerialName("scheduled_downtime_depth") val scheduledDowntimeDepth: Int = 0,
    @SerialName("is_flapping") val isFlapping: Boolean = false,
    @SerialName("notifications_enabled") val notificationsEnabled: Boolean = true,
    @SerialName("checks_enabled") val checksEnabled: Boolean = true,
    val latency: Double = 0.0,
    @SerialName("execution_time") val executionTime: Double = 0.0,
    @SerialName("last_time_up") val lastTimeUp: Long = 0,
    @SerialName("last_time_down") val lastTimeDown: Long = 0,
    @SerialName("last_time_unreachable") val lastTimeUnreachable: Long = 0,
)

@Serializable
data class ServiceDto(
    @SerialName("host_name") val hostName: String = "",
    val description: String = "",
    @SerialName("plugin_output") val pluginOutput: String = "",
    @SerialName("long_plugin_output") val longPluginOutput: String = "",
    @SerialName("perf_data") val perfData: String = "",
    val status: String = "",
    @SerialName("state_type") val stateType: String = "",
    @SerialName("check_type") val checkType: String = "",
    @SerialName("last_check") val lastCheck: Long = 0,
    @SerialName("next_check") val nextCheck: Long = 0,
    @SerialName("last_state_change") val lastStateChange: Long = 0,
    @SerialName("current_attempt") val currentAttempt: Int = 0,
    @SerialName("max_attempts") val maxAttempts: Int = 0,
    @SerialName("problem_has_been_acknowledged") val acknowledged: Boolean = false,
    @SerialName("scheduled_downtime_depth") val scheduledDowntimeDepth: Int = 0,
    @SerialName("is_flapping") val isFlapping: Boolean = false,
    @SerialName("notifications_enabled") val notificationsEnabled: Boolean = true,
    @SerialName("checks_enabled") val checksEnabled: Boolean = true,
    val latency: Double = 0.0,
    @SerialName("execution_time") val executionTime: Double = 0.0,
    @SerialName("last_time_ok") val lastTimeOk: Long = 0,
    @SerialName("last_time_warning") val lastTimeWarning: Long = 0,
    @SerialName("last_time_critical") val lastTimeCritical: Long = 0,
    @SerialName("last_time_unknown") val lastTimeUnknown: Long = 0,
)

@Serializable
data class ProgramStatusDto(
    val version: String = "",
    @SerialName("program_start") val programStart: Long = 0,
    @SerialName("last_log_rotation") val lastLogRotation: Long = 0,
)
