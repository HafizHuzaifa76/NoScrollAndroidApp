package com.noscroll.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.noscroll.app.model.MonitoredApps
import com.noscroll.app.model.SocialApps

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isProtectionEnabled: Boolean
        get() = prefs.getBoolean(KEY_PROTECTION_ENABLED, true)
        set(value) = prefs.edit { putBoolean(KEY_PROTECTION_ENABLED, value) }

    var isSocialBlockEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOCIAL_BLOCK_ENABLED, false)
        set(value) = prefs.edit { putBoolean(KEY_SOCIAL_BLOCK_ENABLED, value) }

    var blockCount: Int
        get() = prefs.getInt(KEY_BLOCK_COUNT, 0)
        set(value) = prefs.edit { putInt(KEY_BLOCK_COUNT, value) }

    var socialBlockCount: Int
        get() = prefs.getInt(KEY_SOCIAL_BLOCK_COUNT, 0)
        set(value) = prefs.edit { putInt(KEY_SOCIAL_BLOCK_COUNT, value) }

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

    companion object {
        private const val PREFS_NAME = "noscroll_prefs"
        private const val KEY_PROTECTION_ENABLED = "protection_enabled"
        private const val KEY_SOCIAL_BLOCK_ENABLED = "social_block_enabled"
        private const val KEY_BLOCK_COUNT = "block_count"
        private const val KEY_SOCIAL_BLOCK_COUNT = "social_block_count"
        private const val KEY_APP_PREFIX = "app_enabled_"
        private const val KEY_SOCIAL_APP_PREFIX = "social_app_enabled_"

        @Volatile
        private var instance: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager =
            instance ?: synchronized(this) {
                instance ?: PreferencesManager(context).also { instance = it }
            }
    }
}
