package dev.sammgit.nocview.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sammgit.nocview.domain.model.HostCounts
import dev.sammgit.nocview.domain.model.HostFilter
import dev.sammgit.nocview.domain.model.ServiceCounts
import dev.sammgit.nocview.domain.model.ServiceFilter
import dev.sammgit.nocview.ui.theme.StatusAmber
import dev.sammgit.nocview.ui.theme.StatusGray
import dev.sammgit.nocview.ui.theme.StatusGreen
import dev.sammgit.nocview.ui.theme.StatusPurple
import dev.sammgit.nocview.ui.theme.StatusRed
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun StatusDot(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
fun StatusPill(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun SummaryHeader(
    hostCounts: HostCounts,
    serviceCounts: ServiceCounts,
    lastUpdated: Long?,
    hostFilter: HostFilter,
    serviceFilter: ServiceFilter,
    onHostStateClick: (HostFilter) -> Unit,
    onServiceStateClick: (ServiceFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Hosts",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${hostCounts.total} total · ${hostCounts.problems} problem",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.size(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBox(
                    label = "Up",
                    value = hostCounts.up,
                    color = StatusGreen,
                    selected = hostFilter == HostFilter.UP,
                    onClick = { onHostStateClick(HostFilter.UP) },
                    modifier = Modifier.weight(1f),
                )
                StatusBox(
                    label = "Down",
                    value = hostCounts.down,
                    color = problemColor(hostCounts.down, StatusRed),
                    selected = hostFilter == HostFilter.DOWN,
                    onClick = { onHostStateClick(HostFilter.DOWN) },
                    modifier = Modifier.weight(1f),
                )
                StatusBox(
                    label = "Unreach",
                    value = hostCounts.unreachable,
                    color = problemColor(hostCounts.unreachable, StatusAmber),
                    selected = hostFilter == HostFilter.UNREACHABLE,
                    onClick = { onHostStateClick(HostFilter.UNREACHABLE) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.size(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Services",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${serviceCounts.total} total · ${serviceCounts.problems} problem",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.size(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBox(
                    label = "Ok",
                    value = serviceCounts.ok,
                    color = StatusGreen,
                    selected = serviceFilter == ServiceFilter.OK,
                    onClick = { onServiceStateClick(ServiceFilter.OK) },
                    modifier = Modifier.weight(1f),
                )
                StatusBox(
                    label = "Warn",
                    value = serviceCounts.warning,
                    color = problemColor(serviceCounts.warning, StatusAmber),
                    selected = serviceFilter == ServiceFilter.WARNING,
                    onClick = { onServiceStateClick(ServiceFilter.WARNING) },
                    modifier = Modifier.weight(1f),
                )
                StatusBox(
                    label = "Crit",
                    value = serviceCounts.critical,
                    color = problemColor(serviceCounts.critical, StatusRed),
                    selected = serviceFilter == ServiceFilter.CRITICAL,
                    onClick = { onServiceStateClick(ServiceFilter.CRITICAL) },
                    modifier = Modifier.weight(1f),
                )
                StatusBox(
                    label = "Unk",
                    value = serviceCounts.unknown,
                    color = problemColor(serviceCounts.unknown, StatusPurple),
                    selected = serviceFilter == ServiceFilter.UNKNOWN,
                    onClick = { onServiceStateClick(ServiceFilter.UNKNOWN) },
                    modifier = Modifier.weight(1f),
                )
            }

            if (lastUpdated != null && lastUpdated > 0) {
                Spacer(Modifier.size(8.dp))
                Text(
                    text = "Updated ${formatTimestamp(lastUpdated)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun problemColor(value: Int, color: Color): Color =
    if (value > 0) color else StatusGray

@Composable
private fun StatusBox(
    label: String,
    value: Int,
    color: Color,
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(10.dp)
    val contentColor = if (color.luminance() > 0.5f) Color.Black else Color.White
    Box(
        modifier = modifier
            .clip(shape)
            .background(color)
            .then(
                if (selected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, shape)
                } else {
                    Modifier
                },
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.9f),
            )
        }
    }
}

@Composable
fun KeyValueRow(key: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = key,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun OutputText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.ifBlank { "—" },
        style = MaterialTheme.typography.bodyMedium,
        fontFamily = FontFamily.Monospace,
        modifier = modifier,
    )
}

private val timestampFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

fun formatTimestamp(epochMs: Long?): String {
    if (epochMs == null || epochMs <= 0) return "—"
    return Instant.ofEpochMilli(epochMs)
        .atZone(ZoneId.systemDefault())
        .format(timestampFormatter)
}

fun formatRelative(epochMs: Long?): String {
    if (epochMs == null || epochMs <= 0) return "never"
    val diff = System.currentTimeMillis() - epochMs
    if (diff < 0) return "in ${humanize(-diff)}"
    return "${humanize(diff)} ago"
}

private fun humanize(millis: Long): String {
    val seconds = millis / 1000
    return when {
        seconds < 60 -> "${seconds}s"
        seconds < 3600 -> "${seconds / 60}m"
        seconds < 86400 -> "${seconds / 3600}h"
        else -> "${seconds / 86400}d"
    }
}
