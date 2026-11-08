package com.noscroll.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.noscroll.app.ui.AppUiState
import com.noscroll.app.ui.components.AppToggleCard
import com.noscroll.app.ui.components.HeroStatusCard
import com.noscroll.app.ui.components.MasterToggleCard
import com.noscroll.app.ui.components.SectionHeader

@Composable
fun ReelsBlockerScreen(
    state: AppUiState,
    onOpenAccessibilitySettings: () -> Unit,
    onProtectionToggled: (Boolean) -> Unit,
    onAppToggled: (String, Boolean) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            HeroStatusCard(
                accessibilityEnabled = state.accessibilityEnabled,
                protectionEnabled = state.reelsProtectionEnabled,
                blockCount = state.reelBlockCount,
                activeLabel = "Reels blocked",
                idleLabel = "Setup needed",
                pausedLabel = "Reels paused",
                activeDescription = "Watching in the background. Opening a Reel or Short closes it instantly.",
                idleDescription = "Enable accessibility once so No Scroll can detect Reels and Shorts while you use other apps.",
                pausedDescription = "Service is ready, but Reels blocking is paused. Turn it back on below.",
                onOpenAccessibilitySettings = onOpenAccessibilitySettings,
                onRefresh = onRefresh,
            )
        }

        item {
            MasterToggleCard(
                title = "Block Reels & Shorts",
                subtitle = "Instant close when short video opens",
                enabled = state.reelsProtectionEnabled,
                interactionEnabled = state.accessibilityEnabled,
                onToggled = onProtectionToggled,
            )
        }

        item {
            SectionHeader(
                title = "Apps to watch",
                subtitle = "Choose which short-form feeds should be closed",
            )
        }

        items(state.reelApps, key = { it.app.id }) { item ->
            AppToggleCard(
                title = item.app.displayName,
                subtitle = item.app.description,
                accent = Color(item.app.accentHex),
                checked = item.enabled,
                enabled = state.accessibilityEnabled && state.reelsProtectionEnabled,
                onToggled = { onAppToggled(item.app.id, it) },
            )
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}
