# Changelog — 3.x

## 3.7.4
- Fixed version information not displaying correctly.

## 3.7.3
- Added unit tests for Kotlin logic.
- Improved error logging and lazy initialization of candidates layout.
- Improved thread safety, null safety, and resource management.
- Performed security audit on JNI code.

## 3.7.2
- Enabled sorting candidates by frequency for better suggestions.
- Upgraded libchewing to 0.11.0.

## 3.7.1
- Added new Bopomofo keyboard layout: DaChen 26 keys (DaChen CP26).

## 3.7.0
- Further adjusted layouts of Bopomofo phonetic symbols.

## 3.6.10
- Adjusted layouts of Bopomofo phonetic symbols.

## 3.6.9
- Added support for Unihertz Titan 2 physical keyboard.

## 3.6.8
- Implemented all physical keyboard layouts available in libchewing.
- Improved automatic detection of the preferred physical keyboard layout.
- Deprecated Dvorak and Dvorak-Hsu keyboard layouts.

## 3.6.7
- Fixed incorrect behavior when switching between input modes.

## 3.6.6
- Fixed Bopomofo buffer not being cleared when switching to alphanumeric layout.

## 3.6.5
- Upgraded libchewing to v0.10.3.

## 3.6.4
- Fixed behavior of Shift+Comma input handling.
- Added support for easy symbol input detection.

## 3.6.3
- Enabled resource shrinking for smaller release builds.
- Removed unnecessary EmojiCompat dependency.
- Improved shared preferences key management.

## 3.6.2
- Removed the "Use physical keyboard" option (physical keyboard is now handled automatically).

## 3.6.1
- Reverted several physical keyboard handling changes for stability.
- Upgraded libchewing to latest master.

## 3.6.0
- Updated libchewing.

## 3.5.7
- Updated libchewing dictionary data.

## 3.5.6
- Upgraded libchewing to v0.10.2.

## 3.5.5
- Updated libchewing and adjusted the dictionary data build process.

## 3.5.4
- Downgraded Android Gradle Plugin to 8.11.1 for F-Droid build server compatibility.

## 3.5.3
- Changed physical keyboard IME switch hotkey from long-pressing Left Alt to Alt+I.

## 3.5.2
- Added description text for width mode toggling.
- Added display of the current width mode (half-width/full-width).

## 3.5.1
- Updated libchewing to v0.10.1.

## 3.5.0
- Upgraded libchewing to v0.10.0.
- Added Bopomofo string display support.

## 3.4.5
- Added Ctrl+Left and Ctrl+Right as Home and End key shortcuts.
- Fixed incorrect behavior of the candidate window management.
- Improved cursor movement behavior.

## 3.4.2
- Fixed buffer updates not triggering when switching to main layout.
- Fixed candidate selection flow.

## 3.4.1
- Added a new option to show or hide the IME switching key button.

## 3.4.0
- Added automatic detection of the current input type to switch to alphanumeric mode when appropriate.
- Added digits input test interface in engineering mode.

## 3.3.5
- Applied Edge-to-Edge layouts for a modern full-screen experience.
- Decorated bottom navigation bar with a gradient shape.
- Added Android version detection logic.
- Fixed theming inconsistencies.
- Applied GPLv3 license.

## 3.3.1
- Fixed potential crashes (IllegalStateException and NullPointerException) when detecting physical keyboard presence.
- Changed the default value for physical keyboard support to enabled.

## 3.3.0
- Fixed cursor position tracking to update explicitly.

## 3.2.3
- Improved status bar accessibility by enabling light status bar icons.

## 3.2.2
- Fixed the IME view to properly hide when the Back key is pressed.

## 3.2.1
- Adjusted the Back key behavior to avoid confusion with the Backspace key.

## 3.2.0
- Fixed the pre-edit buffer text view not wrapping long text correctly.

## 3.1.0
- Migrated to Material Design buttons with ripple effect for key press feedback.
- Replaced key button popups with ripple effect indicators.
- Added elevation to keyboard elements for improved visual depth.
- Introduced new themes including a Wine color style for dark mode.

## 3.0.2
- Added handling for Caps Lock key events.

## 3.0.1
- Fixed an issue where the IME incorrectly handled key events when the input view was not shown.
- Changed layout-specific options to only display when the corresponding layout is selected.

## 3.0.0
- Added official support for Android 16.
- Fixed conversion engine mode behavior.
