package zed.rainxch.core.domain.util

import zed.rainxch.core.domain.model.DiscoveryPlatform

/**
 * Maps a release asset filename to the OS platform it targets, by
 * extension. Returns `null` for files we can't classify (zip bundles,
 * sources, sig/sha files, etc.) — callers should drop those from the
 * cross-platform picker so the list isn't polluted by non-installable
 * sidecars.
 */
fun assetPlatformOf(assetName: String): DiscoveryPlatform? {
    val lower = assetName.lowercase()
    return when {
        lower.endsWith(".apk") -> DiscoveryPlatform.Android
        lower.endsWith(".exe") || lower.endsWith(".msi") -> DiscoveryPlatform.Windows
        lower.endsWith(".dmg") || lower.endsWith(".pkg") -> DiscoveryPlatform.Macos
        lower.endsWith(".deb") ||
            lower.endsWith(".rpm") ||
            lower.endsWith(".appimage") ||
            lower.endsWith(".pkg.tar.zst") ||
            lower.endsWith(".snap") ||
            lower.endsWith(".flatpakref") -> DiscoveryPlatform.Linux
        else -> null
    }
}
