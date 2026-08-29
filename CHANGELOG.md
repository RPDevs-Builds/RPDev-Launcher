# Changelog

All notable changes to **RPDev-Launcher** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased] - 2026-08-29

### Added

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
- **Properties & Directory Nesting**:
  - Cleaned deprecated flags in `gradle.properties` (`android.r8.optimizedResourceShrinking`, `android.uniquePackageNames`, `android.generateSyncIssueWhenLibraryConstraintsAreEnabled`).
  - Fixed nested resource folder declaration in `wmshell/build.gradle.kts` by removing redundant parent `"shared"` source directory.
  - Removed unrecognized `dagger.hilt.disableModulesHaveInstallInCheck` argument from `defaultConfig`.

### Fixed
- **Cover Mode Rendering & Swipe-Up Physics**:
  - Fixed blank folder preview rendering in Cover Mode when custom icon packs or dynamic icons are applied.
  - Fixed NaN velocity / spring animation division-by-zero crash during rapid swipe-up folder expansion (`FolderAnimationSpringBuilderManager`, `FolderAnimationManager`).
- **Resource Formatting / AAPT2 Localization**:
  - Fixed non-positional format string syntax in Croatian translation (`Omega/res/values-hr/strings.xml`) for `battery_charging_percentage_charging_time` and `n_percent` by escaping literal percent signs as `%%`.
