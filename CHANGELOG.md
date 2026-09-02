# Changelog

All notable changes to **RPDev-Launcher** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.1] - 2026-09-02 (Nested Folders Engine, Android 14+ Baseline & REST/Webhook Shortcuts)

### Added
- **📁 Full Nested Folders Hierarchy Engine**:
  - Implemented recursive folder-in-folder nesting on Home Screen Workspace and App Drawer.
  - Added real-time circular hierarchy prevention with bidirectional cycle detection (`isChildOf`) across nested content IDs and layout containers.
  - Seamless nested navigation: opening a nested folder smoothly animates child overlay while hiding parent container; back button press seamlessly ascends to the parent folder before exiting to workspace.
  - Container-aware dynamic icon sizing and drawable padding for nested folder views.
  - Hardened layout parameters and animation transitions in `BaseDragLayer`, `FolderAnimationManager`, and `FolderIcon` preventing null-pointer exceptions.
  - Full SQLite database persistence (`LauncherSettings.Favorites`) preserving nested tree structures across app restarts and device reboots.
- **⚡ Custom Homescreen REST GET & Webhook Push Shortcuts**:
  - Implemented `WebhookShortcutCreatorActivity` with Material 3 configuration UI supporting native `Intent.ACTION_CREATE_SHORTCUT` / `ShortcutManagerCompat`.
  - Configurable Shortcut Name, Target Endpoint URL, HTTP Method (`GET`, `POST`, `PUT`, `DELETE`), Headers (JSON), and Payload/Body.
  - Implemented `WebhookActionActivity` background execution engine executing requests on `Dispatchers.IO` via OkHttp with instant toast notifications.
- **🌟 Modular Companion Add-on Discovery**:
  - Added `FeedCompanionBanner` in Search & Feed preferences providing real-time detection, one-tap installation, and direct launch for RPDev Feed.
  - Maintains lean Launcher core footprint (~17 MB) while enabling modular powerhouse expansion.
- **🚀 Automated CI/CD & Multi-Asset Release Pipeline**:
  - Upgraded GitHub Actions workflow (`.github/workflows/build.yml`) to `actions/upload-artifact@v7`, `actions/checkout@v7`, and `actions/setup-java@v6` with automated release packaging and artifact publishing on version tags.

### Changed & Modernized
- **🧹 Pre-Android 14 Legacy Stripping & 170+ MB Artifact Purge**:
  - Established clean Android 14+ (API 34+) minimum platform baseline.
  - Purged obsolete pre-Android 14 compatibility submodules (`compatLibVQ`, `compatLibVR`, `compatLibVS`, `compatLibVT`).
  - Deleted ~170+ MB of obsolete prebuilt framework jars (`framework-10.jar`, `framework-11.jar`, `framework-12.jar`, `framework-12l.jar`, `framework-13.jar`).
  - Decoupled and modernized `compatLibVU` (Android 14) and `compatLibVV` (Android 15) to implement `QuickstepCompatFactory`, `ActivityManagerCompat`, and `ActivityOptionsCompat` directly.
  - Consolidated legacy runtime SDK level conditionals (`ATLEAST_Q`, `ATLEAST_R`, `ATLEAST_S`, `ATLEAST_T`, `ATLEAST_U`) into compile-time `true` constants.
  - Purged legacy 2017 Android Support Test dependencies in favor of modern `androidx.test`.
  - Removed deprecated CI files (`.gitlab-ci.yml`, `.forgejo/`) while strictly preserving all open-source licensing headers and attribution notices.

## [1.0.0] - 2026-09-02 (Initial Production Release)

### Added
- **Dual Launcher App Icon Support**:
  - Fully replaced the legacy Neo logo with the new **Dark Neon (Default)** master icon (`assets/icon/icon.png`) and generated `ic_launcher` mipmaps across all densities.
  - Added secondary **High Contrast (Light)** icon (`assets/icon/lighticon.png`) with full mipmap generation.
  - Added user preference `profileAppIconStyle` in Theme / Profile settings to dynamically switch between dark neon and high-contrast light launcher app icons.
