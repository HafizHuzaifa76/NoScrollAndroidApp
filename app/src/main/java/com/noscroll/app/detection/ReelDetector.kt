package com.noscroll.app.detection

import android.view.accessibility.AccessibilityNodeInfo
import com.noscroll.app.model.MonitoredApps

/**
 * Heuristics for detecting short-form video screens (Reels, Shorts, etc.).
 *
 * Social apps change their UI often, so detection combines package-specific
 * signals with shared keywords found in class names, view IDs, and content.
 * Keywords are intentionally specific to avoid matching home-feed tab labels.
 */
object ReelDetector {

    private val packageMatchers: Map<String, PackageMatcher> = mapOf(
        "com.instagram.android" to PackageMatcher(
            // Full-screen Reels / Clips viewer — not the home "Reels" tab label.
            idOrClassContains = listOf(
                "clips_viewer",
                "clipsviewer",
                "reels_viewer",
                "reelsviewer",
                "reel_viewer",
                "clips_video_container",
                "reel_viewer_video",
                "clips_tabbed_pager",
            ),
            textOrDescContains = listOf(
                "reel by",
                "reels viewer",
                "double tap to like",
            ),
        ),
        "com.google.android.youtube" to PackageMatcher(
            idOrClassContains = listOf(
                "shorts_player",
                "shortsplayer",
                "reel_watch_fragment",
                "shorts_ui",
                "watch_while_shorts",
            ),
            textOrDescContains = listOf(
                "shorts player",
                "swipe for next short",
            ),
            // YouTube often exposes "Shorts" in the window/root hierarchy when open.
            aggressiveTextOrDescContains = listOf("open shorts", "shorts ·"),
        ),
        "com.zhiliaoapp.musically" to PackageMatcher(
            // TikTok's main surface is endless short video.
            alwaysMatch = true,
        ),
        "com.ss.android.ugc.trill" to PackageMatcher(
            alwaysMatch = true,
        ),
        "com.facebook.katana" to PackageMatcher(
            idOrClassContains = listOf(
                "reels_viewer",
                "reelsviewer",
                "fb_shorts",
                "short_form_video",
                "reels_tray_viewer",
            ),
            textOrDescContains = listOf(
                "reel by",
                "reels viewer",
            ),
        ),
        "com.facebook.lite" to PackageMatcher(
            idOrClassContains = listOf("reels_viewer", "reelsviewer", "reel"),
            textOrDescContains = listOf("reel by"),
        ),
        "com.snapchat.android" to PackageMatcher(
            idOrClassContains = listOf(
                "spotlight",
                "discover_feed_opera",
                "spotlight_opera",
            ),
            textOrDescContains = listOf(
                "spotlight",
            ),
        ),
    )

    fun isShortFormScreen(packageName: String?, root: AccessibilityNodeInfo?): Boolean {
        if (packageName.isNullOrBlank() || root == null) return false
        if (MonitoredApps.findByPackage(packageName) == null) return false

        val matcher = packageMatchers[packageName] ?: return false
        if (matcher.alwaysMatch) return true

        return scan(root, matcher, depth = 0, maxDepth = 14)
    }

    private fun scan(
        node: AccessibilityNodeInfo,
        matcher: PackageMatcher,
        depth: Int,
        maxDepth: Int,
    ): Boolean {
        if (depth > maxDepth) return false

        val className = node.className?.toString()?.lowercase().orEmpty()
        val viewId = node.viewIdResourceName?.lowercase().orEmpty()
        val text = node.text?.toString()?.lowercase().orEmpty()
        val desc = node.contentDescription?.toString()?.lowercase().orEmpty()
        val hint = node.hintText?.toString()?.lowercase().orEmpty()

        val idClassHaystack = "$className $viewId"
        val textHaystack = "$text $desc $hint"

        if (matcher.idOrClassContains.any { idClassHaystack.contains(it) }) return true
        if (matcher.textOrDescContains.any { textHaystack.contains(it) }) return true
        if (matcher.aggressiveTextOrDescContains.any { textHaystack.contains(it) }) return true

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (scan(child, matcher, depth + 1, maxDepth)) return true
        }
        return false
    }

    private data class PackageMatcher(
        val idOrClassContains: List<String> = emptyList(),
        val textOrDescContains: List<String> = emptyList(),
        val aggressiveTextOrDescContains: List<String> = emptyList(),
        val alwaysMatch: Boolean = false,
    )
}
