package com.noscroll.app.model

/**
 * Social apps whose short-form video feeds No Scroll can close.
 */
data class MonitoredApp(
    val id: String,
    val displayName: String,
    val packageNames: List<String>,
    val description: String,
    val accentHex: Long,
)

object MonitoredApps {
    val all: List<MonitoredApp> = listOf(
        MonitoredApp(
            id = "instagram",
            displayName = "Instagram Reels",
            packageNames = listOf("com.instagram.android"),
            description = "Closes Reels as soon as they open",
            accentHex = 0xFFE1306C,
        ),
        MonitoredApp(
            id = "youtube",
            displayName = "YouTube Shorts",
            packageNames = listOf("com.google.android.youtube"),
            description = "Closes Shorts as soon as they open",
            accentHex = 0xFFFF0000,
        ),
        MonitoredApp(
            id = "tiktok",
            displayName = "TikTok",
            packageNames = listOf(
                "com.zhiliaoapp.musically",
                "com.ss.android.ugc.trill",
            ),
            description = "Closes the For You feed",
            accentHex = 0xFF111111,
        ),
        MonitoredApp(
            id = "facebook",
            displayName = "Facebook Reels",
            packageNames = listOf("com.facebook.katana", "com.facebook.lite"),
            description = "Closes Reels as soon as they open",
            accentHex = 0xFF1877F2,
        ),
        MonitoredApp(
            id = "snapchat",
            displayName = "Snapchat Spotlight",
            packageNames = listOf("com.snapchat.android"),
            description = "Closes Spotlight as soon as it opens",
            accentHex = 0xFFFFFC00,
        ),
    )

    fun findByPackage(packageName: String): MonitoredApp? =
        all.firstOrNull { app -> app.packageNames.any { it.equals(packageName, ignoreCase = true) } }
}