- **RPDev Launcher Rebranding & Naming**:
  - Rebranded project identifier to `iamrp.dev.launcher` (`iamrp.dev.launcher.alpha` on debug builds).
  - Renamed core Launcher activity aliases to `iamrp.dev.launcher` and `iamrp.dev.launcher.LauncherLight` to ensure complete alignment with project branding.
  - Rebranded application display name (`derived_app_name`, `app_name`) and user-facing strings across base strings and 19 locale translations.
  - Dynamic artifact output naming: `RPDev_Launcher_v1.0.0-alpha.apk` (debug/main) and `RPDev_Launcher_v1.0.0.apk` (release).
  - Rebranded repository documentation (`README.md`), GitHub issue templates, and created comprehensive architectural documentation (`GEMINI.md`).
- **Continuous Integration & Automated Releases**:
  - Added GitHub Actions workflow (`.github/workflows/build.yml`) configured for JDK 21, Gradle 9.4 caching, automated APK artifact generation, unit test execution, and GitHub Releases on version tags.
- **Android 12+ Backup & Extraction Configuration**:
  - Added modern `res/xml/data_extraction_rules.xml` and configured `android:dataExtractionRules` in root manifests for cloud backup and device-to-device transfers.

### Changed
- **Android 14+ / 16 (API 34–37) Security & Platform Modernization**:
  - Added explicit `ContextCompat.RECEIVER_NOT_EXPORTED` flags to all dynamic `BroadcastReceiver` registrations (`LauncherClient.java`, `LoaderTask.java`, `DefaultTransitionHandler.java`) preventing Android 14+ runtime crashes.
  - Modernized `new Handler()` instantiation across 8 core classes to require explicit Looper bindings (`Looper.getMainLooper()`).
  - Modernized Accessibility framework instantiations (`new AccessibilityEvent()`, `new AccessibilityNodeInfo()`) avoiding deprecated object pool APIs.
  - Replaced single-parameter `resources.getColor(...)` calls with theme-aware `context.getColor(...)` across 10 Java files.
  - Migrated `Intent.getParcelableExtra(...)` and `Bundle.getParcelable(...)` to type-safe `IntentCompat` and `BundleCompat` APIs.
  - Converted string-based `getSystemService` to native `context.getSystemService(FooManager::class.java)` across 12 Kotlin providers and gestures.
  - Replaced high-risk non-null assertions (`!!`) with safe calls and null guards across 8 critical Kotlin classes.
- **AGP 10 & Gradle 9.4 Build System Upgrade**:
  - Migrated variant configuration from legacy `applicationVariants.all` to AGP 10 `androidComponents.onVariants`.
  - Upgraded build scripts to use `ApplicationExtension` and `LibraryExtension` with `android.newDsl=true`.
  - Removed `gradle.projectsEvaluated` anti-patterns across 16+ submodules (`modules/concurrent`, `wmshell`, `flags`, `compatLib/*`, `libs_systemui/*`) for full Gradle Configuration Cache support.
  - Fixed inverted Hilt version catalog artifacts (`hilt-android` / `hilt-android-compiler`).
  - Stripped obsolete `kotlin-stdlib-jdk7` and `lifecycle-extensions` dependencies.
  - Harmonized `compileSdk` and `targetSdk` across all targets to API 37 (Android 16).

---

## [0.9.0-dev] - 2026-08-29

#### 1. Folder Enhancement & Customization Suite
- **Advanced Visual Styling**:
  - Custom background color picker with opacity/alpha slider for open and closed folder states.
  - Configurable border stroke width, outline radius, and border tint color.
  - Adaptive corner radius slider with support for squircle, pill, and circular folder envelopes.
- **Multi-Mode Folder Preview Layouts**:
  - **Grid 2x2 (4-Icon Standard)**: Clean modern 4-app mini preview with proportional scaling.
  - **Grid 3x3 (9-Icon Compact)**: High-density 9-app mini preview for power users with large folders.
  - **1-Item Big Icon (Cover Mode)**: Displays a full-sized single app icon acting as the folder's facade.
  - **Radial / Arc Preview**: Circular arc distribution of folder elements around the center.
  - **Stacked / Overlap Preview**: Layered card-style preview with depth shadows.
