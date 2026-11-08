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
import com.noscroll.app.ui.components.AppToggleCard
import com.noscroll.app.ui.components.HeroStatusCard
import com.noscroll.app.ui.components.MasterToggleCard
import com.noscroll.app.ui.components.SectionHeader
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
                subtitle = "Warn at ${warnMin}m · lock at ${blockMin}m · cooldown ${coolMin}m",
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
                subtitle = "Hard Social Block apps are skipped automatically",
            )
        }

        items(state.controlledApps, key = { it.app.id }) { item ->
            val remaining = (item.blockedUntilMs - System.currentTimeMillis()).coerceAtLeast(0L)
            val usedMin = TimeUnit.MILLISECONDS.toMinutes(item.accumulatedMs)
            val statusLine = when {
                remaining > 0L ->
                    "Locked · ${TimeUnit.MILLISECONDS.toMinutes(remaining).coerceAtLeast(1)}m left"
                item.accumulatedMs > 0L ->
                    "Session · ${usedMin}m scrolled"
                else -> item.app.description.replace(
                    "Close ${item.app.displayName} as soon as it opens",
                    "Limit scrolling in ${item.app.displayName}",
                )
            }

            AppToggleCard(
                title = item.app.displayName,
                subtitle = statusLine,
                accent = Color(item.app.accentHex),
                checked = item.enabled,
                enabled = state.accessibilityEnabled && state.controlledScrollEnabled,
                onToggled = { onAppToggled(item.app.id, it) },
            )
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
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
                    "• Warns at ${warnMin}m, locks at ${blockMin}m for ${coolMin}m",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}
