# PadKeys

PadKeys turns an Android phone into a Bluetooth HID keyboard and mouse for any host (PC, Mac, Linux, iPad, Android TV, smart TV). No companion app is required on the host.

## Features

- Touchpad mode: move, left/right click, scroll
- Full QWERTY keyboard (F1-F12, numbers, symbols, modifiers, arrows) in landscape
- Bluetooth HID (Classic) pairing; auto-reconnect to last host
- Material 3 UI with dynamic color (Material You)
- Portrait by default, landscape only in keyboard mode
- No internet permission; local Bluetooth only

## Requirements

- Android 9+ (API 28)
- Bluetooth classic
- Host supporting Bluetooth HID input

## Build

```bash
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

Release:
```bash
./gradlew assembleRelease
```

## Usage

1. Open PadKeys, make the phone discoverable (Connect tab).
2. On the host, scan Bluetooth and pair with "PadKeys".
3. In PadKeys, tap Reconnect.
4. Use Touchpad or Keyboard tabs.

## Compatibility

- Works best with Windows 10/11, macOS, Linux, iPadOS, Android TV, smart TVs.
- Android phones as host are usually not supported.
- Some OEMs disable Bluetooth HID device profile.

## License

MIT. Based on Linkpad by Devdas Kumar (https://github.com/Devdas-gupta/linkpad). Shoutout to the original project.
