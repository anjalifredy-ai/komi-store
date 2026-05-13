# CLAUDE.md - Search Feature

## Purpose

Repository search with advanced filters. Users can search GitHub repositories by query and filter by platform (Android, Windows, macOS, Linux), programming language, and sort order. Supports paginated results.

## Module Structure

```
feature/search/
├── domain/
│   ├── model/
│   │   ├── SearchPlatform.kt         # All, Android, Windows, macOS, Linux
│   │   ├── ProgrammingLanguage.kt    # Language filter options
│   │   └── SortBy.kt                 # Sort options (stars, updated, etc.)
│   └── repository/SearchRepository.kt  # Filtered, paginated search
├── data/
│   ├── di/SharedModule.kt            # Koin: searchModule
│   ├── repository/SearchRepositoryImpl.kt  # GitHub search API integration
│   ├── dto/                           # Network DTOs
│   └── mappers/                       # DTO → domain model mappers
└── presentation/
    ├── SearchViewModel.kt             # Search state, filter management, pagination
    ├── SearchState.kt                 # query, results, filters, loading state
    ├── SearchAction.kt                # Search, filter changes, load more, clicks
    ├── SearchEvent.kt                 # One-off events
    ├── SearchRoot.kt                  # Main composable with search bar + filter dropdowns
    └── components/                    # Filter UI components
```

## Key Interfaces

```kotlin
interface SearchRepository {
    fun searchRepositories(
        query: String,
        searchPlatform: SearchPlatform,
        language: ProgrammingLanguage,
        sortBy: SortBy,
        page: Int
    ): Flow<PaginatedDiscoveryRepositories>
}
```

## Navigation

Route: `GithubStoreGraph.SearchScreen` (data object, no params)

## Implementation Notes

- Platform filter maps to GitHub topic searches (e.g., `android` topic for Android platform)
- Language filter maps to GitHub's `language:` qualifier
- Search results use the same `PaginatedDiscoveryRepositories` model as home feature
- Debounce/throttle applied to search queries to avoid excessive API calls
- Integrates with favourites and starred status from core repositories
- `SearchViewModel` also injects `SeenReposRepository`, `HiddenReposRepository`, `TweaksRepository`, `ProfileRepository`, `TelemetryRepository`, `SearchHistoryRepository`, `ClipboardHelper`, `ShareManager` — covers seen-filter, hide-from-discovery, history, clipboard URL detection, share
- `computeVisibleRepos` filters `state.repositories` at render time by `hiddenRepoIds` AND (when `isHideSeenEnabled`) `seenRepoIds`; unhide restores the row without re-fetching
- Empty-grid-after-Hide-seen banner offers a one-tap reset (issue #574) — `OnDisableHideSeenForResults` flips the global tweak
- Long-press on a card opens the shared `RepositoryActionsBottomSheet` (Share / Open on GitHub / Mark seen / Hide)
- `DiscoveryRepositoryUi.isCurrentUserOwner` flipped by `observeCurrentUser` (E20 self-owned badge)
