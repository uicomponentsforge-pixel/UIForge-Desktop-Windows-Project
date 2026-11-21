# UIForge Desktop — Windows Edition (conversion foundation)

This is a separate Windows desktop target. The original Android project remains in `app/` and is not modified by this module.

## What this first Windows milestone includes

- Native Windows application packaging targets (EXE and MSI) through Compose Desktop.
- Desktop wallpaper assets reused from the Android project.
- Desktop shortcuts, taskbar, clock, Start menu, app launcher, and wallpaper selection.
- A basic app-window shell for the current built-in app catalogue.

## Build

Requires JDK 17 and Gradle 8.10.2 or compatible.

From this directory:

```powershell
gradle packageReleaseExe packageReleaseMsi
```

Outputs are generated under `build/compose/binaries/main-release/`.

The repository workflow `.github/workflows/build-windows.yml` can build the installers on a GitHub-hosted Windows runner. Run it from the Actions tab with **Build UIForge Desktop for Windows**.

## Porting status

This is the first Windows foundation, not a completed feature-for-feature port. The Android implementation still contains Android-only Activity, DataStore, Room, file-picker, system-information, and Android intent APIs. Those need Windows implementations before the original apps and data behavior can be fully migrated.
