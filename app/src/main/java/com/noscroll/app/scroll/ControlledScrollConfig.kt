package com.noscroll.app.scroll

object ControlledScrollConfig {
    const val WARN_AFTER_MS = 10 * 60 * 1000L
    const val BLOCK_AFTER_MS = 15 * 60 * 1000L
    const val COOLDOWN_MS = 40 * 60 * 1000L

    /** Stop counting scroll time if no scroll for this long. */
    const val IDLE_PAUSE_MS = 60 * 1000L

    /** Leaving the app this long resets the scroll session budget. */
    const val SESSION_RESET_AWAY_MS = 30 * 60 * 1000L
}
