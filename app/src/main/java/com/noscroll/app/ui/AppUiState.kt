package com.noscroll.app.ui

import com.noscroll.app.model.MonitoredApp
import com.noscroll.app.model.SocialApp

data class AppUiState(
    val accessibilityEnabled: Boolean = false,
    val reelsProtectionEnabled: Boolean = true,
    val socialProtectionEnabled: Boolean = false,
    val reelBlockCount: Int = 0,
    val socialBlockCount: Int = 0,
    val reelApps: List<ReelAppToggleState> = emptyList(),
    val socialApps: List<SocialAppToggleState> = emptyList(),
)

data class ReelAppToggleState(
    val app: MonitoredApp,
    val enabled: Boolean,
)

data class SocialAppToggleState(
    val app: SocialApp,
    val enabled: Boolean,
)

enum class NoScrollDestination(
    val label: String,
    val title: String,
    val subtitle: String,
) {
    REELS(
        label = "Reels Blocker",
        title = "Reels Blocker",
        subtitle = "Close Reels & Shorts instantly",
    ),
    SOCIAL(
        label = "Social Apps",
        title = "Social Apps Blocker",
        subtitle = "Block entire social apps",
    ),
}
