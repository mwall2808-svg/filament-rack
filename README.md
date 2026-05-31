# The Filament Rack

An Android app for tracking 3D printing filament spools. Log usage, monitor remaining weight, and link spools to NFC tags for one-tap identification.

## Features

- **Spool inventory** — track brand, material, color, and weight for each spool
- **Usage logging** — subtract grams used after each print; quick-amount chips for common values
- **NFC tag support** — link a physical NFC tag to a spool by UID, or write the spool ID directly onto the tag. Scanning a linked tag from the home screen opens the log screen instantly
- **Visual indicators** — color-coded progress bars (green → orange → red) show remaining filament at a glance

## Requirements

- Android 6.0+ (API 23)
- NFC hardware is optional — all features except tag scanning work without it

## Building

Open in Android Studio or build from the command line:

```bash
./gradlew assembleDebug
./gradlew installDebug   # install on connected device
```

## Tech Stack

- Kotlin, Jetpack Compose, Material3
- Room (SQLite), StateFlow, Coroutines
- Android NFC APIs (NDEF read/write)
