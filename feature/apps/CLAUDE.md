# CLAUDE.md - Apps Feature

## Purpose

Manages installed applications. Lists all apps installed through GitHub Store, allows launching them, and checks for available updates. Primarily relevant on **Android** (apps section is hidden on Desktop).

## Module Structure

```
feature/apps/
├── domain/
│   └── repository/AppsRepository.kt   # Installed apps, launch, update check
├── data/
│   ├── di/SharedModule.kt            # Koin: appsModule
│   └── repository/AppsRepositoryImpl.kt  # Implementation using core InstalledAppsRepository
└── presentation/
    ├── AppsViewModel.kt               # State management for installed apps list
    ├── AppsState.kt                   # apps list, loading, error, search query, sort rule
    ├── AppsAction.kt                  # Refresh, OpenApp, CheckUpdates, OnSortRuleSelected, OnSearchChange, OnLifecycleResume, OnIgnoreUpdate, OnSkipRelease, etc.
    ├── AppsEvent.kt                   # One-off events
    ├── AppsRoot.kt                    # Main composable (sectioned list with sort dropdown + search bar)
    ├── components/                    # App item cards, update badges, LinkAppBottomSheet, AdvancedAppSettingsBottomSheet, ApkInspectSheet, import banner
    ├── import/                        # External-import wizard (ExternalImportRoot/ViewModel) — Obtainium import/export + manual link import
    └── starred/                       # Add-from-starred picker (StarredPickerRoot/ViewModel) — APK-shipping subset of user's GitHub stars
```

## Key Interfaces

```kotlin
interface AppsRepository {
    suspend fun getApps(): Flow<List<InstalledApp>>
    suspend fun openApp(installedApp: InstalledApp, onCantLaunchApp: () -> Unit = {})
    suspend fun getLatestRelease(owner: String, repo: String): GithubRelease?
}
```

## Navigation

Route: `GithubStoreGraph.AppsScreen` (data object, no params)

## Implementation Notes

- Uses `InstalledAppsRepository` and `SyncInstalledAppsUseCase` from core/domain
- `openApp()` uses `AppLauncher` from core/domain to launch the installed app
- `getLatestRelease()` checks if a newer version is available
- Platform-specific: `PackageMonitor` and `Installer` handle Android package management
- The apps section in the home screen bottom nav is only visible on `Platform.ANDROID`
- **Sort + search:** `AppSortRule` enum (UpdatesFirst default, AlphabeticalAZ, RecentlyAdded, RecentlyUpdated). Persisted in DataStore. Inline search bar filters by appName / packageName client-side
- **Per-app actions:** Ignore-updates (silences badge for the app), Skip-this-release (per-tag skip with auto-clear on next release), Advanced filter (regex on asset names + monorepo fallback), Pin variant (token-set + glob fingerprint), Inspect APK (decoded manifest sheet — package, signing, permissions, components)
- **Auto-update on resume:** `AppsAction.OnLifecycleResume` fires `autoCheckForUpdatesIfNeeded` (30-min cooldown) — catches drift when an external install landed while GHS was background-killed
- **External import wizard (`import/`):** Obtainium JSON import/export with pre-import summary (imported / already-tracked / non-GitHub-skipped buckets); manual-link-only add path; survey-signal "import from URL"
- **Starred picker (`starred/`):** scans the signed-in user's GitHub stars, surfaces APK-shipping repos with sort/filter, opens Details on tap. Resumes mid-scan if GitHub rate-limits
