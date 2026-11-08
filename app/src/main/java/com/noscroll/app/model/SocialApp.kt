package com.noscroll.app.model

/**
 * Full social apps that can be closed entirely when opened.
 */
data class SocialApp(
    val id: String,
    val displayName: String,
    val packageNames: List<String>,
    val description: String,
    val accentHex: Long,
)

object SocialApps {
    val all: List<SocialApp> = listOf(
        SocialApp(
            id = "instagram",
            displayName = "Instagram",
            packageNames = listOf("com.instagram.android"),
            description = "Close Instagram as soon as it opens",
            accentHex = 0xFFE1306C,
        ),
        SocialApp(
            id = "youtube",
            displayName = "YouTube",
            packageNames = listOf("com.google.android.youtube"),
            description = "Close YouTube as soon as it opens",
            accentHex = 0xFFFF0000,
        ),
        SocialApp(
            id = "tiktok",
            displayName = "TikTok",
            packageNames = listOf(
                "com.zhiliaoapp.musically",
                "com.ss.android.ugc.trill",
            ),
            description = "Close TikTok as soon as it opens",
            accentHex = 0xFF111111,
        ),
        SocialApp(
            id = "facebook",
            displayName = "Facebook",
            packageNames = listOf("com.facebook.katana", "com.facebook.lite"),
            description = "Close Facebook as soon as it opens",
            accentHex = 0xFF1877F2,
        ),
        SocialApp(
            id = "snapchat",
            displayName = "Snapchat",
            packageNames = listOf("com.snapchat.android"),
            description = "Close Snapchat as soon as it opens",
            accentHex = 0xFFFFFC00,
        ),
        SocialApp(
            id = "x",
            displayName = "X (Twitter)",
            packageNames = listOf("com.twitter.android"),
            description = "Close X as soon as it opens",
            accentHex = 0xFF000000,
        ),
        SocialApp(
            id = "reddit",
            displayName = "Reddit",
            packageNames = listOf("com.reddit.frontpage"),
            description = "Close Reddit as soon as it opens",
            accentHex = 0xFFFF4500,
        ),
    )

    fun findByPackage(packageName: String): SocialApp? =
        all.firstOrNull { app -> app.packageNames.any { it.equals(packageName, ignoreCase = true) } }
}
