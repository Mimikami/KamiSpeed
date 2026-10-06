<p align="center">
  <a href="README.md">English</a> | <a href="README.zh-CN.md">简体中文</a>
</p>

# KamiSpeed - 耳神速

Open-source Android game speed modifier (1x ~ 20x), no root required.

## Features

- **No root**: built on a virtualization container — games run inside KamiSpeed's own process, system untouched
- **Real-time 1x ~ 20x speed control**: GOT/PLT hooking of libc time functions; drag the slider and it takes effect instantly, with a strictly continuous (never jumping backwards) time axis
- **Floating panel / half-circle ball**: draggable panel; ✕ collapses it into an edge-snapped ball (auto-fitting text size); tap the ball to expand
- **Channel SDK compatible**: guest services / self-broadcasts are hosted in-process, so channel logins (U8 / TFY etc.) complete normally
- **Quick launch**: pin your games and start them at the current speed with one tap

## Download

Grab the APK from [Releases](https://github.com/Mimikami/KamiSpeed/releases) and install it directly.

## Usage

1. Open KamiSpeed, toggle the switch and drag the slider to set the speed (1.0x ~ 20.0x)
2. Tap "＋ Add" to pin games to the home panel, then tap to launch
3. The floating panel appears in the game: slider / switch / ± adjust the speed live; ✕ collapses to a ball, tap the ball to expand
4. If a game gets stuck on a channel SDK permission gate, long-press the shortcut and pick the real game Activity

## Build

Requirements: JDK 17, Android SDK (platform 34), NDK, Gradle 8.7.

```bash
# Prebuild the native library (or skip it and use the committed jniLibs)
./tools/build-native.ps1

# Assemble the debug APK
gradle -PnativePrebuilt=true :app:assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`.

## How It Works

- **Container**: only the ApplicationInfo (uid / dataDir) is patched; the framework natively builds the LoadedApk / ClassLoader / resources. Activities launch through host stubs and then get their real intent / activityInfo restored
- **Speed hack**: hooks the GOT slots of loaded modules for libc time functions; virtual time is `virtual = anchor + (real - anchor) * scale`; sleep-family calls are shrunk by the inverse of the scale

## Known Limitations

- Multi-process components (e.g. independent-process download services) are not supported yet
- While accelerating, the game's local clock runs ahead of real time (resets after restart); gameplay is server-authoritative anyway

## License

[GPL-3.0](LICENSE) — you must retain the copyright and author attribution when using, modifying or distributing the source; derivative works must be released under the same license. Copyright © 2026 Mimikami.
