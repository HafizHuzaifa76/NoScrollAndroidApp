package com.noscroll.app.detection

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import com.noscroll.app.model.MonitoredApps

/**
 * Detects immersive short-form players (Reels / Shorts), not Stories or feed shelves.
 *
 * Important Instagram quirk: Stories still use legacy "reel_viewer_*" resource IDs.
 * Actual Reels use "clips_*" IDs. Matching "reel_viewer" therefore blocks Stories and
 * misses Reels — that was the previous bug.
 */
object ReelDetector {

    private val packageRules: Map<String, DetectionRules> = mapOf(
        "com.instagram.android" to DetectionRules(
            // Full-screen Reels / Clips player only.
            exactViewIds = listOf(
                "clips_viewer_view_pager",
                "clips_viewer_pager",
                "clips_viewer_container",
                "clips_video_container",
                "clips_viewer_view_pager_container",
                "clips_tabbed_fragment_main_view_pager",
            ),
            idSuffixHints = listOf(
                "clips_viewer",
                "clips_video_container",
                "clips_viewer_view_pager",
            ),
            // Never treat Stories as Reels.
            excludeIdHints = listOf(
                "story_viewer",
                "stories_gallery",
                "reel_viewer", // Stories legacy IDs — not Reels
                "reels_tray",
                "stories_tray",
            ),
            excludeTextHints = listOf(
                "story by",
                "add to story",
                "close story",
                "send message to share this story",
            ),
        ),
        "com.google.android.youtube" to DetectionRules(
            exactViewIds = listOf(
                "reel_recycler",
                "reel_player_page_container",
                "reel_watch_player",
                "reel_player_underlay",
                "reel_watch_fragment_root",
                "shorts_player_view",
                "shorts_video_pager",
            ),
            idSuffixHints = listOf(
                "reel_recycler",
                "reel_player_page_container",
                "reel_watch_player",
                "reel_watch_fragment",
                "shorts_player",
                "shorts_video_pager",
            ),
            // Home-feed Shorts shelves — do not block the normal YouTube feed.
            excludeIdHints = listOf(
                "shorts_shelf",
                "reel_shelf",
                "shelf_header",
            ),
        ),
        "com.zhiliaoapp.musically" to DetectionRules(alwaysMatch = true),
        "com.ss.android.ugc.trill" to DetectionRules(alwaysMatch = true),
        "com.facebook.katana" to DetectionRules(
            exactViewIds = listOf(
                "reels_viewer_view_pager",
                "reels_viewer_fragment_root",
                "video_home_root",
            ),
            idSuffixHints = listOf(
                "reels_viewer",
                "fb_shorts",
                "short_form_video_viewer",
                "reels_pivot",
            ),
            excludeIdHints = listOf(
                "story_viewer",
                "stories_viewer",
                "story_media",
                "fb_story",
                "bucket_view",
            ),
            excludeTextHints = listOf(
                "story by",
                "add to your story",
                "close story",
            ),
        ),
        "com.facebook.lite" to DetectionRules(
            idSuffixHints = listOf(
                "reels_viewer",
                "short_form_video_viewer",
            ),
            excludeIdHints = listOf(
                "story_viewer",
                "stories_viewer",
                "fb_story",
            ),
            excludeTextHints = listOf(
                "story by",
                "add to your story",
            ),
        ),
        "com.snapchat.android" to DetectionRules(
            idSuffixHints = listOf(
                "spotlight",
                "discover_feed_opera",
                "spotlight_opera",
            ),
            excludeIdHints = listOf(
                "story",
                "my_story",
            ),
        ),
    )

    fun isShortFormScreen(packageName: String?, root: AccessibilityNodeInfo?): Boolean {
        if (packageName.isNullOrBlank() || root == null) return false
        if (MonitoredApps.findByPackage(packageName) == null) return false

        val rules = packageRules[packageName] ?: return false
        if (rules.alwaysMatch) return true

        // Fast path: framework ID lookup for known player containers.
        // A visible clips/shorts player ID is enough — don't second-guess with story text.
        if (hasVisibleExactId(root, packageName, rules.exactViewIds)) {
            return true
        }

        // Fallback for version drift: walk the tree for ID suffixes.
        return scanForPlayer(root, rules, depth = 0, maxDepth = 16, nodesVisited = intArrayOf(0))
    }

