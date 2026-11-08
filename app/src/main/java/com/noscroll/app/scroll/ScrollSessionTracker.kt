package com.noscroll.app.scroll

/**
 * Scroll-based session accounting for Controlled Scrolling.
 *
 * - Accumulates time only between scroll events when the gap is under [ControlledScrollConfig.IDLE_PAUSE_MS]
 * - Pauses counting after 1 minute idle (does not wipe progress)
 * - Resets the budget after [ControlledScrollConfig.SESSION_RESET_AWAY_MS] away from the app
 */
class ScrollSessionTracker(
    private val store: ScrollSessionStore,
) {

    sealed class Action {
        data object None : Action()
        data class Warn(val accumulatedMs: Long, val appId: String, val packageName: String) : Action()
        data class Block(val accumulatedMs: Long, val appId: String, val packageName: String, val blockedUntilMs: Long) : Action()
        data class EnforceCooldown(val appId: String, val packageName: String, val blockedUntilMs: Long) : Action()
    }

    fun checkCooldown(appId: String, packageName: String, now: Long): Action {
        val blockedUntil = store.getBlockedUntil(appId)
        if (blockedUntil > now) {
            store.setLastSeenAt(appId, now)
            return Action.EnforceCooldown(appId, packageName, blockedUntil)
        }
        if (blockedUntil > 0L) {
            // Cooldown finished — clear block marker.
            store.clearBlock(appId)
        }
        return Action.None
    }

    fun onAppSeen(appId: String, now: Long) {
        maybeResetAfterAway(appId, now)
        store.setLastSeenAt(appId, now)
    }

    fun onScroll(appId: String, packageName: String, now: Long): Action {
        val cooldown = checkCooldown(appId, packageName, now)
        if (cooldown is Action.EnforceCooldown) return cooldown

        maybeResetAfterAway(appId, now)

        val lastScrollAt = store.getLastScrollAt(appId)
        var accumulated = store.getAccumulatedMs(appId)

        if (lastScrollAt > 0L) {
            val gap = now - lastScrollAt
            if (gap in 1 until ControlledScrollConfig.IDLE_PAUSE_MS) {
                accumulated += gap
                store.setAccumulatedMs(appId, accumulated)
            }
            // gap >= idle → pause (do not add the idle gap)
        }

        store.setLastScrollAt(appId, now)
        store.setLastSeenAt(appId, now)

        if (accumulated >= ControlledScrollConfig.BLOCK_AFTER_MS) {
            val blockedUntil = now + ControlledScrollConfig.COOLDOWN_MS
            store.setBlockedUntil(appId, blockedUntil)
            store.setWarningShown(appId, true)
            store.incrementControlledBlockCount()
            // Fresh budget after this lockout ends.
            store.setAccumulatedMs(appId, 0L)
            store.setLastScrollAt(appId, 0L)
            return Action.Block(accumulated, appId, packageName, blockedUntil)
        }

        if (accumulated >= ControlledScrollConfig.WARN_AFTER_MS && !store.isWarningShown(appId)) {
            store.setWarningShown(appId, true)
            return Action.Warn(accumulated, appId, packageName)
        }

        return Action.None
    }

    private fun maybeResetAfterAway(appId: String, now: Long) {
        val lastSeen = store.getLastSeenAt(appId)
        if (lastSeen <= 0L) return
        if (now - lastSeen < ControlledScrollConfig.SESSION_RESET_AWAY_MS) return
        // Away long enough → new session budget (unless still in cooldown).
        if (store.getBlockedUntil(appId) > now) return
        store.resetSession(appId)
    }
}

interface ScrollSessionStore {
    fun getAccumulatedMs(appId: String): Long
    fun setAccumulatedMs(appId: String, value: Long)
    fun getLastScrollAt(appId: String): Long
    fun setLastScrollAt(appId: String, value: Long)
    fun getLastSeenAt(appId: String): Long
    fun setLastSeenAt(appId: String, value: Long)
    fun isWarningShown(appId: String): Boolean
    fun setWarningShown(appId: String, shown: Boolean)
    fun getBlockedUntil(appId: String): Long
    fun setBlockedUntil(appId: String, value: Long)
    fun clearBlock(appId: String)
    fun resetSession(appId: String)
    fun incrementControlledBlockCount()
}
