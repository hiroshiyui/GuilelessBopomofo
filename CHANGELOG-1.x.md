# Changelog — 1.x

## 1.9.51
- Upgraded libchewing to v0.8.2.

## 1.9.50
- Upgraded libchewing to v0.8.1.
- Converted build scripts from Groovy to Kotlin DSL.
- Repackaged libchewing as an AAR module for cleaner builds.

## 1.9.48
- Upgraded libchewing to v0.7.0.

## 1.9.47
- Made input window visible when toggling physical keyboard on/off.

## 1.9.46
- Updated libchewing to v0.6.0.

## 1.9.45
- Fixed layout of key B in QWERTY keyboard on tablet displays.

## 1.9.44
- Added E-Ten 41 Keys keyboard layout.

## 1.9.43
- Fixed DaChen keyboard layout constraints.

## 1.9.42
- Simplified build process by removing external build script.

## 1.9.41
- Fixed wrong landscape QWERTY layout.

## 1.9.40
- Adjusted Enter key width across all keyboard layouts.

## 1.9.39
- Fixed haptic feedback when strength is 0 and same feedback is applied to function buttons.

## 1.9.38
- Updated libchewing (fixed Dvorak-QWERTY mapping).
- Adjusted popup window styles.

## 1.9.37
- Rebuilt all keyboard layouts (DaChen, Hsu, E-Ten 26, Dvorak) using ConstraintLayout for improved consistency.

## 1.9.36
- Adjusted candidate item button style.

## 1.9.35
- Improved vibration performance.

## 1.9.34
- Upgraded target SDK to Android 14 (API 34).

## 1.9.33
- Adjusted pre-edit buffer text colors.

## 1.9.32
- Adjusted dark theme and compact layout colors.

## 1.9.31
- Adopted Material Design 3 (M3) for settings screens.
- Improved dark theme styling for buttons and switches.

## 1.9.30
- Upgraded dependencies.

## 1.9.29
- Fixed settings layout styles.

## 1.9.28
- Fixed input view initialization issue.

## 1.9.27
- Improved view binding initialization reliability.

## 1.9.26
- Fixed edge case when view binding has not been initialized.

## 1.9.25
- Improved physical keyboard detection.

## 1.9.24
- Fixed input window being hidden in fullscreen mode.

## 1.9.23
- Major internal refactoring using EventBus for better event handling.

## 1.9.22
- Significantly improved candidate listing performance by reducing native calls.

## 1.9.21
- Fixed a crash (IndexOutOfBoundsException) in pre-edit buffer display.
- Adjusted button corner radius to 8dp.

## 1.9.20
- Fixed a crash during screen orientation changes.

## 1.9.19
- Added automatic layout switching when physical keyboard is connected or disconnected.

## 1.9.18
- Fixed a crash when changing preferences.

## 1.9.17
- Fixed potential abnormal behavior when long-pressing backspace.

## 1.9.16
- Improved stability by checking view binding state during key events.

## 1.9.15
- Improved long-press key handling.

## 1.9.14
- Fixed initialization timing issue in the input service.

## 1.9.13
- Fixed a crash when rotating the device.

## 1.9.12
- Improved view binding initialization timing.

## 1.9.11
- Migrated to emoji2 library for better emoji support.

## 1.9.10
- Fixed an issue where the IME could intercept key events when hidden.

## 1.9.9
- Fixed an issue when Shift+Space was pressed while the IME window was hidden.

## 1.9.8
- Improved physical keyboard handling.

## 1.9.7
- Fixed repeated key-up events with physical Enter key causing issues in some apps (e.g., Edge Browser).

## 1.9.6
- Reverted EventBus re-enablement due to issues.

## 1.9.5
- Re-enabled EventBus for decoupled event communication.

## 1.9.4
- Fixed cursor handling when the pre-edit buffer is empty but the symbol candidates window is open.

## 1.9.3
- Added support for NumPad keys on physical keyboards.

## 1.9.2
- Reduced UI blocking caused by haptic feedback during fast typing.
- Added support for Android 13 (API 33).

## 1.9.1
- Fixed an internal variable naming issue.

## 1.9.0
- Resolved a dependency conflict with AndroidX Lifecycle.

## 1.8.8
- Further refined Enter key behavior.

## 1.8.7
- Improved Enter key reliability.

## 1.8.6
- Improved Enter key button behavior precision.

## 1.8.5
- Fixed potential memory leaks in activities and candidate views.

## 1.8.4
- Fixed potential memory leak related to view binding.

## 1.8.3
- Improved null safety for better stability.

## 1.8.2
- Upgraded NDK to r23b.

## 1.8.1
- Added support for Android 12L (API 32).
- Improved Shift key stability.

## 1.8.0
- Fixed a crash (IllegalStateException) that could occur during use.
- Fixed letter case display on key captions.

## 1.7.3
- Adjusted keycap popup styles.

## 1.7.2
- Improved keycap popup animations.

## 1.7.1
- Further improved Enter key behavior for various input field types.

## 1.7.0
- Fixed Enter key to correctly perform context-specific actions (search, send, next, etc.).
- Fixed Bopomofo layout assignment for physical keyboards.
- Fixed candidate text color in dark mode.
- Added privacy policy.

## 1.6.0
- Added support for Android 12 (API 31).

## 1.5.1
- Applied user preferences immediately without requiring restart.

## 1.5.0
- Added preliminary dark theme support.

## 1.4.3
- Fixed key buttons being detected as double-taps instead of allowing repeat.

## 1.4.2
- Refactored gesture detection in keys and buffers.
- Moved "ㄦ" key to the bottom row of DaChen layout.

