# Changelog — 2.x

## 2.3.0
- Added option to choose a conversion engine.
- Added new Chewing API wrappers: configGetStr, configGetInt, configSetInt, configHasOption, version, and ack.
- Added support for Android 16 (API 36).
- Added BopomofoKeyboards enum for keyboard layout management.
- Merged libchewing JNI back into the main project.
- Fixed Shift state not resetting when switching main layouts.
- Fixed wrong return type for getShapeMode JNI function.
- Enabled native symbol generation in release builds.

## 2.2.16
- Upgraded libchewing module.

## 2.2.15
- Added ability to toggle half-width and full-width (shape) mode.
- Fixed Hsu layout Q key behavior to only open candidate window when in Bopomofo layout.

## 2.2.14
- Fixed Back key to work correctly without blocking other back operations.

## 2.2.13
- Fixed Q key remapping for KB_DVORAK_HSU layout.

## 2.2.12
- Added Q key to open candidate window when using Hsu layout.
- Consolidated candidate window opening code.

## 2.2.11
- Fixed key remapping for Dvorak layout in candidates window.
- Fixed candidates window opening keys behavior.
- Fixed pass-through for physical keys like volume and back keys.

## 2.2.10
- Fixed behavior of candidates window opening keys (Up, Down, and Space).

## 2.2.9
- Adjusted settings layouts.

## 2.2.8
- Adjusted settings layouts.

## 2.2.7
- Added Japanese (ja-JP) locale.
- Added support for per-app language preferences.

## 2.2.6
- Added Esc key to hide IME view in normal layouts.

## 2.2.5
- Added error-prevention mechanism for enhanced physical keyboard compatibility mode.
- Added double-tap on buffer area to toggle between compact and full IME layout.

## 2.2.4
- Refactored key event handling for both physical and virtual keyboards.
- Reverted to original physical keyboard detection logic.

## 2.2.3-p6
- Removed the layout switching logic in onKeyDown().

## 2.2.3-p5
- Changed physical keyboard detection to be a partial condition.

## 2.2.3-p4
- Improved physical keyboard detection and keyboard layout switching.

## 2.2.3-p3
- Added more diagnostic information in engineering mode.

## 2.2.3-p2
- Adjusted physical keyboard detection behavior.

## 2.2.3-p1
- Attempted fix for issue #51 related to shared preference changes.

## 2.2.3
- Added hardware keyboard D-pad Up key behavior.
- Corrected several option behaviors.
- Fixed wrong comma mapping in KB_DVORAK_HSU.

## 2.2.2
- Updated libchewing module to support 16 KB page sizes.

## 2.2.1
- Changed Latin script font to PT Serif.
- Improved PopupWindow dismissal behavior.

## 2.2.0
- Redesigned keycap images with updated SVG artwork.
- Improved events handling and code readability.

## 2.1.4
- Upgraded libchewing module to 0.9.1.2.
- Fixed system window fitting in AndroidManifest.

## 2.1.3
- Fixed layout issues on Android 15 by specifying `android:fitsSystemWindows`.

## 2.1.2
- Migrated to Gradle version catalog (`libs.versions.toml`) for dependency management.
- Upgraded Kotlin to 2.0.21.

## 2.1.1
- Upgraded target SDK to 35 (Android 15).
- Improved null safety for applicationInfo and versionName.
- Consolidated Chewing data file list into a single source.

## 2.1.0
- Upgraded libchewing to v0.9.1.
- Upgraded Android Studio to Ladybug and Gradle.

## 2.0.3
- Upgraded libchewing to v0.8.5.

## 2.0.2
- Added automatic redirect to system input method settings if Guileless Bopomofo hasn't been enabled.

## 2.0.1
- Fixed issue #39.
- Improved code readability with clearer method and class naming.

## 2.0.0
- Upgraded libchewing to v0.8.4 (Rust-based).
- Added Right key to toggle to next candidate page.
- Added Colemak ANSI and Colemak Ortholinear layout test cases.
- No longer requires NDK to build native shared library.
- Fixed KB_DVORAK_HSU keyboard layout.
