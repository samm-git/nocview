package dev.asamorukov.nocview.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.asamorukov.nocview.domain.model.Host
import dev.asamorukov.nocview.domain.model.Service
import dev.asamorukov.nocview.ui.theme.StatusBlue
import dev.asamorukov.nocview.ui.theme.StatusGray
import dev.asamorukov.nocview.ui.theme.hostStatusColor
import dev.asamorukov.nocview.ui.theme.serviceStatusColor

@Composable
fun HostRow(
    host: Host,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onAck: (() -> Unit)? = null,
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusDot(hostStatusColor(host.status))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = host.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = host.pluginOutput.ifBlank { "—" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (host.acknowledged || host.inDowntime || host.isFlapping) {
                    Spacer(Modifier.size(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (host.acknowledged) StatusPill("ACK", StatusBlue)
                        if (host.inDowntime) StatusPill("DOWNTIME", StatusGray)
                        if (host.isFlapping) StatusPill("FLAPPING", StatusGray)
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            StatusPill(host.status.name, hostStatusColor(host.status))
            if (onAck != null) {
                TextButton(
                    onClick = onAck,
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) {
                    Text("ACK")
                }
            }
        }
    }
}

@Composable
fun ServiceRow(
    service: Service,
    onClick: (() -> Unit)? = null,
    showHost: Boolean = true,
    onAck: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val cardModifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 4.dp)

    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusDot(serviceStatusColor(service.status))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.description,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (showHost) {
                    Text(
                        text = service.hostName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = service.pluginOutput.ifBlank { "—" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (service.acknowledged || service.inDowntime || service.isFlapping) {
                    Spacer(Modifier.size(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (service.acknowledged) StatusPill("ACK", StatusBlue)
                        if (service.inDowntime) StatusPill("DOWNTIME", StatusGray)
                        if (service.isFlapping) StatusPill("FLAPPING", StatusGray)
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            StatusPill(service.status.name, serviceStatusColor(service.status))
            if (onAck != null) {
                TextButton(
                    onClick = onAck,
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) {
                    Text("ACK")
                }
            }
        }
    }

    if (onClick != null) {
        Card(onClick = onClick, modifier = cardModifier, content = { content() })
    } else {
        Card(modifier = cardModifier, content = { content() })
    }
}
