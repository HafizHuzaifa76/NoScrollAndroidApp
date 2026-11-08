package com.noscroll.app.ui

import com.noscroll.app.model.MonitoredApp
import com.noscroll.app.model.SocialApp

data class AppUiState(
    val accessibilityEnabled: Boolean = false,
    val reelsProtectionEnabled: Boolean = true,
    val socialProtectionEnabled: Boolean = false,
    val controlledScrollEnabled: Boolean = false,
    val reelBlockCount: Int = 0,
    val socialBlockCount: Int = 0,
    val controlledBlockCount: Int = 0,
    val reelApps: List<ReelAppToggleState> = emptyList(),
    val socialApps: List<SocialAppToggleState> = emptyList(),
    val controlledApps: List<ControlledAppToggleState> = emptyList(),
)

data class ReelAppToggleState(
    val app: MonitoredApp,
    val enabled: Boolean,
)

data class SocialAppToggleState(
    val app: SocialApp,
    val enabled: Boolean,
)

data class ControlledAppToggleState(
    val app: SocialApp,
    val enabled: Boolean,
    val accumulatedMs: Long = 0L,
    val blockedUntilMs: Long = 0L,
)

enum class NoScrollDestination(
    val label: String,
    val title: String,
    val subtitle: String,
) {
    REELS(
        label = "Reels",
        title = "Reels Blocker",
        subtitle = "Close Reels & Shorts instantly",
    ),
    SOCIAL(
        label = "Apps",
        title = "Social Apps Blocker",
        subtitle = "Block entire social apps",
    ),
    CONTROLLED(
        label = "Scroll",
        title = "Controlled Scrolling",
        subtitle = "Warn, then lock after doomscrolling",
    ),
}
