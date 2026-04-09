# Changelog — 0.x (Pre-release)

This covers the early development history before the first official release (1.0.0). These 192 commits laid the foundation for Guileless Bopomofo.

## Project Bootstrap
- Initial commit and project setup.
- Integrated libchewing as a Git submodule with CMake build system.
- Implemented JNI bridge for `chewing_new2()`, `chewing_delete()`, and core libchewing C API functions.
- Set up chewing data files (dictionary, phrase tables) as Android assets.
- Configured minimum SDK to Android 5.0 (API 21), target SDK to Android 11 (API 30).

## Input Method Foundation
- Declared `GuilelessBopomofoService` as an Android input method service.
- Implemented basic Bopomofo input flow: key input -> libchewing processing -> candidate selection -> text commit.
- Created `ChewingEngine` class to encapsulate libchewing operations.
- Implemented pre-edit buffer and Bopomofo buffer display.
- Added phrase selection direction handling.
- Added candidate list navigation (`candListNext`, `candListPrev`, `candClose`).

## Keyboard UI
- Built the software keyboard layout using `RelativeLayout` (after ConstraintLayout proved difficult).
- Created `KeyImageButton` with keycode and keytype support.
- Added Bopomofo symbol icons for keycaps.
- Implemented keycap haptic feedback.
- Added backspace key.
- Added Enter key with colored background.
- Supported landscape orientation layout.

## Candidate Selection
- Implemented candidate listing via `RecyclerView` with `StaggeredGridLayoutManager` for displaying more candidates at once.
- Used libchewing's internal punctuation and symbol candidates, replacing initial hardcoded lists.
- Styled candidates with serif font.

## Punctuation & Symbols
- Built punctuation picker as a popup window.
- Added comma key as a quick punctuation input shortcut.
- Integrated libchewing's built-in punctuation and symbol selection.

## Settings & Configuration
- Added settings UI in `MainActivity`.
- Created engineering mode activity with test input field.
- Added Bopomofo keyboard layout selection (DaChen, E-Ten 26).
- Enabled `viewBinding` for the project.

## Input Method Switching
- Implemented IME switcher/picker support.
- Set `android:supportsSwitchingToNextInputMethod` to true.

## App Identity
- Designed app icon (golden keycaps, v2).
- Added Traditional Chinese (zh-rTW) string resources.
- Applied GPLv2 license.
- Added copyright notices and libchewing license information.

## Testing
- Created `ChewingEngineInstrumentedTest` with test cases for layout switching, candidate selection, and symbol input.
- Moved test input field from `MainActivity` to engineering mode.
