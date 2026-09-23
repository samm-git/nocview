package dev.sammgit.nocview.data.remote

import dev.sammgit.nocview.data.remote.dto.CountData
import dev.sammgit.nocview.data.remote.dto.HostDto
import dev.sammgit.nocview.data.remote.dto.ServiceDto
import dev.sammgit.nocview.domain.model.Host
import dev.sammgit.nocview.domain.model.HostCounts
import dev.sammgit.nocview.domain.model.HostStatus
import dev.sammgit.nocview.domain.model.Service
import dev.sammgit.nocview.domain.model.ServiceCounts
import dev.sammgit.nocview.domain.model.ServiceStatus

private fun Long?.zeroToNull(): Long? = this?.takeIf { it > 0L }

fun HostDto.toDomain(fallbackName: String = ""): Host = Host(
    name = name.ifBlank { fallbackName },
    status = HostStatus.fromApi(status),
    pluginOutput = pluginOutput,
    longPluginOutput = longPluginOutput,
    perfData = perfData,
    stateType = stateType,
    lastCheck = lastCheck.zeroToNull(),
    nextCheck = nextCheck.zeroToNull(),
    lastStateChange = lastStateChange.zeroToNull(),
    currentAttempt = currentAttempt,
    maxAttempts = maxAttempts,
    acknowledged = acknowledged,
    scheduledDowntimeDepth = scheduledDowntimeDepth,
    isFlapping = isFlapping,
    notificationsEnabled = notificationsEnabled,
    checksEnabled = checksEnabled,
    latency = latency,
    executionTime = executionTime,
    lastTimeUp = lastTimeUp.zeroToNull(),
    lastTimeDown = lastTimeDown.zeroToNull(),
    lastTimeUnreachable = lastTimeUnreachable.zeroToNull(),
)

fun ServiceDto.toDomain(
    fallbackHost: String = "",
    fallbackDescription: String = "",
): Service = Service(
    hostName = hostName.ifBlank { fallbackHost },
    description = description.ifBlank { fallbackDescription },
    status = ServiceStatus.fromApi(status),
    pluginOutput = pluginOutput,
    longPluginOutput = longPluginOutput,
    perfData = perfData,
    stateType = stateType,
    lastCheck = lastCheck.zeroToNull(),
    nextCheck = nextCheck.zeroToNull(),
    lastStateChange = lastStateChange.zeroToNull(),
    currentAttempt = currentAttempt,
    maxAttempts = maxAttempts,
    acknowledged = acknowledged,
    scheduledDowntimeDepth = scheduledDowntimeDepth,
    isFlapping = isFlapping,
    notificationsEnabled = notificationsEnabled,
    checksEnabled = checksEnabled,
    latency = latency,
    executionTime = executionTime,
    lastTimeOk = lastTimeOk.zeroToNull(),
    lastTimeWarning = lastTimeWarning.zeroToNull(),
    lastTimeCritical = lastTimeCritical.zeroToNull(),
    lastTimeUnknown = lastTimeUnknown.zeroToNull(),
)

fun CountData.toHostCounts(): HostCounts = HostCounts(
    up = count["up"] ?: 0,
    down = count["down"] ?: 0,
    unreachable = count["unreachable"] ?: 0,
    pending = count["pending"] ?: 0,
)

fun CountData.toServiceCounts(): ServiceCounts = ServiceCounts(
    ok = count["ok"] ?: 0,
    warning = count["warning"] ?: 0,
    critical = count["critical"] ?: 0,
    unknown = count["unknown"] ?: 0,
    pending = count["pending"] ?: 0,
)
