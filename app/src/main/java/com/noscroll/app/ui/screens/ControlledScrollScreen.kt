package com.noscroll.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.noscroll.app.scroll.ControlledScrollConfig
import com.noscroll.app.ui.AppUiState
import com.noscroll.app.ui.ControlledAppToggleState
import com.noscroll.app.ui.components.AppToggleCard
import com.noscroll.app.ui.components.HeroStatusCard
import com.noscroll.app.ui.components.MasterToggleCard
import com.noscroll.app.ui.components.SectionHeader
import java.text.DateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

@Composable
fun ControlledScrollScreen(
    state: AppUiState,
    onOpenAccessibilitySettings: () -> Unit,
    onProtectionToggled: (Boolean) -> Unit,
    onAppToggled: (String, Boolean) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val warnMin = TimeUnit.MILLISECONDS.toMinutes(ControlledScrollConfig.WARN_AFTER_MS)
    val blockMin = TimeUnit.MILLISECONDS.toMinutes(ControlledScrollConfig.BLOCK_AFTER_MS)
    val coolMin = TimeUnit.MILLISECONDS.toMinutes(ControlledScrollConfig.COOLDOWN_MS)
    val now = System.currentTimeMillis()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            HeroStatusCard(
                accessibilityEnabled = state.accessibilityEnabled,
                protectionEnabled = state.controlledScrollEnabled,
                blockCount = state.controlledBlockCount,
                activeLabel = "Scroll limited",
                idleLabel = "Setup needed",
                pausedLabel = "Limits paused",
                activeDescription = "Active scrolling is timed. Warning at ${warnMin}m, lock at ${blockMin}m, then ${coolMin}m cooldown.",
                idleDescription = "Enable accessibility once so No Scroll can time active scrolling in the background.",
                pausedDescription = "Service is ready, but controlled scrolling is paused. Turn it back on below.",
                onOpenAccessibilitySettings = onOpenAccessibilitySettings,
                onRefresh = onRefresh,
            )
        }

        item {
            MasterToggleCard(
                title = "Controlled scrolling",
                subtitle = buildString {
                    append("Warn at ${warnMin}m · lock at ${blockMin}m · cooldown ${coolMin}m")
                    if (!ControlledScrollConfig.isProduction) append(" · DEV")
                },
                enabled = state.controlledScrollEnabled,
                interactionEnabled = state.accessibilityEnabled,
                onToggled = onProtectionToggled,
            )
        }

        item {
            RulesCard(
                warnMin = warnMin,
                blockMin = blockMin,
                coolMin = coolMin,
            )
        }

        item {
            SectionHeader(
                title = "Apps to limit",
                subtitle = "Locked apps stay on until their cooldown ends",
            )
        }

        items(state.controlledApps, key = { it.app.id }) { item ->
            val locked = item.blockedUntilMs > now
            val status = controlledStatusMessage(
                item = item,
                now = now,
                warnMin = warnMin,
                blockMin = blockMin,
                coolMin = coolMin,
            )

            AppToggleCard(
                title = item.app.displayName,
                subtitle = status.message,
                accent = Color(item.app.accentHex),
                checked = if (locked) true else item.enabled,
                // While locked, switch cannot be turned off.
                enabled = state.accessibilityEnabled &&
                    state.controlledScrollEnabled &&
                    !locked,
                onToggled = { enabled ->
                    if (locked && !enabled) return@AppToggleCard
                    onAppToggled(item.app.id, enabled)
                },
                highlightSubtitle = locked,
                badgeText = if (locked) "Locked" else null,
            )
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

private data class ControlledStatus(
    val message: String,
)

private fun controlledStatusMessage(
    item: ControlledAppToggleState,
    now: Long,
    warnMin: Long,
    blockMin: Long,
    coolMin: Long,
): ControlledStatus {
    val remaining = (item.blockedUntilMs - now).coerceAtLeast(0L)
    if (remaining > 0L) {
        val availableAt = DateFormat.getTimeInstance(DateFormat.SHORT)
            .format(Date(item.blockedUntilMs))
        val remainingMin = TimeUnit.MILLISECONDS.toMinutes(remaining).coerceAtLeast(1)
        return ControlledStatus(
            message = "You scrolled for $blockMin min, so this app is blocked for $coolMin min. " +
                "Available again at $availableAt (about ${remainingMin}m left).",
        )
    }

    val usedMin = TimeUnit.MILLISECONDS.toMinutes(item.accumulatedMs)
    if (item.accumulatedMs > 0L) {
        val usedLabel = if (usedMin < 1L) "less than 1" else usedMin.toString()
        return ControlledStatus(
            message = "Session so far: $usedLabel min scrolled. Warning at $warnMin min, lock at $blockMin min.",
        )
    }

    return ControlledStatus(
        message = "Limit scrolling in ${item.app.displayName}",
    )
}

@Composable
private fun RulesCard(
    warnMin: Long,
    blockMin: Long,
    coolMin: Long,
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "How it works",
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "• Counts active scrolling (not idle watching/chatting)\n" +
                    "• Pauses after 1 minute without scrolling\n" +
                    "• Resets the budget after 30 minutes away\n" +
                    "• Warns at ${warnMin}m, locks at ${blockMin}m for ${coolMin}m\n" +
                    "• While locked, that app toggle stays on and can’t be turned off",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}
