package com.noscroll.app.scroll

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScrollSessionTrackerTest {

    private class FakeStore : ScrollSessionStore {
        var accum = 0L
        var lastScroll = 0L
        var lastSeen = 0L
        var warnShown = false
        var blockedUntil = 0L
        var blocks = 0

        override fun getAccumulatedMs(appId: String) = accum
        override fun setAccumulatedMs(appId: String, value: Long) { accum = value }
        override fun getLastScrollAt(appId: String) = lastScroll
        override fun setLastScrollAt(appId: String, value: Long) { lastScroll = value }
        override fun getLastSeenAt(appId: String) = lastSeen
        override fun setLastSeenAt(appId: String, value: Long) { lastSeen = value }
        override fun isWarningShown(appId: String) = warnShown
        override fun setWarningShown(appId: String, shown: Boolean) { warnShown = shown }
        override fun getBlockedUntil(appId: String) = blockedUntil
        override fun setBlockedUntil(appId: String, value: Long) { blockedUntil = value }
        override fun clearBlock(appId: String) { blockedUntil = 0L }
        override fun resetSession(appId: String) {
            accum = 0L
            lastScroll = 0L
            warnShown = false
        }
        override fun incrementControlledBlockCount() { blocks += 1 }
    }

    @Test
    fun accumulatesScrollGapsUnderOneMinute() {
        val store = FakeStore()
        val tracker = ScrollSessionTracker(store)
        val t0 = 1_000_000L

        tracker.onScroll("instagram", "com.instagram.android", t0)
        tracker.onScroll("instagram", "com.instagram.android", t0 + 20_000L)

        assertEquals(20_000L, store.accum)
    }

    @Test
    fun pausesWhenIdleMoreThanOneMinute() {
        val store = FakeStore()
        val tracker = ScrollSessionTracker(store)
        val t0 = 1_000_000L

        tracker.onScroll("instagram", "com.instagram.android", t0)
        tracker.onScroll("instagram", "com.instagram.android", t0 + 20_000L)
        // Idle > 1 minute — gap must not be counted.
        tracker.onScroll("instagram", "com.instagram.android", t0 + 20_000L + 90_000L)

        assertEquals(20_000L, store.accum)
    }

    @Test
    fun resetsAfterLongAway() {
        val store = FakeStore()
        val tracker = ScrollSessionTracker(store)
        val t0 = 1_000_000L

        tracker.onScroll("instagram", "com.instagram.android", t0)
        tracker.onScroll("instagram", "com.instagram.android", t0 + 30_000L)
        assertEquals(30_000L, store.accum)

        // Away longer than session reset window.
        val later = t0 + 30_000L + ControlledScrollConfig.SESSION_RESET_AWAY_MS + 1
        tracker.onScroll("instagram", "com.instagram.android", later)
        assertEquals(0L, store.accum)
    }

    @Test
    fun warnsThenBlocks() {
        val store = FakeStore()
        val tracker = ScrollSessionTracker(store)
        var now = 1_000_000L

        tracker.onScroll("instagram", "com.instagram.android", now)
        // Jump accumulation near warn threshold via store, then scroll.
        store.accum = ControlledScrollConfig.WARN_AFTER_MS - 5_000L
        store.lastScroll = now
        now += 10_000L
        val warn = tracker.onScroll("instagram", "com.instagram.android", now)
        assertTrue(warn is ScrollSessionTracker.Action.Warn)

        store.accum = ControlledScrollConfig.BLOCK_AFTER_MS - 5_000L
        store.lastScroll = now
        now += 10_000L
        val block = tracker.onScroll("instagram", "com.instagram.android", now)
        assertTrue(block is ScrollSessionTracker.Action.Block)
        assertEquals(1, store.blocks)
    }
}
