package com.noscroll.app

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.noscroll.app.data.PreferencesManager
import com.noscroll.app.detection.ReelDetector

/**
 * Runs in the background once Accessibility permission is granted.
 *
 * 1. Social Apps Blocker — closes entire selected apps with Home
 * 2. Reels Blocker — closes Reels / Shorts with Back
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

        val packageName = event.packageName?.toString() ?: return
        if (packageName == applicationContext.packageName) return

        val now = System.currentTimeMillis()
        if (packageName == lastBlockedPackage && now - lastBlockAtMs < BLOCK_COOLDOWN_MS) {
            return
        }

        val relevant = event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED
        if (!relevant) return

        // Full-app social blocking takes priority over reel-only blocking.
        val socialPackages = preferences.enabledSocialPackageNames()
        if (packageName in socialPackages) {
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            ) {
                closeSocialApp(packageName, now)
            }
            return
        }

        if (!preferences.isProtectionEnabled) return

        val reelPackages = preferences.enabledReelPackageNames()
        if (packageName !in reelPackages) return

        val root = rootInActiveWindow ?: return
        val matched = ReelDetector.isShortFormScreen(packageName, root)
        if (matched) {
            closeShortForm(packageName, now)
        } else if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val candidates = ReelDetector.debugCandidateIds(root)
            Log.d(
                TAG,
                "No match in $packageName class=${event.className} candidates=$candidates",
            )
        }
    }

    private fun closeSocialApp(packageName: String, now: Long) {
        lastBlockAtMs = now
        lastBlockedPackage = packageName
        preferences.incrementSocialBlockCount()
        val wentHome = performGlobalAction(GLOBAL_ACTION_HOME)
        Log.i(TAG, "Blocked social app $packageName (home=$wentHome)")
    }

    private fun closeShortForm(packageName: String, now: Long) {
        lastBlockAtMs = now
        lastBlockedPackage = packageName
        preferences.incrementBlockCount()

        val closed = performGlobalAction(GLOBAL_ACTION_BACK)
        Log.i(TAG, "Blocked short-form in $packageName (back=$closed)")

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
