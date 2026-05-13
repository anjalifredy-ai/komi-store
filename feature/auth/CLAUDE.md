# CLAUDE.md - Auth Feature

## Purpose

GitHub OAuth authentication using the **device flow**. Users authenticate by visiting a URL and entering a code displayed in the app. No browser redirect needed, making it suitable for both Android and Desktop.

## Module Structure

```
feature/auth/
├── domain/
│   └── repository/AuthenticationRepository.kt  # Device flow interface
├── data/
│   ├── di/SharedModule.kt            # Koin: authModule
│   ├── repository/AuthenticationRepositoryImpl.kt  # OAuth device flow implementation
│   └── network/GitHubAuthApi.kt      # GitHub OAuth API endpoints
└── presentation/
    ├── AuthenticationViewModel.kt     # Manages device flow lifecycle
    ├── AuthenticationState.kt         # Code, URL, loading, error
    ├── AuthenticationAction.kt        # StartAuth, Cancel, etc.
    ├── AuthenticationEvent.kt         # One-off events
    ├── AuthenticationRoot.kt          # UI: displays code + verification URL
    └── components/                    # Auth UI components
```

## Key Interfaces

```kotlin
interface AuthenticationRepository {
    val accessTokenFlow: Flow<String?>
    suspend fun startDeviceFlow(): GithubDeviceStart
    suspend fun awaitDeviceToken(start: GithubDeviceStart): GithubDeviceTokenSuccess
}
```

## Navigation

Route: `GithubStoreGraph.AuthenticationScreen` (data object, no params)

## Implementation Notes

- Uses GitHub's [device authorization flow](https://docs.github.com/en/apps/oauth-apps/building-oauth-apps/authorizing-oauth-apps#device-flow)
- `startDeviceFlow()` returns a user code + verification URL to display
- `awaitDeviceToken()` polls GitHub until the user completes verification
- Token is stored via `TokenStore` in core/data (DataStore-backed)
- `GITHUB_CLIENT_ID` must be set in `local.properties` for builds
- `accessTokenFlow` is observed app-wide by `MainViewModel` for auth state
- **Backend-proxied path:** Primary path calls `/v1/auth/device/start` + `/v1/auth/device/poll` on the GitHub-Store backend so users on networks that throttle `github.com` (China, corporate filters) can still log in. Each session picks one `AuthPath` (`Backend` or `Direct`) at start and sticks to it; `AuthenticationRepositoryImpl` only escalates `Backend → Direct` on infrastructure errors (timeout / 5xx). HTTP 4xx and GitHub's negative 200-bodies (`authorization_pending`, `slow_down`, `access_denied`, `expired_token`, `bad_verification_code`) are real answers, never cause fallback
- **`AuthPath` persisted in `SavedStateHandle`** as `auth_path` so activity recreation resumes on the same path
- **Backend rate limits** (10 starts/hr, 200 polls/hr per IP) are hard — don't add retry loops on top of Ktor's `HttpRequestRetry(maxRetries = 2)`
- Backend responses carry `X-Request-ID` — `GitHubAuthApi` embeds it in every error message via `asRequestIdTag()` so bug reports cite the ID and it maps straight to backend logs
- Both paths share the same OAuth App — client-side `GITHUB_CLIENT_ID` must match backend's `GITHUB_OAUTH_CLIENT_ID`. Backend endpoints in `core/data/network/BackendEndpoints.kt`
