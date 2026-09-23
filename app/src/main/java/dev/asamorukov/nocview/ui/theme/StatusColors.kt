package dev.asamorukov.nocview.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import dev.asamorukov.nocview.domain.model.HostStatus
import dev.asamorukov.nocview.domain.model.ServiceStatus

@Composable
@ReadOnlyComposable
fun hostStatusColor(status: HostStatus): Color = when (status) {
    HostStatus.UP -> StatusGreen
    HostStatus.DOWN -> StatusRed
    HostStatus.UNREACHABLE -> StatusAmber
    HostStatus.PENDING -> StatusGray
    HostStatus.UNKNOWN -> StatusPurple
}

@Composable
@ReadOnlyComposable
fun serviceStatusColor(status: ServiceStatus): Color = when (status) {
    ServiceStatus.OK -> StatusGreen
    ServiceStatus.WARNING -> StatusAmber
    ServiceStatus.CRITICAL -> StatusRed
    ServiceStatus.UNKNOWN -> StatusPurple
    ServiceStatus.PENDING -> StatusGray
}

@Composable
@ReadOnlyComposable
fun hostStatusLabel(status: HostStatus): String = status.name

@Composable
@ReadOnlyComposable
fun serviceStatusLabel(status: ServiceStatus): String = status.name

@Composable
@ReadOnlyComposable
fun summaryAccent(): Color = MaterialTheme.colorScheme.primary
