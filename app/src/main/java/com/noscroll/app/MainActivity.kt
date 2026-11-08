package com.noscroll.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.noscroll.app.data.PreferencesManager
import com.noscroll.app.model.MonitoredApps
import com.noscroll.app.model.SocialApps
import com.noscroll.app.ui.AppUiState
import com.noscroll.app.ui.ControlledAppToggleState
import com.noscroll.app.ui.NoScrollApp
import com.noscroll.app.ui.ReelAppToggleState
import com.noscroll.app.ui.SocialAppToggleState
import com.noscroll.app.ui.theme.NoScrollAppTheme
import com.noscroll.app.util.AccessibilityUtils

class MainActivity : ComponentActivity() {

    private lateinit var preferences: PreferencesManager
    private var uiState by mutableStateOf(AppUiState())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = PreferencesManager.getInstance(this)
        enableEdgeToEdge()
        setContent {
            NoScrollAppTheme {
                NoScrollApp(
                    state = uiState,
                    onOpenAccessibilitySettings = {
                        AccessibilityUtils.openAccessibilitySettings(this)
                    },
                    onReelsProtectionToggled = { enabled ->
                        preferences.isProtectionEnabled = enabled
                        refreshUiState()
                    },
                    onSocialProtectionToggled = { enabled ->
                        preferences.isSocialBlockEnabled = enabled
                        refreshUiState()
                    },
                    onControlledScrollToggled = { enabled ->
                        preferences.isControlledScrollEnabled = enabled
                        refreshUiState()
                    },
                    onReelAppToggled = { appId, enabled ->
                        preferences.setAppEnabled(appId, enabled)
                        refreshUiState()
                    },
                    onSocialAppToggled = { appId, enabled ->
                        preferences.setSocialAppEnabled(appId, enabled)
                        refreshUiState()
                    },
                    onControlledAppToggled = { appId, enabled ->
                        preferences.setControlledAppEnabled(appId, enabled)
                        refreshUiState()
                    },
                    onRefresh = { refreshUiState() },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshUiState()
    }

    private fun refreshUiState() {
        uiState = AppUiState(
            accessibilityEnabled = AccessibilityUtils.isServiceEnabled(this),
            reelsProtectionEnabled = preferences.isProtectionEnabled,
            socialProtectionEnabled = preferences.isSocialBlockEnabled,
            controlledScrollEnabled = preferences.isControlledScrollEnabled,
            reelBlockCount = preferences.blockCount,
            socialBlockCount = preferences.socialBlockCount,
            controlledBlockCount = preferences.controlledScrollBlockCount,
            reelApps = MonitoredApps.all.map { app ->
                ReelAppToggleState(
                    app = app,
                    enabled = preferences.isAppEnabled(app.id),
                )
            },
            socialApps = SocialApps.all.map { app ->
                SocialAppToggleState(
                    app = app,
                    enabled = preferences.isSocialAppEnabled(app.id),
                )
            },
            controlledApps = SocialApps.all.map { app ->
                ControlledAppToggleState(
                    app = app,
                    enabled = preferences.isControlledAppEnabled(app.id),
                    accumulatedMs = preferences.controlledAccumulatedMs(app.id),
                    blockedUntilMs = preferences.controlledBlockedUntil(app.id),
                )
            },
        )
    }
}
