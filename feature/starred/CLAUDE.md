# CLAUDE.md - Starred Feature

## Purpose

Displays the user's locally saved starred repositories. This is a **presentation-only** feature with no domain or data layer -- it uses `StarredRepository` from `core/domain` directly.

## Module Structure

```
feature/starred/
└── presentation/
    ├── StarredReposViewModel.kt       # Observes starred repos, handles remove
    ├── StarredReposState.kt           # starred list, loading
    ├── StarredReposAction.kt          # RemoveStarred, click actions
    ├── StarredReposRoot.kt            # Main composable (list of starred repos)
    ├── model/StarredRepositoryUi.kt   # UI model for display
    ├── mappers/StarredRepoToUiMapper.kt  # Domain → UI model mapper
    ├── utils/TimeFormatUtils.kt       # Time formatting utilities
    └── components/StarredRepositoryItem.kt  # Individual starred repo card
```

## Key Dependencies

- `StarredRepository` (from `core/domain`) - CRUD operations for starred repos
- `FavouritesRepository`, `AuthenticationState` (from `core/domain`) - favourite-status overlay + login gating
- `ProfileRepository` (from `feature/profile/domain`) - current user login for E20 self-owned ✓ badge
- Starred repos are stored locally in Room database (`StarredRepoDao` in `core/data`)

## Navigation

Route: `GithubStoreGraph.StarredReposScreen` (data object, no params)

## Implementation Notes

- Periodic sync against GitHub's `/user/starred` (gated on `isAuthenticated`); local Room mirror is the read source
- Uses a presentation-layer `StarredRepositoryUi` model mapped from the domain `StarredRepository` entity
- Starring happens in other features (home, details, search); this feature only displays and removes
- Includes its own `TimeFormatUtils` for formatting timestamps on starred items
- Inline search bar (E562) when the list is non-empty — filters by name / owner / description / language client-side. `OnRefresh` clears the active query so a refreshed list isn't masked behind a stale filter
- `StarredRepositoryUi.isCurrentUserOwner` field flips when the signed-in user owns the repo (E20)
- The Koin module for this feature is registered in `composeApp/.../app/di/ViewModelsModule.kt` since there's no `data/di/` layer