- **Automated & Manual Folder Content Sorting**:
  - Integrated `FolderSortUtil` supporting instant sorting inside folder sheets:
    - **Alphabetical (A–Z / Z–A)**: Sort by localized application display title.
    - **Installation Time (Newest / Oldest)**: Sort by package `firstInstallTime`.
    - **Usage Frequency (Most / Least Used)**: Sort by launch frequency counter.
    - **Custom Drag & Drop Reordering**: Manual layout arrangement persisted across reboots.

#### 2. Cover Mode & Gesture Engine
- **One-Tap Quick Launch**: Tapping a folder configured in Cover Mode instantly executes the primary cover application.
- **Swipe-Up Reveal**: Swiping upwards on a Cover Mode folder cleanly expands the complete folder overlay without launching the cover app.
- **Folder-Scoped Cover Selection**: Restricted the "Select Cover App" picker dialog to strictly display apps that belong to the targeted folder instead of enumerating the full device application drawer (`SelectCoverPage` / `FolderItemProvider`).
- **Dynamic Icon Binding**: Automatic fallback to the first folder item if the assigned cover app is uninstalled or moved.

#### 3. Bidirectional Drawer-to-Workspace Synchronization
- **Real-Time 1-to-1 Folder Mirroring**:
  - Enabled bidirectional synchronization between App Drawer folders (`DrawerFolders` / `DrawerFolderItem`) and Home Screen Workspace folders (`FolderInfo` / `WorkspaceItemProcessor`).
  - Item addition, removal, and reordering within a synced folder in either the drawer or the desktop immediately mirrors to the paired folder instance.
  - Synchronized folder title renames, styling attributes, and sorting order.

#### 4. Kotlin 2.5 Future-Proofing & Compiler Compliance
- **Data Class Copy Visibility (`KT-11914`)**:
  - Configured `-Xconsistent-data-class-copy-visibility` across all subprojects via root `build.gradle.kts`.
  - Added `@ConsistentCopyVisibility` annotations where applicable to enforce strict data class copy encapsulation.

### Changed

#### Android Platform SDK API Modernization (API 33–37)
- **Type-Safe Parcelable & Bundle Deserialization**:
  - Migrated legacy `Intent.getParcelableExtra(...)` and `Bundle.getParcelable(...)` calls to AndroidX `IntentCompat.getParcelableExtra(...)`, `BundleCompat.getParcelable(...)`, and `BundleCompat.getParcelableArrayList(...)` across:
    - `NeoLauncher.kt`: Activity result intent sender request dispatching.
    - `EditIconPage.kt`: External icon pack shortcut resource picker.
    - `LauncherGestureHandler.kt`: Gesture `UserHandle` shortcut intent deserialization.
    - `MediaListener.kt`: Notification `MediaSession.Token` extraction.
    - `UserCache.kt`: System user profile change broadcast receiver.
    - `WidgetSizeHandler.kt`: AppWidget `OPTION_APPWIDGET_SIZES` list extraction.
    - `DrawerTabs.kt` and `ParcelableFlyoutMessage.kt`: `ParcelCompat.readParcelable(...)` migrations.
- **Modern Location Providers (API 31+)**:
  - Modernized `OWMWeatherProvider.kt` to use `LocationManager.FUSED_PROVIDER` on Android 12+ (API 31+) with resilient fallback handling.
- **Modern Window Insets & Navigation**:
  - Refactored `SleepTimeoutActivity.kt` to use `WindowInsetsControllerCompat` and `WindowInsetsCompat.Type.navigationBars()` instead of deprecated `View.SYSTEM_UI_FLAG_HIDE_NAVIGATION`.
- **Colors, Insets & Services**:
  - Migrated `PreloadIconDelegate.kt` to `context.getColor(...)` instead of deprecated `resources.getColor(...)`.
  - Converted `MagnetizedObject.kt` to `context.getSystemService(Vibrator::class.java)`.
  - Updated `DbEntry.kt` to use `Intent.toUri(0)` instead of `Intent.toURI()`.
  - Replaced deprecated `setBackgroundDrawable(...)` in `DismissView.kt` with `background = gradientDrawable`.

