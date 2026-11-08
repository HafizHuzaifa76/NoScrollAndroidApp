package com.noscroll.app

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.noscroll.app.data.PreferencesManager
import com.noscroll.app.detection.ReelDetector
import com.noscroll.app.model.SocialApps
import com.noscroll.app.scroll.ScrollSessionTracker

/**
 * Runs in the background once Accessibility permission is granted.
 *
 * Priority:
 * 1. Social Apps Blocker — closes entire selected apps with Home
 * 2. Controlled Scrolling cooldown — keeps locked apps closed
 * 3. Reels Blocker — closes Reels / Shorts with Back
 * 4. Controlled Scrolling — scroll-time warn / block
 */
class NoScrollAccessibilityService : AccessibilityService() {

    private lateinit var preferences: PreferencesManager
    private lateinit var scrollTracker: ScrollSessionTracker
    private var lastBlockAtMs: Long = 0L
    private var lastBlockedPackage: String? = null
    private var lastWarnAtMs: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        preferences = PreferencesManager.getInstance(this)
        scrollTracker = ScrollSessionTracker(preferences)
        Log.i(TAG, "No Scroll accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        ensureDeps()

        val packageName = event.packageName?.toString() ?: return
        if (packageName == applicationContext.packageName) return

        val now = System.currentTimeMillis()
        if (packageName == lastBlockedPackage && now - lastBlockAtMs < ACTION_COOLDOWN_MS) {
            return
        }

        val relevant = event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED
        if (!relevant) return

        // 1) Full-app social blocking — unchanged exclusive path.
        val socialPackages = preferences.enabledSocialPackageNames()
        if (packageName in socialPackages) {
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            ) {
                closeSocialApp(packageName, now)
            }
            return
        }

        val controlledPackages = preferences.enabledControlledPackageNames()
        val controlledApp = SocialApps.findByPackage(packageName)
        val inControlled = packageName in controlledPackages && controlledApp != null

        // 2) Controlled-scroll cooldown enforcement.
        if (inControlled) {
            val cooldownAction = scrollTracker.checkCooldown(controlledApp!!.id, packageName, now)
            if (cooldownAction is ScrollSessionTracker.Action.EnforceCooldown) {
                enforceControlledCooldown(packageName, controlledApp.displayName, cooldownAction.blockedUntilMs, now)
                return
            }
            scrollTracker.onAppSeen(controlledApp.id, now)
        }

        // 3) Reels / Shorts blocker — preserved behavior.
        var closedReel = false
        if (preferences.isProtectionEnabled) {
            val reelPackages = preferences.enabledReelPackageNames()
            if (packageName in reelPackages) {
                val root = rootInActiveWindow
                if (root != null && ReelDetector.isShortFormScreen(packageName, root)) {
                    closeShortForm(packageName, now)
                    closedReel = true
                } else if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                    val candidates = ReelDetector.debugCandidateIds(root)
                    Log.d(
                        TAG,
                        "No match in $packageName class=${event.className} candidates=$candidates",
                    )
                }
            }
        }

        // 4) Controlled scrolling — only count scroll when not just kicked out of a reel.
        if (!closedReel && inControlled && event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            handleControlledScroll(controlledApp!!, packageName, now)
        }
    }

    private fun handleControlledScroll(
        app: com.noscroll.app.model.SocialApp,
        packageName: String,
        now: Long,
    ) {
        when (val action = scrollTracker.onScroll(app.id, packageName, now)) {
            is ScrollSessionTracker.Action.Warn -> {
                if (now - lastWarnAtMs < WARN_LAUNCH_COOLDOWN_MS) return
                lastWarnAtMs = now
                Log.i(TAG, "Controlled scroll warning for ${app.id} after ${action.accumulatedMs}ms")
                openAlert(
                    mode = ControlledScrollAlertActivity.MODE_WARN,
                    appName = app.displayName,
                    blockedUntil = 0L,
                )
            }

            is ScrollSessionTracker.Action.Block -> {
                lastBlockAtMs = now
                lastBlockedPackage = packageName
                Log.i(TAG, "Controlled scroll block for ${app.id} until ${action.blockedUntilMs}")
                performGlobalAction(GLOBAL_ACTION_HOME)
                openAlert(
                    mode = ControlledScrollAlertActivity.MODE_BLOCK,
                    appName = app.displayName,
                    blockedUntil = action.blockedUntilMs,
                )
            }

            is ScrollSessionTracker.Action.EnforceCooldown -> {
                enforceControlledCooldown(packageName, app.displayName, action.blockedUntilMs, now)
            }

            ScrollSessionTracker.Action.None -> Unit
        }
    }

    private fun enforceControlledCooldown(
        packageName: String,
        appName: String,
        blockedUntilMs: Long,
        now: Long,
    ) {
        lastBlockAtMs = now
        lastBlockedPackage = packageName
        val wentHome = performGlobalAction(GLOBAL_ACTION_HOME)
        Log.i(TAG, "Controlled cooldown for $packageName (home=$wentHome)")
        // Avoid spamming the block screen on every event.
        if (now - lastWarnAtMs > WARN_LAUNCH_COOLDOWN_MS) {
            lastWarnAtMs = now
            openAlert(
                mode = ControlledScrollAlertActivity.MODE_BLOCK,
                appName = appName,
                blockedUntil = blockedUntilMs,
            )
        }
    }

    private fun openAlert(mode: String, appName: String, blockedUntil: Long) {
        val intent = Intent(this, ControlledScrollAlertActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(ControlledScrollAlertActivity.EXTRA_MODE, mode)
            putExtra(ControlledScrollAlertActivity.EXTRA_APP_NAME, appName)
            putExtra(ControlledScrollAlertActivity.EXTRA_BLOCKED_UNTIL, blockedUntil)
        }
        startActivity(intent)
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

    private fun ensureDeps() {
        if (!::preferences.isInitialized) {
            preferences = PreferencesManager.getInstance(this)
        }
        if (!::scrollTracker.isInitialized) {
            scrollTracker = ScrollSessionTracker(preferences)
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "No Scroll accessibility service interrupted")
    }

    companion object {
        private const val TAG = "NoScrollService"
        private const val ACTION_COOLDOWN_MS = 800L
        private const val WARN_LAUNCH_COOLDOWN_MS = 8_000L
    }
}
