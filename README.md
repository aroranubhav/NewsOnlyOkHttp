# NewsOnlyOkHttp

An Android application that displays a list of news sources using the [NewsAPI](https://newsapi.org). The primary purpose of this project is to demonstrate **Clean Architecture** principles combined with modern Android development tools, with a strong focus on **OkHttp interceptor-based caching** — without any local database persistence.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Error Handling](#error-handling)
- [OkHttp Caching Strategy](#okhttp-caching-strategy)
- [Setup & Installation](#setup--installation)
- [Testing](#testing)
- [CI/CD](#cicd)

---

## Overview

- Fetches and displays a list of news sources from NewsAPI
- Network-only data layer — no local database (Room is available as a dependency but not used for persistence in this project)
- Caching handled entirely by OkHttp interceptors
- UI built with Jetpack Compose
- ViewModel separates **UI States** (loading, success, no data) from **UI Events** (errors, snackbars, messages)

---

## Architecture

The project follows **Clean Architecture** with three distinct layers:

```
presentation  →  domain  →  data
```

Each layer has a single responsibility and dependencies only point inward — the domain layer knows nothing about Android or the network.

### Layers

**Domain Layer** — pure Kotlin, no Android dependencies
- `model/` — domain data classes (`NewsSource`)
- `repository/` — repository interfaces
- `usecase/` — use case interfaces and their default implementations

**Data Layer** — implements domain contracts
- `sources/remote/` — `RemoteDataSource` wrapping the Retrofit API service
- `repository/` — `DefaultNewsSourcesRepository` coordinating data flow
- `mappers/` — DTO to domain model mapping functions
- `common/` — `safeApiCall` wrapper, constants

**Presentation Layer**
- `viewmodel/` — ViewModels exposing `StateFlow` for UI state and `SharedFlow` for one-time events
- `screens/` — Composable screens
- `common/` — shared UI constants, state and event definitions

**Common** — shared across all layers
- `Resource` — sealed class representing `Success`, `Error`, `Loading`, and `NoChange`
- `ErrorType` — enum classifying error categories
- `DispatcherProvider` — abstraction over coroutine dispatchers for testability
- `NetworkConnectivityHelper` — network state helper

---

## Tech Stack

| Category | Library |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | Clean Architecture, MVVM |
| DI | Hilt |
| Networking | Retrofit, OkHttp |
| Serialization | Kotlinx Serialization |
| Navigation | Jetpack Navigation Compose |
| Async | Kotlin Coroutines, Flow |
| Caching | OkHttp Cache + Interceptors |
| Testing | JUnit 4, MockK, Turbine, MockWebServer, Google Truth |
| CI/CD | GitHub Actions |

---

## Project Structure

```
app/src/main/java/com/maxi/newsonlyokhttp/
│
├── common/
│   ├── Resource.kt                  # Sealed class: Success, Error, Loading, NoChange
│   ├── ErrorType.kt                 # Enum: NO_CONNECTIVITY, TIMEOUT, UNAUTHORISED, etc.
│   ├── DispatcherProvider.kt        # Coroutine dispatcher abstraction
│   ├── NetworkConnectivityHelper.kt # Network state helper
│   ├── ApiException.kt              # Base exception class for all HTTP errors
│   └── TransportException.kt        # Sealed class for transport-level failures
│
├── data/
│   ├── common/
│   │   ├── safeApiCall.kt           # Centralised API call wrapper with error handling
│   │   └── Constants.kt
│   ├── mappers/
│   │   └── NewsSourceMapper.kt      # DTO → Domain model mapping
│   ├── repository/
│   │   └── DefaultNewsSourcesRepository.kt
│   └── sources/
│       └── remote/
│           ├── RemoteDataSource.kt
│           ├── NetworkApiService.kt
│           ├── SourcesResponseDto.kt
│           └── NewsSourceDto.kt
│
├── domain/
│   ├── model/
│   │   └── NewsSource.kt
│   ├── repository/
│   │   └── NewsSourcesRepository.kt
│   └── usecase/
│       └── GetNewsSourcesUseCase.kt
│
├── framework/
│   └── di/
│       ├── qualifiers/
│       │   └── Qualifiers.kt        # Hilt binding qualifiers
│       ├── AppModule.kt             # OkHttp, Retrofit, Hilt singletons
│       ├── RepositoryModule.kt
│       └── UseCaseModule.kt
│
└── ui/
    ├── common/                      # UI constants, UiState, UiEvent definitions
    ├── screens/                     # Composable screens
    └── viewmodel/                   # ViewModels
```

---

## Error Handling

The project uses a structured, layered exception hierarchy to map every possible failure to a typed `Resource.Error`.

### Exception Types

**`ApiException`** — base class for all HTTP-related exceptions, carrying `errorCode`, `errorMessage`, `errorBody`, `requestMethod`, and `requestUrl`.

**`HttpException`** (extends `ApiException`) — sealed class for HTTP status code errors:
- `Unauthorized` — 401
- `Forbidden` — 403
- `NotFound` — 404
- `ServerError` — 5xx
- `Unknown` — any other HTTP error

**`TransportException`** — sealed class for network transport failures:
- `NoConnectivity` — device has no internet connection
- `Timeout` — connection or read timeout exceeded
- `Unknown` — any other transport-level failure

### `safeApiCall`

All network calls are wrapped in `safeApiCall`, which catches every exception type and maps it to the appropriate `Resource.Error` with the correct `ErrorType`. It also handles the 304 Not Modified response by returning `Resource.NoChange`, and re-throws `CancellationException` to respect coroutine cancellation.

```
Network Call
    └── safeApiCall
            ├── 304 → Resource.NoChange
            ├── Success body → Resource.Success
            ├── Empty body → Resource.Error(UNKNOWN)
            ├── TransportException.NoConnectivity → Resource.Error(NO_CONNECTIVITY)
            ├── TransportException.Timeout → Resource.Error(TIMEOUT)
            ├── HttpException.Unauthorized → Resource.Error(UNAUTHORISED)
            ├── HttpException.Forbidden → Resource.Error(FORBIDDEN)
            ├── HttpException.NotFound → Resource.Error(NOT_FOUND)
            ├── HttpException.ServerError → Resource.Error(SERVER_ERROR)
            └── IOException / Unknown → Resource.Error(UNKNOWN)
```

---

## OkHttp Caching Strategy

Caching is handled entirely at the network layer via OkHttp interceptors — there is no local database.

| Interceptor | Role |
|---|---|
| `AuthorizationInterceptor` | Adds the API key and User-Agent headers to every request |
| `CacheControlInterceptor` | Network interceptor that sets cache headers on responses |
| `ErrorHandlingInterceptor` | Parses error responses and throws typed `HttpException` subclasses |
| `HttpLoggingInterceptor` | Logs requests and responses in debug builds only |

The cache is stored in the app's cache directory with a maximum size of 10 MB. The `X-Force-Refresh` header is passed through to the interceptor layer, allowing the UI to bypass the cache and force a fresh network request when needed.

---

## Setup & Installation

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17
- A NewsAPI key — obtain one free at [newsapi.org](https://newsapi.org)

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/aroranubhav/NewsOnlyOkHttp.git
   cd NewsOnlyOkHttp
   ```

2. **Create `local.properties`** in the project root (this file is gitignored and must not be committed):
   ```properties
   API_KEY=your_newsapi_key_here
   USER_AGENT=your_app_name_or_identifier
   ```
   - `API_KEY` — your key from newsapi.org. It is Base64-encoded at build time and decoded at runtime.
   - `USER_AGENT` — a string to identify your client to the API (e.g. `NewsOnlyOkHttp/1.0`). Useful when the API is under load.

3. **Build and run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or simply open the project in Android Studio and run it on a device or emulator.

---

## Testing

The project uses a layered testing strategy, starting from the innermost domain layer and moving outward.

### Test Libraries

| Library | Purpose |
|---|---|
| JUnit 4 | Test runner and structure |
| MockK | Mocking classes and suspend functions in Kotlin |
| Turbine | Testing Kotlin Flow emissions cleanly |
| Google Truth | Fluent, readable assertions |
| Kotlinx Coroutines Test | Controlled testing of coroutines and suspend functions |
| MockWebServer | Simulating real HTTP responses for network layer tests |
| Robolectric | Running Android-dependent code on the JVM |

### What's Tested

**Use Case layer** (`GetNewsSourcesUseCaseTest`)
- Verifies that `Loading`, `Success`, and `Error` emissions are passed through from the repository unchanged
- Verifies that `forceRefresh` is correctly delegated to the repository

**Repository layer** (`DefaultNewsSourcesRepositoryTest`)
- Verifies `Loading` is always the first emission
- Verifies `Success` response is correctly mapped from DTO to domain model
- Verifies `304` response maps to `Resource.NoChange`
- Verifies empty body maps to `Resource.Error(UNKNOWN)`
- Verifies each exception type (`NoConnectivity`, `Timeout`, `Unauthorized`, `ServerError`, `IOException`) maps to the correct `ErrorType`

### Running Tests

```bash
# Run all unit tests
./gradlew test

# Run tests for a specific class
./gradlew testDebugUnitTest --tests "*.GetNewsSourcesUseCaseTest"
```

---

## CI/CD

The project uses **GitHub Actions** for continuous integration. The pipeline is defined in `.github/workflows/ci.yml`.

### What It Does

On every push to `main` and every pull request targeting `main`, the pipeline:
1. Checks out the code on a clean Ubuntu runner
2. Sets up JDK 17
3. Caches Gradle dependencies for faster subsequent runs
4. Generates a `local.properties` with placeholder keys (sufficient for unit tests since no real network calls are made)
5. Runs all unit tests via `./gradlew test`
6. Uploads the test report as a downloadable artifact — even on failure

### Branch Protection

The `main` branch is protected by a GitHub Ruleset that requires the `Run Unit Tests` check to pass before any pull request can be merged. This ensures broken code can never land on `main`.
