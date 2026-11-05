package com.noscroll.app

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.noscroll.app.data.PreferencesManager
import com.noscroll.app.detection.ReelDetector

/**
 * Runs in the background once Accessibility permission is granted.
 * When a monitored reel / short / spotlight surface is detected, it
 * immediately presses Back to close it.
 */
class NoScrollAccessibilityService : AccessibilityService() {

    private lateinit var preferences: PreferencesManager
    private var lastBlockAtMs: Long = 0L
    private var lastBlockedPackage: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        preferences = PreferencesManager.getInstance(this)
        Log.i(TAG, "No Scroll accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (!::preferences.isInitialized) {
            preferences = PreferencesManager.getInstance(this)
        }
        if (!preferences.isProtectionEnabled) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName == applicationContext.packageName) return

        val enabledPackages = preferences.enabledPackageNames()
        if (packageName !in enabledPackages) return

        // Avoid reacting to every tiny content change for the same package.
        val now = System.currentTimeMillis()
        if (packageName == lastBlockedPackage && now - lastBlockAtMs < BLOCK_COOLDOWN_MS) {
            return
        }

        val relevant = event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED

        if (!relevant) return

        val root = rootInActiveWindow
        if (ReelDetector.isShortFormScreen(packageName, root)) {
            closeShortForm(packageName, now)
        }
    }

    private fun closeShortForm(packageName: String, now: Long) {
        lastBlockAtMs = now
        lastBlockedPackage = packageName
        preferences.incrementBlockCount()

        val closed = performGlobalAction(GLOBAL_ACTION_BACK)
        Log.i(TAG, "Blocked short-form in $packageName (back=$closed)")

        // If Back did nothing (e.g. already at root of TikTok), go Home.
        if (!closed) {
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "No Scroll accessibility service interrupted")
    }

    companion object {
        private const val TAG = "NoScrollService"
        private const val BLOCK_COOLDOWN_MS = 800L
    }
}
