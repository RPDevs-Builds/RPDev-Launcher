# RPDev Launcher GEMINI Guide

## Project Overview
RPDev Launcher is a modern AOSP Launcher3-based custom launcher targeting Android 16. It is designed to be feature-rich and optimized for the RPDevs ecosystem.

## Architecture and Module Structure
- `Omega/` - The core application codebase.
- `wmshell/` - WindowManager Shell integrations.
- `compatLib/` - Compatibility library for various Android versions.
- `libs_systemui/` - SystemUI related libraries.
- `flags/` - Feature flags for different implementations.
- `modules/widgetpicker/` - Widget selection modules.
- `modules/concurrent/` - Concurrency enhancements and extensions.

## Build Commands
- Run debug build: `./gradlew assembleAospOmegaDebug`
- Run unit tests: `./gradlew testAospOmegaDebugUnitTest`

## Code Standards
- **Application Identifier / Package ID:** `iamrp.dev.launcher`
- **Modern Kotlin & Java:** Kotlin 2.4.x, Java 21.
- **SDK Target:** Target API 34-37.
- **Build System:** AGP 8/9/10 using `androidComponents` variant API.
- **Android Manifest:** Ensure explicit BroadcastReceiver export flags.
- **Code Quality:** Enforce type-safe system services and adhere to strict null-safety standards.
