package dev.sammgit.nocview.domain.model

enum class HostStatus {
    UP,
    DOWN,
    UNREACHABLE,
    PENDING,
    UNKNOWN;

    val isProblem: Boolean
        get() = this == DOWN || this == UNREACHABLE || this == UNKNOWN

    companion object {
        fun fromApi(raw: String?): HostStatus = when (raw?.trim()?.lowercase()) {
            "up" -> UP
            "down" -> DOWN
            "unreachable" -> UNREACHABLE
            "pending" -> PENDING
            else -> UNKNOWN
        }
    }
}

enum class ServiceStatus {
    OK,
    WARNING,
    CRITICAL,
    UNKNOWN,
    PENDING;

    val isProblem: Boolean
        get() = this == WARNING || this == CRITICAL || this == UNKNOWN

    companion object {
        fun fromApi(raw: String?): ServiceStatus = when (raw?.trim()?.lowercase()) {
            "ok" -> OK
            "warning" -> WARNING
            "critical" -> CRITICAL
            "pending" -> PENDING
            else -> UNKNOWN
        }
    }
}

data class Host(
    val name: String,
    val status: HostStatus,
    val pluginOutput: String,
    val longPluginOutput: String,
    val perfData: String,
    val stateType: String,
    val lastCheck: Long?,
    val nextCheck: Long?,
    val lastStateChange: Long?,
    val currentAttempt: Int,
    val maxAttempts: Int,
    val acknowledged: Boolean,
    val scheduledDowntimeDepth: Int,
    val isFlapping: Boolean,
    val notificationsEnabled: Boolean,
    val checksEnabled: Boolean,
    val latency: Double?,
    val executionTime: Double?,
    val lastTimeUp: Long?,
    val lastTimeDown: Long?,
    val lastTimeUnreachable: Long?,
) {
    val inDowntime: Boolean get() = scheduledDowntimeDepth > 0
}

data class Service(
    val hostName: String,
    val description: String,
    val status: ServiceStatus,
    val pluginOutput: String,
    val longPluginOutput: String,
    val perfData: String,
    val stateType: String,
    val lastCheck: Long?,
    val nextCheck: Long?,
    val lastStateChange: Long?,
    val currentAttempt: Int,
    val maxAttempts: Int,
    val acknowledged: Boolean,
    val scheduledDowntimeDepth: Int,
    val isFlapping: Boolean,
    val notificationsEnabled: Boolean,
    val checksEnabled: Boolean,
    val latency: Double?,
    val executionTime: Double?,
    val lastTimeOk: Long?,
    val lastTimeWarning: Long?,
    val lastTimeCritical: Long?,
    val lastTimeUnknown: Long?,
) {
    val inDowntime: Boolean get() = scheduledDowntimeDepth > 0
    val key: String get() = "$hostName/$description"
}

data class HostCounts(
    val up: Int = 0,
    val down: Int = 0,
    val unreachable: Int = 0,
    val pending: Int = 0,
) {
    val total: Int get() = up + down + unreachable + pending
    val problems: Int get() = down + unreachable
}

data class ServiceCounts(
    val ok: Int = 0,
    val warning: Int = 0,
    val critical: Int = 0,
    val unknown: Int = 0,
    val pending: Int = 0,
) {
    val total: Int get() = ok + warning + critical + unknown + pending
    val problems: Int get() = warning + critical + unknown
}

data class StatusSnapshot(
    val lastUpdated: Long?,
    val programVersion: String,
    val hostCounts: HostCounts,
    val serviceCounts: ServiceCounts,
)
