package dev.sammgit.nocview.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dagger.hilt.android.EntryPointAccessors
import dev.sammgit.nocview.MainActivity
import dev.sammgit.nocview.data.local.MetaEntity
import dev.sammgit.nocview.ui.components.formatRelative

private val Background = ColorProvider(Color(0xFF11161C))
private val TitleColor = ColorProvider(Color(0xFF8AB4F8))
private val PrimaryText = ColorProvider(Color(0xFFECEFF1))
private val MutedText = ColorProvider(Color(0xFF90A4AE))
private val OkColor = ColorProvider(Color(0xFF4CAF50))
private val ProblemColor = ColorProvider(Color(0xFFE53935))

class StatusWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        val meta = runCatching { entryPoint.statusRepository().metaSnapshot() }.getOrNull()
        provideContent { WidgetContent(meta) }
    }
}

@Composable
private fun WidgetContent(meta: MetaEntity?) {
    val size = LocalSize.current
    val compact = size.height < 100.dp

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Background)
            .cornerRadius(16.dp)
            .padding(if (compact) 10.dp else 14.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (meta == null) {
            if (!compact) {
                Text(
                    text = "NOC View",
                    style = TextStyle(
                        color = TitleColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Spacer(GlanceModifier.height(4.dp))
            }
            Text(
                text = "Not configured",
                style = TextStyle(color = PrimaryText, fontSize = 18.sp, fontWeight = FontWeight.Bold),
            )
            if (!compact) {
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = "Tap to open the app and connect",
                    style = TextStyle(color = MutedText, fontSize = 12.sp),
                )
            }
        } else {
            val hostTotal = meta.hostUp + meta.hostDown + meta.hostUnreachable + meta.hostPending
            val serviceTotal = meta.svcOk + meta.svcWarning + meta.svcCritical + meta.svcUnknown + meta.svcPending
            val hostProblems = meta.hostDown + meta.hostUnreachable
            val serviceProblems = meta.svcWarning + meta.svcCritical + meta.svcUnknown
            val problems = hostProblems + serviceProblems

            if (!compact) {
                Text(
                    text = "NOC View",
                    style = TextStyle(
                        color = TitleColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Spacer(GlanceModifier.height(4.dp))
            }
            Text(
                text = "$hostTotal hosts · $serviceTotal services",
                style = TextStyle(color = PrimaryText, fontSize = 18.sp, fontWeight = FontWeight.Bold),
            )
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = "$problems problems · ${formatRelative(meta.lastUpdated)}",
                style = TextStyle(
                    color = if (problems > 0) ProblemColor else OkColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
    }
}
