# Atom Tasks - Gemini Context

## Project Overview
**Atom Tasks** is a modern Android application designed for task organization. It is built entirely with **Kotlin** and **Jetpack Compose**, following Clean Architecture, SOLID principles, and the MVI (Model-View-Intent) pattern.

The project emphasizes modularization, robustness (high test coverage including snapshot tests), and the use of cutting-edge Android development tools.

## Technology Stack
- **Language:** Kotlin (100%)
- **UI:** Jetpack Compose (Material 3)
- **Navigation:** Compose Destinations (wrapper around Jetpack Navigation)
- **Dependency Injection:** Hilt
- **Persistence:** Room Database
- **Concurrency:** Kotlin Coroutines & Flows
- **Architecture:** MVI with `StateFlow`
- **Linting/Static Analysis:** Detekt
- **Build System:** Gradle (Kotlin DSL) with Version Catalog (`libs.versions.toml`)

## Project Structure & Architecture
The codebase is modularized to separate concerns and improve build times:

*   **`app/`**: The main application module, handling application-level configuration and dependency graphs. Contains build variants (`development`, `production`).
*   **`core/`**: Shared functional modules used across features (e.g., `designsystem`, `analytics`, `logging`, `ui`, `testing`).
*   **`feature/`**: Independent feature modules representing vertical slices of the app (e.g., `agenda`, `settings`, `onboarding`).
*   **`data/`**: Data access layer, including repositories and the Room database schema.
*   **`common/`**: Shared domain logic (e.g., `common/tasks`).
*   **`build-logic/`**: Custom Gradle convention plugins for consistent build configuration across modules.
*   **`screenshot_testing/`**: Infrastructure for snapshot testing.
*   **`scripts/`**: Shell scripts for automating tasks like screenshot verification.

## Development Workflow

### Build & Run
*   **Build Debug APK:**
    ```bash
    ./gradlew assembleDebug
    ```
*   **Run Unit Tests:**
    ```bash
    ./gradlew testDebugUnitTest
    ```
*   **Run Instrumentation Tests:**
    ```bash
    ./gradlew connectedAndroidTest
    ```

### Code Quality & Formatting
*   **Linting (Detekt):**
    ```bash
    ./gradlew detekt
    ```
    *Ensure all Detekt warnings are resolved before committing.*

### Screenshot Testing
*   **Verify Screenshots:**
    ```bash
    ./scripts/verify-screenshots.sh
    ```
*   **Record/Update Screenshots:**
    ```bash
    ./scripts/record-screenshots.sh
    ```

## Key Configuration Files
*   **Dependencies:** `gradle/libs.versions.toml` (Version Catalog)
*   **Build Logic:** `build-logic/` (Convention plugins)
*   **CI/CD:** `.github/workflows/`
*   **App Config:** `app/build.gradle.kts`

## Coding Conventions
*   **Style:** Follows the official Kotlin coding conventions.
*   **Architecture:** Adheres to Clean Architecture boundaries. Features should not depend on other features directly.
*   **Testing:**
    *   **Unit Tests:** Required for logic.
    *   **Snapshot Tests:** Used for UI components (Jetpack Compose).
    *   **Integration Tests:** Located in `src/androidTest`.
*   **Commits:** Use imperative mood (e.g., "Fix bug", "Add feature").

## Notes for Gemini
*   When adding dependencies, always check and update `gradle/libs.versions.toml`.
*   Respect the modular structure; do not introduce circular dependencies.
*   Use `codebase_investigator` for complex architectural queries, as the modular graph can be deep.
*   Refer to `AGENTS.md` for specific repository guidelines if available.
*   When finishing a task, always make sure that lint (Detekt) and tests tasks pass.
