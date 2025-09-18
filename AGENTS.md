# Repository Guidelines

## Project Structure & Module Organization
- `app/` contains the Android app; unit and instrumentation suites sit under `src/test` and `src/androidTest`.
- `core/*` supplies shared UI, design, analytics, logging, notifications, and `core/testing` utilities.
- `feature/*` hosts vertical slices (agenda, settings, detail, onboarding); keep new flows in their own feature module.
- `data/` handles repositories and persistence; `common/tasks` holds task-domain models.
- `build-logic/` ships custom Gradle convention plugins; `screenshot_testing/`, `scripts/`, and `benchmarks/` cover snapshot + baseline tooling.

## Build, Test, and Development Commands
- `./gradlew assembleDebug` builds a debuggable APK.
- `./gradlew testDebugUnitTest` runs JVM unit tests.
- `./gradlew connectedAndroidTest` launches instrumentation tests with `AtomTestRunner` (device/emulator required).
- `./gradlew detekt lintDebug` enforces Kotlin + Android linting.
- `./scripts/verify-screenshots.sh` compares Compose snapshots; `record-screenshots.sh` refreshes baselines.

## Coding Style & Naming Conventions
- Kotlin uses the official style (`kotlin.code.style=official`), 4-space indents, trailing commas when it aids diffs.
- Prefer `PascalCase` for types, `camelCase` for members, `snake_case` for resources; Compose previews belong in `*Preview.kt`.
- Keep modules isolated: share code through `core`/`common`, not direct feature cross-deps.
- Detekt (`atomtasks.detekt`) runs locally and in CI; resolve warnings before pushing.

## Testing Guidelines
- Co-locate unit tests with source modules and lean on `core/testing` for coroutine and Hilt helpers.
- Place UI and integration suites in `src/androidTest`; rely on Compose test APIs and stable semantics.
- Snapshot updates need new images in `app/screenshots/` and `screenshot_testing/`, plus a PR note.
- Aim for unit coverage on new logic and add an instrumentation or snapshot check for user-facing UI.

## Commit & Pull Request Guidelines
- Commits stay short and imperative (`Fix hardcoded string`, `Update theme`), referencing issues as `(#123)` when needed.
- Rebase or tidy history before opening a PR; avoid merge commits.
- PR descriptions should state intent, local testing, and UI impact (attach screenshots or screencasts).
- Link issues, call out config or feature flag changes, and note any follow-up work.

## Security & Configuration Tips
- Signing uses `SIGNING_*` env vars; never commit keystores or secrets.
- `local.properties` is for local SDK paths only. Firebase config lives in `app/google-services.json`; coordinate with maintainers before altering analytics.
