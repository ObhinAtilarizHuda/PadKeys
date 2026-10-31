# PadKeys

**Turn your Android phone into a Bluetooth keyboard, mouse, and media remote.**

PadKeys uses Android's built-in Bluetooth HID Device profile, so the host sees a standard keyboard + mouse combo. No driver, dongle, or companion app is needed on the receiving device.

[Download APK](https://github.com/ObhinAtilarizHuda/PadKeys/releases/latest) · [Latest release](https://github.com/ObhinAtilarizHuda/PadKeys/releases/tag/v1.2.0) · Android 9+

![Release](https://img.shields.io/github/v/release/ObhinAtilarizHuda/PadKeys?label=release&color=2563EB)
![Android](https://img.shields.io/badge/Android-9%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.23-7F52FF?logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Compose-Material%203-4285F4)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

## Features

- **Touchpad** - move the pointer, tap to click, right-click, and scroll.
- **Full keyboard** - QWERTY layout with number row, F1-F12, symbols, arrows, Tab, Caps Lock, Shift, Ctrl, Alt, and Enter. Opens in landscape for comfortable typing.
- **Media and TV controls** - playback shortcuts and TV navigation keys.
- **Simple pairing** - make the phone discoverable, pair from the host, then reconnect from the app.
- **Auto-reconnect** - remembers the last paired host.
- **Material 3 UI** - dynamic color on Android 12+, clean layouts, no unnecessary effects.
- **Private by design** - no `INTERNET` permission. Everything stays on the local Bluetooth link.

## Requirements

- Android 9 (API 28) or newer
- Bluetooth Classic support
- A host that accepts Bluetooth HID input (PC, Mac, Linux, iPad, Android TV, or smart TV)

## Install

1. Download the signed APK from [Releases](https://github.com/ObhinAtilarizHuda/PadKeys/releases/latest).
2. Allow "Install unknown apps" for your browser or file manager.
3. Open the APK and install it.
4. If an older build is already installed with a different signature, uninstall it first.

## Pairing

1. Open PadKeys and go to the **Connect** tab.
2. Tap **Make phone discoverable**.
3. On the host, scan for Bluetooth devices and pair with **PadKeys**.
4. Return to PadKeys and tap **Reconnect** if needed.
5. Switch to **Pad**, **Keys**, **Media**, or **TV** and start using it.

> Android phones generally cannot act as the host. Android's Bluetooth stack filters out HID combo devices from another phone. Use a PC, Mac, Linux machine, tablet, or TV instead.

## Build

**Requirements:** JDK 17, Android SDK 35, and the Gradle wrapper included in this repository.

```bash
git clone https://github.com/ObhinAtilarizHuda/PadKeys.git
cd PadKeys

# Windows
gradlew.bat assembleDebug

# macOS / Linux
./gradlew assembleDebug
```

The debug APK is written to:

```text
app/build/outputs/apk/debug/app-debug.apk
```

For a release build:

```bash
gradlew.bat assembleRelease
```

The release build is unsigned by default. Sign it with your own keystore through Android Studio or `apksigner` before distributing it.

## Tech stack

| Layer | Technology |
| --- | --- |
| Language | Kotlin 1.9.23 |
| UI | Jetpack Compose, Material 3, Compose BOM 2024.04.01 |
| Architecture | MVVM, Hilt 2.51.1, Navigation Compose |
| Storage | DataStore Preferences |
| Bluetooth | Android `BluetoothHidDevice` API |
| Background | Foreground service for the HID connection |
| Build | AGP 8.5.2, Gradle 8.9, minSdk 28, targetSdk 35 |

## Project structure

```text
app/src/main/java/com/btremote/app/
├── bluetooth/     HID descriptor, report sender, connection service, QS tile
├── data/          Preferences and paired-device storage
├── sensor/        Motion sensor code (currently unused)
└── ui/
    ├── components/  Shared UI components
    ├── navigation/  Compose navigation graph
    ├── screens/     Touchpad, keyboard, media, TV, connect, settings, onboarding
    └── theme/       Material 3 color and typography
```

## Compatibility

| Host | Status |
| --- | --- |
| Windows 10 / 11 | Works |
| macOS 12+ | Works |
| Linux (BlueZ 5.50+) | Works |
| iPadOS / iOS 14+ | Works |
| Android TV / Google TV | Works |
| LG / Samsung smart TVs | Usually works |
| Android phone as host | Not supported |

Bluetooth HID availability depends on the phone manufacturer. Some OEM builds disable the HID Device profile.

## Privacy

PadKeys does not request internet access and does not collect analytics. The only stored data is local app preferences, such as the last paired host and UI settings.

## Credits

PadKeys is based on [Linkpad](https://github.com/Devdas-gupta/linkpad) by **Devdas Kumar**. Thanks to the original author for releasing it under the MIT license.

This fork changes the branding, UI, keyboard layout, pairing flow, and removes the air mouse feature.

## License

MIT. See [LICENSE](LICENSE) for details.