## 1.4.1
- Improved build clean process.

## 1.4.0
- Made haptic feedback settings more flexible with consistent values for all buttons.

## 1.3.6
- Upgraded Kotlin to 1.5.21 and other dependencies.

## 1.3.5
- Fixed input method manifest label.

## 1.3.4
- Improved coroutine usage safety.

## 1.3.3
- Improved layout compatibility for smaller devices (Xperia Z3 Compact).
- Lowered minimum SDK to 23 (Android 6.0).
- Fixed key button popup dismissal on touch release.

## 1.3.2
- Added emoji support via androidx.emoji.
- Reorganized settings into multiple sections.

## 1.3.1
- Deprecated "Physical keyboard keymap" options due to compatibility difficulties.

## 1.3.0
- Fixed several layout switching bugs.

## 1.2.23
- Fixed inconsistent key mapping between on-screen and physical keyboards when using Dvorak layout.

## 1.2.22
- Added Dvorak keymap support.
- Added Ctrl-Z (undo) and Ctrl-R (redo) shortcut keys.
- Added 2 candidate selection key styles for Dvorak layout.

## 1.2.21
- Adjusted compact layout and added logo.

## 1.2.20
- Adjusted visual presentation of number keys.

## 1.2.19
- Adjusted visual presentation of number keys.

## 1.2.18
- Adjusted number keys layout.
- Added support for common Ctrl shortcuts (Ctrl-A, X, C, V).

## 1.2.17
- Enabled QWERTY keycaps for Hsu and E-TEN 26 keys layouts.

## 1.2.16
- Added adjustable key button height.

## 1.2.15
- Added keycap popup animation.

## 1.2.14
- Added popup display for currently touched keycap.

## 1.2.13
- Made backspace repeat faster.
- Added highlight for current IME service on/off status.

## 1.2.12
- Improved custom haptic feedback options.

## 1.2.11
- Fixed long-click backspace behavior.

## 1.2.10
- Fixed crash: `java.lang.NoClassDefFoundError: android.view.textclassifier.TextClassifier`.

## 1.2.9
- Upgraded appcompat and Kotlin.

## 1.2.8
- Refactored backspace key internals.
- Renamed native library from libchewing to libchewing-jni to avoid confusion.

## 1.2.7
- Adjusted backspace key-down behavior.

## 1.2.6
- Adjusted backspace repeat timer rate.

## 1.2.5
- Allowed character keys to repeat when long-pressed.
- Updated libchewing.

## 1.2.4
- Used monospace font for candidate key labels.

## 1.2.3
- Removed unnecessary spaces in monospace display.

## 1.2.2
- Added candidate selection physical key options.

## 1.2.1
- Fixed pass-through for physical keys like volume to avoid disabling them.

## 1.2.0
- Added hardware (physical) keyboard support.
- Added FlexboxLayoutManager for tablet-friendly candidate display.
- Added cursor movement via Left and Right arrow keys.
- Added Shift+Space to toggle main layouts.
- Added handling for physical Enter, Esc, and Grave keys.
- Added configurable candidate selection keys for physical keyboard.

## 1.1.6
- Fixed ChewingEngine.setSelKey().
- Added missing haptic feedbacks.

## 1.1.5
- Added fullscreen mode setting for portrait orientation.
- Made keyboard layout shorter for better accessibility on devices with screens smaller than 5".
- Fixed issue #3.
- Reduced logging for better user privacy.

## 1.1.4
- Increased backspace repeat clicking rate and added fast-repeat prevention.
- Restored pre-edit buffers when changing device orientation.
- Made key buttons larger for improved accessibility.
- Migrated to EventBus for decoupled event communication.

## 1.1.3
- Upgraded Gradle to 6.5.

## 1.1.2
- Added F-Droid metadata.
- Improved accessibility when choosing Bopomofo layouts.

## 1.1.1
- Fixed layout switching logic.

## 1.1.0
- Added QWERTY keyboard layout support.
- Added Shift key with visual state indicators.
- Improved comma input handling by mode.

## 1.0.10
- Added configurable haptic feedback strength.

## 1.0.9
- Fixed keyboard layout assignments.
- Made ChewingEngine a singleton.
- Added Shift+Comma simulation for quick punctuation input.

## 1.0.8
- Adjusted keyboard layouts.

## 1.0.7
- Added configurable phrase choice rearward option.
- Added configurable space-as-selection option.

## 1.0.6
- Fixed a fatal error caused by viewBinding not being available during key events.

## 1.0.5
- Added Space key as candidate selection shortcut for the current cursor character.
- Fixed duplicate commit issue in chewingEngine.commitCheck().

## 1.0.4
- Improved buffer text view with FlexboxLayout.
- Adjusted Bopomofo buffer style to look like a cursor.

## 1.0.3
- Fixed Space key behavior.
- Adjusted buffer text view style.

## 1.0.2
- Made Enter keys wider for better accessibility.
- Added "Back to main layout" button.
- Fixed comma key mapping in DaChen layout.
- Raised minimum SDK to 24 (Android 7.0).
- Improved key image button layout and backward compatibility.

## 1.0.1
- Added configurable fullscreen mode.
- Disabled fullscreen mode automatically in landscape orientation.
- Fixed KeyImageButton theme error.
- Added device orientation detection.

## 1.0.0
- Initial release with DaChen and E-Ten 26 keyboard layouts.
- Support for tablet and landscape layouts.
- Repeatable backspace key.
- Comma key as quick punctuation input shortcut.
- Keyboard layout switching.
- Keyboard settings in main activity.
