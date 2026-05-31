# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

Use Gradle wrapper from the project root (PowerShell on Windows):

```powershell
# Build debug APK
.\gradlew assembleDebug

# Run unit tests
.\gradlew test

# Run instrumented tests (requires connected device/emulator)
.\gradlew connectedAndroidTest

# Run a single unit test class
.\gradlew test --tests "com.example.thefilamentrack.ExampleUnitTest"

# Install and run on connected device
.\gradlew installDebug
```

There is no lint or code-style tooling configured beyond what the Android Gradle plugin provides. KSP (`ksp`) is used for Room annotation processing — any new Room entities/DAOs need to be added to `AppDatabase` and KSP will generate the implementation on the next build.

## Architecture

**MVVM, single-Activity, 100% Jetpack Compose.** No XML layouts exist anywhere.

```
FilamentRackApplication  ←  owns Room DB + SpoolRepository (lazy singletons)
        │
        └── SpoolViewModel (AndroidViewModel)
                 │  accesses repo via (application as FilamentRackApplication).repository
                 │  exposes: allSpools: StateFlow<List<SpoolEntity>>
                 │           nfcTag: SharedFlow<Tag>  ← NFC events flow through here
                 └── SpoolRepository → SpoolDao → Room ("filament-db")

MainActivity  ←  hosts NavHost, owns NFC foreground-dispatch lifecycle
```

**Navigation** (`Navigation.kt`) uses a sealed `Screen` class with string routes. All screen-to-screen navigation lives in `MainActivity`'s `NavHost`; screens receive lambdas for actions rather than a `NavController` reference.

**NFC flow** (`nfc/NfcManager.kt`, `MainActivity.kt`, `SpoolViewModel.kt`):
- Foreground dispatch is enabled in `onResume`/`onPause`.
- `onNewIntent` calls `viewModel.dispatchNfcTag(tag)`, which emits on `nfcTag: SharedFlow<Tag>`.
- `MainActivity` has a `LaunchedEffect(Unit)` that collects from `nfcTag` and handles **SpoolList** navigation only (reads `navController.currentBackStackEntry` inline to get the live route).
- **SpoolDetailScreen** and **AddEditSpoolScreen** subscribe directly: a `LaunchedEffect(nfcWaitMode)` / `LaunchedEffect(awaitingNfc)` suspends on `viewModel.nfcTag.first()` only when the user has pressed an NFC button. Cancelling the dialog sets the mode back to `None`, which restarts the effect and cancels the pending `first()`.
- `NfcManager` supports both already-formatted NDEF tags and blank unformatted tags (`NdefFormatable`). `writeSpoolId()` returns `Boolean`; call sites in `SpoolDetailScreen` check the result and show a snackbar on failure.

**Data layer** (`data/` package):
- `SpoolEntity` — single Room table `spools`, auto-increment `id`, optional `nfcTagId: String?`
- `SpoolDao` — standard CRUD + `updateNfcTag` targeted query
- `SpoolRepository` — thin wrapper; exists mainly to separate DAO from ViewModel
- DB is constructed once in `FilamentRackApplication` (no `fallbackToDestructiveMigration`). Schema is version 1 — add explicit `Migration` objects to the builder before any schema change.

**Shared UI utilities** (`ui/SpoolUiUtils.kt`):
- `colorFromHex(hex)` — safe color parsing (returns `Color.Gray` on failure)
- `progressColorFor(pct)` — green/orange/red threshold coloring
Use these in any new screen rather than inlining the logic.

**State initialization pattern in edit screens** (`AddEditSpoolScreen.kt`):
Because `allSpools` starts as `emptyList()` (the `stateIn` initial value), form fields are initialized to defaults and populated via `LaunchedEffect(existing)` once the DB emits data, guarded by a `fieldsLoaded` flag. Follow this pattern for any new screen that edits an existing entity.

**Weight validation** (`AddEditSpoolScreen.kt`):
Save is disabled when `remainingWeight > totalWeight` or when either field is not a valid float. The remaining weight field shows `isError = true` and an error message is shown below the row.
