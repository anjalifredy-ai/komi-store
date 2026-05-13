# CLAUDE.md - Recently Viewed Feature

## Purpose

Displays the user's local "recently viewed" history — every repo whose Details screen was opened. Presentation-only feature backed directly by `SeenReposRepository` from `core/domain`. Same seen-tracking pipeline that drives the "Hide seen" filter on Home/Search.

## Module Structure

```
feature/recently-viewed/
└── presentation/
    ├── RecentlyViewedViewModel.kt       # Observes seen-repos, exposes UI list
    ├── RecentlyViewedState.kt           # repos list + loading
    ├── RecentlyViewedAction.kt          # OnRepositoryClick, OnRemove, OnClearAll, OnDeveloperProfileClick
    ├── RecentlyViewedRoot.kt            # Main composable (grid)
    ├── model/RecentlyViewedRepo.kt      # UI model
    ├── mappers/RecentlyViewedRepoMapper.kt  # SeenRepo (domain) → UI
    └── components/RecentlyViewedItem.kt # Single-row card
```

## Key Dependencies

- `SeenReposRepository` (`core/domain`) — CRUD over the `seen_repos` Room table.
- Visited timestamps come from `seenRepoDao.insert(...)` calls in `DetailsViewModel` whenever the user opens a Details page.

## Navigation

Route: `GithubStoreGraph.RecentlyViewedScreen` (data object, no params).

## Implementation Notes

- No network calls — entirely local Room.
- "Clear all" wipes the seen_repos table via `SeenReposRepository.clearAll()`.
- Single-row remove via `SeenReposRepository.removeFromHistory(repoId)`.
- The Koin module for this feature is registered in `composeApp/.../app/di/ViewModelsModule.kt` since there's no `data/di/` layer.
- Cards reuse `feature/favourites`'s visual language for cross-feature consistency.