    /**
     * Collects visible view-id suffixes that look reel/short/story related.
     * Used only for logcat diagnostics when detection misses.
     */
    fun debugCandidateIds(root: AccessibilityNodeInfo?, limit: Int = 20): List<String> {
        if (root == null) return emptyList()
        val out = LinkedHashSet<String>()
        collectDebugIds(root, out, limit, depth = 0, maxDepth = 12)
        return out.toList()
    }

    private fun collectDebugIds(
        node: AccessibilityNodeInfo,
        out: MutableSet<String>,
        limit: Int,
        depth: Int,
        maxDepth: Int,
    ) {
        if (out.size >= limit || depth > maxDepth) return
        val viewId = node.viewIdResourceName?.lowercase().orEmpty()
        if (viewId.isNotEmpty() && isMeaningfulVisible(node)) {
            val suffix = viewId.substringAfter(":id/", viewId)
            if (suffix.contains("clip") ||
                suffix.contains("reel") ||
                suffix.contains("short") ||
                suffix.contains("story") ||
                suffix.contains("spotlight")
            ) {
                out.add(suffix)
            }
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collectDebugIds(child, out, limit, depth + 1, maxDepth)
            if (out.size >= limit) return
        }
    }

    private fun hasVisibleExactId(
        root: AccessibilityNodeInfo,
        packageName: String,
        viewIds: List<String>,
    ): Boolean {
        for (id in viewIds) {
            val fullId = "$packageName:id/$id"
            val matches = root.findAccessibilityNodeInfosByViewId(fullId) ?: continue
            val hit = matches.any { node -> isMeaningfulVisible(node) }
            if (hit) return true
        }
        return false
    }

    private fun scanForPlayer(
        node: AccessibilityNodeInfo,
        rules: DetectionRules,
        depth: Int,
        maxDepth: Int,
        nodesVisited: IntArray,
    ): Boolean {
        if (depth > maxDepth || nodesVisited[0] > 250) return false
        nodesVisited[0]++

        val viewId = node.viewIdResourceName?.lowercase().orEmpty()
        val className = node.className?.toString()?.lowercase().orEmpty()
        val text = node.text?.toString()?.lowercase().orEmpty()
        val desc = node.contentDescription?.toString()?.lowercase().orEmpty()
        val idSuffix = viewId.substringAfter(":id/", viewId)
        val textHaystack = "$text $desc"

        if (rules.excludeIdHints.any { idSuffix.contains(it) || viewId.contains(it) }) {
            // Skip this story-related subtree but keep scanning siblings via caller.
            return false
        }
        if (rules.excludeTextHints.any { textHaystack.contains(it) }) {
            return false
        }

        val idHit = rules.idSuffixHints.any { hint ->
            idSuffix.contains(hint) || viewId.contains(hint)
        }
        val classHit = rules.classNameHints.any { className.contains(it) }

        if ((idHit || classHit) && isMeaningfulVisible(node)) {
            return true
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (scanForPlayer(child, rules, depth + 1, maxDepth, nodesVisited)) {
                return true
            }
        }
        return false
    }

    private fun isMeaningfulVisible(node: AccessibilityNodeInfo): Boolean {
        if (!node.isVisibleToUser) return false
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        // Instagram can keep zero-size clips_viewer nodes in the tree when Reels aren't open.
        return bounds.width() > 80 && bounds.height() > 80
    }

    private data class DetectionRules(
        val exactViewIds: List<String> = emptyList(),
        val idSuffixHints: List<String> = emptyList(),
        val classNameHints: List<String> = emptyList(),
        val excludeIdHints: List<String> = emptyList(),
        val excludeTextHints: List<String> = emptyList(),
        val alwaysMatch: Boolean = false,
    )
}
