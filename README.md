<h1 align="center">
  <img src="assets/icon/icon.png" alt="RPDev Launcher Dark Icon" width="128" height="128"/>
  <br>
  RPDev Launcher
</h1>

<p align="center">
  <strong>The modern, performance-driven AOSP &amp; Android 16-based launcher tailored for the RPDevs ecosystem.</strong>
</p>

<p align="center">
  <a href="https://github.com/RPDevs-Builds/RPDev-Launcher/releases/latest">
    <img src="https://img.shields.io/github/v/release/RPDevs-Builds/RPDev-Launcher?style=flat&labelColor=1a1a2e&color=4e54c8" alt="Latest stable release version"/>
  </a>
  <a href="https://github.com/RPDevs-Builds/RPDev-Launcher/actions/workflows/build.yml">
    <img src="https://github.com/RPDevs-Builds/RPDev-Launcher/actions/workflows/build.yml/badge.svg" alt="Build Status" />
  </a>
  <a href="https://github.com/RPDevs-Builds/RPDev-Launcher/releases/">
    <img alt="GitHub downloads" src="https://img.shields.io/github/downloads/RPDevs-Builds/RPDev-Launcher/total.svg?style=flat&labelColor=1a1a2e&color=4e54c8"/>
  </a>
  <a href="https://github.com/RPDevs-Builds/RPDev-Launcher/stargazers">
    <img alt="GitHub repo stars" src="https://img.shields.io/github/stars/RPDevs-Builds/RPDev-Launcher?style=flat&labelColor=1a1a2e&color=4e54c8"/>
  </a>
  <a href="/COPYING">
    <img src="https://img.shields.io/github/license/RPDevs-Builds/RPDev-Launcher?style=flat&labelColor=1a1a2e&color=4e54c8" alt="Project License" />
  </a>
</p>

---

## 📖 Overview

**RPDev Launcher** is an open-source, highly customizable Android launcher based on Neo Launcher, AOSP Launcher3, and WindowManager Shell. Modernized for Android 14+ (Min SDK 34) and targeting Android 16 (API 37) with Gradle 9+ and AGP 10-ready architecture.

---

## ✨ Key Features

### 🗂️ Advanced Folder Suite & Cover Mode
- **Multi-Mode Previews**: 2×2 (4-Icon Standard), 3×3 (9-Icon Compact), 1-Item Big Icon (Cover Mode), Radial Arc, and Stacked Layer previews.
- **Cover Mode Gesture Engine**: One-tap quick launch of primary cover application + swipe-up gesture to reveal full folder contents without launching.
- **Automated Content Sorting**: Sort folder items alphabetically (A–Z / Z–A), by installation date (Newest / Oldest), launch frequency, or custom drag-and-drop.
- **Bidirectional Drawer-to-Desktop Sync**: 1-to-1 live synchronization between App Drawer folders and Home Screen workspace folders.

### 🎨 Theming & Dynamic App Icon Styles
- **Dual Launcher Icon Options**: Choose between **Dark Neon (Default)** and **High Contrast (Light)** launcher icons directly in Theme settings via dynamic activity aliases.
- **Material You Dynamic Theming**: Adaptive color extraction matching system wallpapers.
- **Themed Icons & Custom Icon Packs**: Complete icon pack masking, adaptify engine, and per-app custom icon assignment.
- **Adaptive Blur & Corners**: Configurable background blur radius and custom window corner radius.

### ⚡ Custom REST GET & Webhook Push Shortcuts
- **One-Tap Desktop Actions**: Create and pin native desktop shortcuts to trigger custom REST GET, POST, PUT, or DELETE HTTP requests in the background.
- **Home Assistant & IoT Automation**: Configure target URLs, JSON headers (e.g. `Authorization: Bearer`), and payloads to trigger smart home webhooks and server commands without opening a browser.
- **Immediate Toast Feedback**: Background requests run on `Dispatchers.IO` via OkHttp with instant HTTP response status toasts.

### 🌟 Modular Companion Add-on Discovery
- **Decoupled Powerhouse Architecture**: Keeps the Launcher core fast, lean (~17 MB), and memory-efficient while enabling seamless integration with the **RPDev Feed** companion app for Privacy Weather (Open-Meteo), Calendar Agenda, Hardware Telemetry, and RSS/JSON feeds.

### ⚡ Performance & Android 16 (API 37) Modernization
- **Security-First Architecture**: Explicit `RECEIVER_NOT_EXPORTED` flags across all dynamic BroadcastReceivers preventing Android 14+ crashes.
- **Modern Looper & Concurrency**: Explicit `Looper.getMainLooper()` dispatching eliminating deprecated Handler constructors.
- **Safe Type Deserialization**: Migrated to AndroidX `IntentCompat` and `BundleCompat` for type-safe parcelable handling.
- **Android 12+ Cloud Backup**: Full `data_extraction_rules.xml` support for encrypted device-to-device and cloud backups.

---

## 📱 Screenshots

| <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/desktop_icons.png" alt="Desktop" width="300"/> | <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/desktop_dash.png" alt="Dash" width="300"/> | <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/settings.png" alt="Settings" width="300"/> |
|:---:|:---:|:---:|
| **Desktop Workspace** | **The Dash** | **Preferences** |

| <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/profile.png" alt="Theme" width="300"/> | <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/gestures.png" alt="Gestures" width="300"/> | <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/dash.png" alt="Control Center" width="300"/> |
|:---:|:---:|:---:|
| **Theme &amp; Profile** | **Gestures** | **Quick Controls** |

---

## 🛠️ Building From Source

### Prerequisites
- **JDK 21** (`export JAVA_HOME=/path/to/jdk-21`)
- **Android SDK** (API Level 37 / Build-Tools 36.0.0+)

### Commands
```bash
# Clone the repository
git clone https://github.com/RPDevs-Builds/RPDev-Launcher.git
cd RPDev-Launcher

# Build Debug APK (outputs to build/outputs/apk/aospOmega/debug/)
./gradlew assembleAospOmegaDebug

# Build R8-Optimized Release APK (outputs to build/outputs/apk/aospOmega/release/)
./gradlew assembleAospOmegaRelease
```

---

## 🤝 Contributions

Contributions, bug reports, and feature requests are welcome! Feel free to explore our [open issues](https://github.com/RPDevs-Builds/RPDev-Launcher/issues) or submit pull requests.

---

## 📜 License & Acknowledgements

**RPDev Launcher** modifications and features are licensed under the [GPLv3+](https://github.com/RPDevs-Builds/RPDev-Launcher/blob/main/COPYING).
Upstream AOSP Launcher3 code is licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).

This project is originally based on and heavily indebted to the work of [Neo-Launcher](https://github.com/NeoApplications/Neo-Launcher) by the NeoApplications team. Special thanks to key contributors [Saul Henriquez](https://github.com/saulhdev) and [Antonios Hazim](https://github.com/machiav3lli) for their foundational work on the launcher.

Copyright © 2026 [RPDevs-Builds](https://github.com/RPDevs-Builds)
