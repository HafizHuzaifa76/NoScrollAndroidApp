package com.noscroll.app.scroll

object ControlledScrollConfig {

    /**
     * Flip this for local testing.
     * - true  → production timings (10 / 15 / 40 min)
     * - false → fast dev timings (1 / 2 / 3 min)
     */
    const val isProduction: Boolean = true

    val WARN_AFTER_MS: Long
        get() = if (isProduction) 10 * 60 * 1000L else 1 * 60 * 1000L

    val BLOCK_AFTER_MS: Long
        get() = if (isProduction) 15 * 60 * 1000L else 2 * 60 * 1000L

    val COOLDOWN_MS: Long
        get() = if (isProduction) 40 * 60 * 1000L else 3 * 60 * 1000L

    /** Stop counting scroll time if no scroll for this long. */
    const val IDLE_PAUSE_MS = 60 * 1000L

    /** Leaving the app this long resets the scroll session budget. */
    const val SESSION_RESET_AWAY_MS = 30 * 60 * 1000L

    val warnAfterMinutes: Long
        get() = WARN_AFTER_MS / 60_000L

    val blockAfterMinutes: Long
        get() = BLOCK_AFTER_MS / 60_000L

    val cooldownMinutes: Long
        get() = COOLDOWN_MS / 60_000L
}
