# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Daily Recall is a personal Android app (single module) for recording key events from the previous day and reviewing them later. Kotlin + Jetpack Compose + Room.

## Build & run

No `gradlew` wrapper script is checked in (only `gradle/wrapper/gradle-wrapper.properties`, pinned to Gradle 8.9), so use a local `gradle` install or open the project in Android Studio and let it sync/generate the wrapper.

```
gradle assembleDebug     # build the debug APK
gradle build             # full build (compile, lint, etc.)
gradle installDebug      # install on a connected device/emulator
```

There are no tests in the repo yet.

Primary way to develop: open the `DailyRecall` folder (the one containing `settings.gradle.kts`) in Android Studio, let Gradle sync, run on an emulator or a USB-debugging-enabled device.

## Architecture

Single `:app` module, package `com.dan.dailyrecall`, min/target/compile SDK 26/35/35, Kotlin 2.0, Java 17.

- **`data/`** — persistence layer, built on Room.
  - `Entities.kt`: `Entry` (one row per recalled day, keyed by unique ISO `date` string, not the primary key), `Label` (people/places/tags share a single table distinguished by `LabelType`), and `EntryLabelCrossRef` (many-to-many join, cascade delete). `EntryWithLabels` is the `@Relation`-embedded read model used everywhere entries are displayed.
  - `Daos.kt`: `EntryDao` and `LabelDao`. Search is a plain `LIKE` query across entry text fields and label names (see comment in `EntryDao.search` — intentionally not FTS since it's a personal journal; revisit only if it's slow). `LabelDao.suggest`/`popular` back the autocomplete chips.
  - `AppDatabase.kt`: builds the Room database (`daily_recall.db`), no migrations defined yet (`version = 1`).
  - `EntryRepository.kt`: the only entry point the UI talks to. `save(EntryDraft)` does an upsert-by-date inside `withTransaction`, then re-resolves labels (find-or-create) and replaces the entry's cross-refs wholesale (`clearLabels` + re-insert) rather than diffing. Tag names are lowercased for dedup; person/place names keep their typed casing but match case-insensitively via a `NOCASE` column collation.
- **`ui/`** — Compose screens + view models, one screen per feature, no shared UI state container.
  - `Navigation.kt` (`AppNav`): two-route `NavHost` — `timeline` and `editor/{date}`. "Add" always opens yesterday's date; opening an existing entry passes its date. There's no "new empty entry for today" route — the editor is always addressed by date and upserts.
  - `TimelineScreen.kt` / `TimelineViewModel.kt`: entry list with a debounced (200ms) search-as-you-type box, driven by `flatMapLatest` over `EntryRepository.observeEntries`.
  - `EditorScreen.kt` / `EditorViewModel.kt`: single-day editor. `EditorViewModel.changeDay(delta)` is the day-to-day nav (prev/next arrows, presumably) — it autosaves the current day first *if* `EditorUiState.hasContent` is true, then loads the target date. `save()` is explicit (e.g. a "Done" action).
  - `ChipInput.kt`: the shared multi-value input used for people/places/tags on the editor screen. Handles chip creation (comma or IME "Done"), autocomplete suggestions (popular labels when empty, prefix match while typing), and commits any half-typed text as a chip on focus loss.
  - View models are wired up ad hoc at each call site via `viewModelFactory { initializer { ... } }` reading `DailyRecallApp.repository` off `LocalContext` — there's no DI framework.
- **`DailyRecallApp.kt`**: `Application` subclass holding the lazily-built `AppDatabase` and `EntryRepository` singletons — the only shared state/composition root in the app.

## Not built yet

Per the README: daily reminder notification, export/backup, delete entry, mood charts, "on this day" recall view. Keep this in mind before assuming a feature is missing due to a bug rather than being unimplemented.
