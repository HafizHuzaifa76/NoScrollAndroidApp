package com.noscroll.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.noscroll.app.model.MonitoredApps
import com.noscroll.app.model.SocialApps
import com.noscroll.app.scroll.ScrollSessionStore

class PreferencesManager(context: Context) : ScrollSessionStore {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isProtectionEnabled: Boolean
        get() = prefs.getBoolean(KEY_PROTECTION_ENABLED, true)
        set(value) = prefs.edit { putBoolean(KEY_PROTECTION_ENABLED, value) }

    var isSocialBlockEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOCIAL_BLOCK_ENABLED, false)
        set(value) = prefs.edit { putBoolean(KEY_SOCIAL_BLOCK_ENABLED, value) }

    var isControlledScrollEnabled: Boolean
        get() = prefs.getBoolean(KEY_CONTROLLED_SCROLL_ENABLED, false)
        set(value) = prefs.edit { putBoolean(KEY_CONTROLLED_SCROLL_ENABLED, value) }

    var blockCount: Int
        get() = prefs.getInt(KEY_BLOCK_COUNT, 0)
        set(value) = prefs.edit { putInt(KEY_BLOCK_COUNT, value) }

    var socialBlockCount: Int
        get() = prefs.getInt(KEY_SOCIAL_BLOCK_COUNT, 0)
        set(value) = prefs.edit { putInt(KEY_SOCIAL_BLOCK_COUNT, value) }

    var controlledScrollBlockCount: Int
        get() = prefs.getInt(KEY_CONTROLLED_BLOCK_COUNT, 0)
        set(value) = prefs.edit { putInt(KEY_CONTROLLED_BLOCK_COUNT, value) }

    fun isAppEnabled(appId: String): Boolean =
        prefs.getBoolean(KEY_APP_PREFIX + appId, true)

    fun setAppEnabled(appId: String, enabled: Boolean) {
        prefs.edit { putBoolean(KEY_APP_PREFIX + appId, enabled) }
    }

    fun isSocialAppEnabled(appId: String): Boolean =
        prefs.getBoolean(KEY_SOCIAL_APP_PREFIX + appId, false)

    fun setSocialAppEnabled(appId: String, enabled: Boolean) {
        prefs.edit { putBoolean(KEY_SOCIAL_APP_PREFIX + appId, enabled) }
    }

    fun isControlledAppEnabled(appId: String): Boolean =
        prefs.getBoolean(KEY_CONTROLLED_APP_PREFIX + appId, true)

    fun setControlledAppEnabled(appId: String, enabled: Boolean) {
        prefs.edit { putBoolean(KEY_CONTROLLED_APP_PREFIX + appId, enabled) }
    }

    fun incrementBlockCount() {
        blockCount += 1
    }

    fun incrementSocialBlockCount() {
        socialBlockCount += 1
    }

    fun enabledReelPackageNames(): Set<String> {
        if (!isProtectionEnabled) return emptySet()
        return MonitoredApps.all
            .filter { isAppEnabled(it.id) }
            .flatMap { it.packageNames }
            .toSet()
    }

    /** @deprecated Use [enabledReelPackageNames] */
    fun enabledPackageNames(): Set<String> = enabledReelPackageNames()

    fun enabledSocialPackageNames(): Set<String> {
        if (!isSocialBlockEnabled) return emptySet()
        return SocialApps.all
            .filter { isSocialAppEnabled(it.id) }
            .flatMap { it.packageNames }
            .toSet()
    }

    /**
     * Packages under Controlled Scrolling.
     * Hard Social Block apps are excluded so the nuclear mode stays exclusive.
     */
    fun enabledControlledPackageNames(): Set<String> {
        if (!isControlledScrollEnabled) return emptySet()
        val hardBlocked = enabledSocialPackageNames()
        return SocialApps.all
            .filter { isControlledAppEnabled(it.id) }
            .flatMap { it.packageNames }
            .filterNot { it in hardBlocked }
            .toSet()
    }

    fun controlledAccumulatedMs(appId: String): Long = getAccumulatedMs(appId)

    fun controlledBlockedUntil(appId: String): Long = getBlockedUntil(appId)

    // region ScrollSessionStore

    override fun getAccumulatedMs(appId: String): Long =
        prefs.getLong(KEY_CS_ACCUM + appId, 0L)

    override fun setAccumulatedMs(appId: String, value: Long) {
        prefs.edit { putLong(KEY_CS_ACCUM + appId, value.coerceAtLeast(0L)) }
    }

    override fun getLastScrollAt(appId: String): Long =
        prefs.getLong(KEY_CS_LAST_SCROLL + appId, 0L)

    override fun setLastScrollAt(appId: String, value: Long) {
        prefs.edit { putLong(KEY_CS_LAST_SCROLL + appId, value) }
    }

    override fun getLastSeenAt(appId: String): Long =
        prefs.getLong(KEY_CS_LAST_SEEN + appId, 0L)

    override fun setLastSeenAt(appId: String, value: Long) {
        prefs.edit { putLong(KEY_CS_LAST_SEEN + appId, value) }
    }

    override fun isWarningShown(appId: String): Boolean =
        prefs.getBoolean(KEY_CS_WARN_SHOWN + appId, false)

    override fun setWarningShown(appId: String, shown: Boolean) {
        prefs.edit { putBoolean(KEY_CS_WARN_SHOWN + appId, shown) }
    }

    override fun getBlockedUntil(appId: String): Long =
        prefs.getLong(KEY_CS_BLOCKED_UNTIL + appId, 0L)

    override fun setBlockedUntil(appId: String, value: Long) {
        prefs.edit { putLong(KEY_CS_BLOCKED_UNTIL + appId, value) }
    }

    override fun clearBlock(appId: String) {
        prefs.edit { putLong(KEY_CS_BLOCKED_UNTIL + appId, 0L) }
    }

    override fun resetSession(appId: String) {
        prefs.edit {
            putLong(KEY_CS_ACCUM + appId, 0L)
            putLong(KEY_CS_LAST_SCROLL + appId, 0L)
            putBoolean(KEY_CS_WARN_SHOWN + appId, false)
        }
    }

    override fun incrementControlledBlockCount() {
        controlledScrollBlockCount += 1
    }

    // endregion

    companion object {
        private const val PREFS_NAME = "noscroll_prefs"
        private const val KEY_PROTECTION_ENABLED = "protection_enabled"
        private const val KEY_SOCIAL_BLOCK_ENABLED = "social_block_enabled"
        private const val KEY_CONTROLLED_SCROLL_ENABLED = "controlled_scroll_enabled"
        private const val KEY_BLOCK_COUNT = "block_count"
        private const val KEY_SOCIAL_BLOCK_COUNT = "social_block_count"
        private const val KEY_CONTROLLED_BLOCK_COUNT = "controlled_block_count"
        private const val KEY_APP_PREFIX = "app_enabled_"
        private const val KEY_SOCIAL_APP_PREFIX = "social_app_enabled_"
        private const val KEY_CONTROLLED_APP_PREFIX = "controlled_app_enabled_"

        private const val KEY_CS_ACCUM = "cs_accum_"
        private const val KEY_CS_LAST_SCROLL = "cs_last_scroll_"
        private const val KEY_CS_LAST_SEEN = "cs_last_seen_"
        private const val KEY_CS_WARN_SHOWN = "cs_warn_shown_"
        private const val KEY_CS_BLOCKED_UNTIL = "cs_blocked_until_"

        @Volatile
        private var instance: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager =
            instance ?: synchronized(this) {
                instance ?: PreferencesManager(context).also { instance = it }
            }
    }
}
