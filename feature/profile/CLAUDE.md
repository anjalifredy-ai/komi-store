# CLAUDE.md - Profile Feature

## Purpose

Account-level screen — GitHub user profile, login/logout, sponsor entry, version info. **Settings have moved to `feature/tweaks/`** (appearance, installer type, proxy, telemetry, translation, mirror, hidden/skipped lists). This module is now narrow on purpose: it owns the account identity, exposes `ProfileRepository.getUser()` to the rest of the app (read by `feature/home`, `feature/search`, `feature/details`, `feature/starred`, `feature/favourites`, `feature/tweaks`, `feature/dev-profile` for the E20 self-owned badge and account-aware flows), and renders the SponsorScreen.

## Module Structure

```
feature/profile/
├── domain/
│   ├── model/UserProfile.kt          # User profile data model
│   └── repository/ProfileRepository.kt  # Auth state, user, logout, cache
├── data/
│   ├── di/SharedModule.kt            # Koin: profileModule
│   ├── repository/ProfileRepositoryImpl.kt  # Implementation
│   └── mappers/UserProfileMappers.kt # DTO → domain model mappers
└── presentation/
    ├── ProfileViewModel.kt            # State management for profile screen
    ├── ProfileState.kt                # User, theme, proxy, installer, Shizuku status
    ├── ProfileAction.kt               # Theme, logout, proxy, installer, Shizuku actions
    ├── ProfileEvent.kt                # One-off events (navigation, etc.)
    ├── ProfileRoot.kt                 # Main composable (LazyColumn of sections)
    ├── SponsorScreen.kt               # Sponsor/donation screen
    ├── model/ProxyType.kt             # NONE, HTTP, SOCKS
    └── components/
        ├── LogoutDialog.kt            # Logout confirmation dialog
        ├── SectionText.kt             # Section header text component
        └── sections/
            ├── Account.kt             # Login/logout actions
            ├── AccountSection.kt      # Account info display
            ├── Appearance.kt          # Theme color, font, dark mode, AMOLED
            ├── Installation.kt        # Installer type selector (Default/Shizuku) with status
            ├── Network.kt             # Proxy configuration (type, host, port, auth)
            ├── Options.kt             # Favourites, starred, clipboard detection
            ├── Others.kt              # Help, clear cache, version info
            ├── ProfileSection.kt      # User avatar, name, bio
            └── SettingsSection.kt     # Settings group container
```

## Key Interfaces

```kotlin
interface ProfileRepository {
    val isUserLoggedIn: Flow<Boolean>
    fun getUser(): Flow<UserProfile?>
    fun getVersionName(): String
    suspend fun logout()
    fun observeCacheSize(): Flow<Long>
    suspend fun clearCache()
}
```

## State

```kotlin
data class ProfileState(
    val userProfile: UserProfile?,
    val selectedThemeColor: AppTheme,
    val selectedFontTheme: FontTheme,
    val isLogoutDialogVisible: Boolean,
    val isUserLoggedIn: Boolean,
    val isAmoledThemeEnabled: Boolean,
    val isDarkTheme: Boolean?,
    val versionName: String,
    val proxyType: ProxyType,
    val proxyHost: String, val proxyPort: String,
    val proxyUsername: String, val proxyPassword: String,
    val isProxyPasswordVisible: Boolean,
    val autoDetectClipboardLinks: Boolean,
    val cacheSize: String,
    val installerType: InstallerType,          // DEFAULT or SHIZUKU
    val shizukuAvailability: ShizukuAvailability  // UNAVAILABLE, NOT_RUNNING, PERMISSION_NEEDED, READY
)
```

## Navigation

Routes:
- `GithubStoreGraph.ProfileScreen` (data object, no params) — main profile screen
- `GithubStoreGraph.SponsorScreen` (data object, no params) — sponsor/donation page

## Implementation Notes

- **Account**: Shows GitHub user profile when logged in; login/logout with confirmation dialog. Tapping "Settings" routes to the Tweaks screen.
- **ProfileRepository.getUser():** suspend-cached via `CacheManager` (`profile:me` key). Other features call this to resolve the current user login for self-owned badges, gated features, etc. Cache invalidates on logout.
- **BuildKonfig**: Uses `convention.buildkonfig` plugin for build-time configuration.
- ViewModel depends on: `ProfileRepository`, `Platform`
- **Settings moved out:** appearance, installer type / Shizuku / Dhizuku / Root, proxy, telemetry, cache management, hidden / skipped lists, mirror picker, feedback are all owned by `feature/tweaks/` now. Don't add new settings here.
