package com.noscroll.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.noscroll.app.data.PreferencesManager
import com.noscroll.app.model.MonitoredApps
import com.noscroll.app.ui.AppToggleState
import com.noscroll.app.ui.HomeScreen
import com.noscroll.app.ui.HomeUiState
import com.noscroll.app.ui.theme.NoScrollAppTheme
import com.noscroll.app.util.AccessibilityUtils

class MainActivity : ComponentActivity() {

    private lateinit var preferences: PreferencesManager
    private var uiState by mutableStateOf(HomeUiState())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = PreferencesManager.getInstance(this)
        enableEdgeToEdge()
        setContent {
            NoScrollAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(
                        state = uiState,
                        onOpenAccessibilitySettings = {
                            AccessibilityUtils.openAccessibilitySettings(this)
                        },
                        onProtectionToggled = { enabled ->
                            preferences.isProtectionEnabled = enabled
                            refreshUiState()
                        },
                        onAppToggled = { appId, enabled ->
                            preferences.setAppEnabled(appId, enabled)
                            refreshUiState()
                        },
                        onRefresh = { refreshUiState() },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshUiState()
    }

    private fun refreshUiState() {
        uiState = HomeUiState(
            accessibilityEnabled = AccessibilityUtils.isServiceEnabled(this),
            protectionEnabled = preferences.isProtectionEnabled,
            blockCount = preferences.blockCount,
            apps = MonitoredApps.all.map { app ->
                AppToggleState(
                    app = app,
                    enabled = preferences.isAppEnabled(app.id),
                )
            },
        )
    }
}