#### Type Safety & Architecture Hardening
- **Kotlin Primitive Types**: Refactored `LauncherPrefs.kt` to use `Boolean::class.javaObjectType`, `Int::class.javaObjectType`, `Float::class.javaObjectType`, and `Long::class.javaObjectType` instead of deprecated `java.lang.*` class literals.
- **Elimination of Unsafe Casts**:
  - Replaced invalid `ActivityInfo` cast in `ShortcutInfoProvider.kt` with `LauncherActivityInfo.getIcon(0)`.
  - Replaced unsafe unchecked collection cast in `AppCategoriesPage.kt` with `groups.filterIsInstance<DrawerTabs.Tab>()`.
  - Converted `AppPairInfo.kt` copy constructor from `clone() as ArrayList` to idiomatic `ArrayList(appPairInfo.contents)`.
  - Cleaned redundant casts and replaced unsafe group-by casts in `FirstScreenBroadcastHelper.kt`.
  - Removed unnecessary safe calls and non-null assertions in `WidgetsInteractor.kt`, `PopupContainer.kt`, and `HomeScreenFilesChangedTask.kt`.
  - Replaced `Object()` instantiation with idiomatic `Any()` for `STABLE_ID` in `OseWidgetView.kt`.
  - Aligned overriding method parameter names in `AppInfoCachingLogic.kt`, `CachedObjectCachingLogic.kt`, `LauncherActivityCachingLogic.kt`, `CacheableShortcutInfo.kt`, `StatsLogCompatManager.kt`, and `UtilitiesKt.kt` to match supertype declarations.
  - Added explicit `@Deprecated("Deprecated in Java")` annotations to override methods for `getOpacity()` and `onLowMemory()` in `BubblePopupDrawable.kt`, `ShaderBlurDrawable.kt`, `WallpaperThemeManager.kt`, and `DoubleShadowIconDrawable.kt`.
- **Material 3 Compose UI**:
  - Upgraded deprecated `TabRow` to `PrimaryTabRow` in `TabUtils.kt`.

#### Build System & Gradle Toolchain
- **Namespace Cleanups (AGP 10+ Ready)**:
  - Removed obsolete `package` attributes from `<manifest>` tags across `iconloaderlib`, `msdllib`, `widgetpicker`, `wmshell`, and `AndroidManifest-common.xml`.
  - Removed unused `<activity ... tools:node="remove" />` tag in `Omega/AndroidManifest.xml`.
- **Properties, Caching & Gradle 10 Readiness**:
  - Configured `android.dependency.useConstraints=false` to eliminate AGP 9/10 dependency constraint configuration warnings and speed up project evaluation.
  - Enabled `org.gradle.parallel=true` and `org.gradle.caching=true` for faster multi-module incremental compilation.
  - Modernized `hidden-api/build.gradle` from space syntax to assignment syntax (`=`) for Gradle 10 Groovy DSL compliance.
  - Cleaned deprecated flags in `gradle.properties` (`android.r8.optimizedResourceShrinking`, `android.uniquePackageNames`, `android.generateSyncIssueWhenLibraryConstraintsAreEnabled`).
  - Fixed nested resource folder declaration in `wmshell/build.gradle.kts` by removing redundant parent `"shared"` source directory.
  - Added `-Xlint:-dep-ann` Java compiler option to suppress annotations on auto-generated Protobuf models.

### Fixed
- **Cover Mode Rendering & Swipe-Up Physics**:
  - Fixed blank folder preview rendering in Cover Mode when custom icon packs or dynamic icons are applied.
  - Fixed NaN velocity / spring animation division-by-zero crash during rapid swipe-up folder expansion (`FolderAnimationSpringBuilderManager`, `FolderAnimationManager`).
- **Resource Formatting / AAPT2 Localization**:
  - Fixed non-positional format string syntax in Croatian translation (`Omega/res/values-hr/strings.xml`) for `battery_charging_percentage_charging_time` and `n_percent` by escaping literal percent signs as `%%`.
