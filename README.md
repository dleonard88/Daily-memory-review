# Daily Recall

Personal Android app for recording key events from the previous day and reviewing them later.

## Run it
1. Open the `DailyRecall` folder in Android Studio and let Gradle sync.
   (Versions are pinned to a known-good set; if Studio suggests upgrades, accepting them is fine.)
2. Enable Developer options and USB debugging on your phone, plug it in, pick it as the run target, press Run.
   Or use the emulator.

## Structure
- `data/`  Room entities, DAOs, database, repository
- `ui/`    Compose screens (Timeline, Editor), reusable `ChipInput`, view models, navigation

## Not built yet
Daily reminder notification, export/backup, delete entry, mood charts, "on this day".
