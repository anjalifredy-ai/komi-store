# CLAUDE.md - Tweaks Feature

## Purpose

Single home for every app-level setting. Covers update preferences, installer choice (Default / Shizuku / Dhizuku / Root), telemetry, translation provider, mirror selection, feedback, and the management screens for skipped updates + hidden repositories. Replaces the old `feature/settings/` and absorbs the settings half of `feature/profile/`. Reached from the bottom navigation bar.

## Module Structure

```
feature/tweaks/
└── presentation/
    ├── TweaksViewModel.kt              # Main settings state machine
    ├── TweaksState.kt                  # all settings + loading
    ├── TweaksAction.kt                 # toggles, selections, navigations
    ├── TweaksEvent.kt                  # snackbars, restarts
    ├── TweaksRoot.kt                   # Scrollable LazyColumn of sections
    ├── RestartApp.kt                   # Restart-on-locale-change helper
    ├── components/
    │   ├── sections/                   # Account, Appearance, Installation, Language, Network, Others, Translation, SettingsSection
    │   ├── ToggleSettingCard.kt        # Reusable toggle row
    │   ├── ClearDownloadsDialog.kt
    │   └── SectionText.kt
    ├── feedback/                       # FeedbackViewModel + bottom sheet for user feedback channels
    ├── hidden/                         # HiddenRepositoriesRoot — Tweaks → Updates → Hidden repositories
    ├── skipped/                        # SkippedUpdatesRoot — Tweaks → Updates → Skipped updates
    ├── mirror/                         # MirrorPickerRoot — Tweaks → Network → Mirror picker
    └── model/                          # ProxyScopeFormState, ProxyType
```

## Key Dependencies

`TweaksViewModel` injects: `TweaksRepository`, `ThemesRepository`, `ProxyRepository`, `InstalledAppsRepository`, `ProfileRepository`, `InstallerStatusProvider`, `TelemetryRepository`, `Platform`, `BatteryOptimizationManager` (Android), `GitHubStoreLogger`. Sub-features (`hidden`, `skipped`) inject the specific repository they manage (`HiddenReposRepository`, `InstalledAppsRepository`).

## Sub-screens (separate `viewModelOf` registrations in `composeApp/.../app/di/ViewModelsModule.kt`)

- `SkippedUpdatesViewModel` — list + unskip + persist via `InstalledAppsRepository.setSkippedReleaseTag`
- `HiddenRepositoriesViewModel` — list + unhide + unhide-all via `HiddenReposRepository`
- `FeedbackViewModel` — channel selection + open in browser
- `AutoSuggestMirrorViewModel` + `MirrorPickerViewModel` — mirror test/pick flow

## Navigation

- `GithubStoreGraph.TweaksScreen` — main settings screen
- `GithubStoreGraph.SkippedUpdatesScreen` — skipped-updates manager
- `GithubStoreGraph.HiddenRepositoriesScreen` — hidden-repos manager
- `GithubStoreGraph.MirrorPickerScreen` — mirror picker

## Implementation Notes

- **One-shot coachmark flags** live in `TweaksRepository` as `booleanPreferencesKey`s (`apk_inspect_coachmark_shown`, `channel_chip_coachmark_shown`). Once persisted `true`, never re-shown.
- **Default beta channel** preference (`include_pre_releases`) is read once on every new install in `InstallationManagerImpl` to seed the new row's `InstalledApp.includePreReleases`. Existing rows keep their own per-app value.
- **`Show all platforms`** preference (`show_all_platforms`) drives the cross-platform asset section in Details — set globally here, consumed by `ReleaseAssetsItemsPicker`.
- **Mirror picker** is gated by the user's locale — only suggested when the device is in a region where direct GitHub access is throttled.
- **Restart-on-language-change** uses `RestartApp.kt` to apply the new locale: persists language tag, restarts MainActivity / DesktopApp.
- The Koin module for this feature is registered in `composeApp/.../app/di/ViewModelsModule.kt`.
