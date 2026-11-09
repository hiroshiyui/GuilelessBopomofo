# Changelog — 5.x

## 5.2.1
- Fixed the pre-edit buffer's "move cursor left/right" accessibility actions, which only re-read libchewing's cursor and never moved it, leaving every character before the last out of a screen reader's reach. The buffer now also states the cursor position through a state description.
- Exposed long presses (common punctuation, input method picker) to accessibility services, and made an accessibility click on the IME switch key work when the double-touch switch is enabled (`VirtualKey.onAccessibilityClick`).
- Stopped announcing the buffers as a live region in password fields (`EnglishWords.isPasswordField`, `ImeUiState.inPasswordField`).
- Capped the font scale of candidates (1.3x) and suggestions (1.5x), whose fixed-height boxes cut the text off at large font sizes.
- Raised contrast in the settings: the selected tab label, and selected radio buttons, switches, sliders and focused text fields, now use the primary text color instead of the theme's too-faint secondary and primary.
- Laid the physical keyboard tips out as a bulleted list with a hanging indent, the bullet drawn by Compose rather than typed into the translated strings and hidden from screen readers, and set them in an information card.

## 5.2.0
- Made the on-screen keyboard usable with screen readers. Every key surface is one Button node with a label and a click action: Bopomofo keys read their symbols (taken from the keycap drawable's name, `KeyLabels`), function keys their action, Enter the editor action it will perform, candidates and suggestions their text. The compact keypad's swipe keys offer what their swipes type as accessibility actions.
- Announced keyboard state: the Shift key carries a state description (off, on for the next key, locked) and shows a dot or a bar under its arrow so the state no longer rests on color alone; the pre-edit buffer is a polite live region with "move cursor left/right" actions; the candidates panel is a titled pane; the compact bar and each paged candidate read as one node.
- Fixed glyph contrast on the colored keys: each Shift state draws its glyph in the color that belongs to its background, and the dark theme's Enter glyph uses base10, so every glyph-on-key pairing passes 3:1.
- Added an Accessibility section to the user interface settings with an optional 1dp outline around every key face (`USER_OUTLINE_KEYS`, off by default). Key preferences (height, IME switch key, outline) now apply the moment they change rather than at the next layout switch.
- Raised the key height floor to 48dp; a smaller saved value is lifted to it.
- Named the settings sliders for screen readers and marked the screen and section titles as headings. The settings header keeps its version line clear of the edges at large font sizes.
- Let a physical Enter perform the text field's editor action (Send, Done, ...) in multi-line fields too, with Shift+Enter or Alt+Enter for a line break; the on-screen Enter keeps Android's rule (a line break in multi-line fields) and shows an icon for the action it will perform (Send, Search, Go/Next, Previous, Done).
- Stripped the off-canvas paths from the 181 keycap and 8 key icon vectors, which carried every other glyph of their Inkscape sheet: 10,073 of 10,446 paths never drew. Each was verified pixel for pixel against its original. The drawables shrink from 7.8MB to under 1MB, the release APK from 13.0MB to 10.3MB, and keyboard composition is markedly faster.
- Added screenshots of every settings tab and refreshed the phone and tablet screenshots in `README.md` and the store listing.

## 5.1.6
- Added the hotkeys the physical keyboard tips were missing against `README.md`: Shift+Enter or Alt+Enter for a line break where Enter would submit, a long press of the right Shift for the punctuation candidates, and the ` key for the symbol categories, with ← and → paging. Enter, the arrows and ` are drawn as key caps by Libertinus Keyboard like the other key names.
- Reworded each tip to name the feature first and the keys after, and laid the tips out as a full-width list below the info icon, one tip per line with a solid bullet and a gap between tips. The lead-in line is its own string, `tips_using_physical_keyboard_intro`.
- Labelled the candidate selection key choices in capitals (`QWERTYUIOP`, `ASDFGHJKL;` and the rest), as physical key caps are printed; the stored preference names and the key codes passed to libchewing are unchanged.
- Upgraded the Android Gradle Plugin to 9.5.1.

## 5.1.5
- Set the physical key names in the physical keyboard tips (Shift, Alt, Space, I) in Libertinus Keyboard, whose `liga` ligatures draw each name as one key cap. The Traditional Chinese and Japanese tips now name the space bar "Space", since the font only ligates the English key names. The font (SIL Open Font License 1.1) ships as a font resource with its license in assets, credited in `NOTICES.md`.
- Drew the candidate selection key choices (`1234567890`, `asdfghjkl;` and the rest) as a row of key caps in the same typeface, one cap per key.
- Generalized the settings' bold-markup string helper into `markupStringResource`, which takes the span style the `<b>` markup stands for, and gave `RadioGroupPreference` an optional label style.

## 5.1.4
- Moved the last logic out of `GuilelessBopomofoService`: carrying out the Enter key into `EnterKeyAction`, and preference changes to the classes that own them, `KeyboardController` for the layout keys and `EnglishSuggestions` for the prediction keys, each covered by tests.

## 5.1.3
- Made punctuation typed right after a chosen English suggestion close up against the word: the space the suggestion was committed with is taken back before `, . ! ? ; : ) ] }` and `'`, so `word ,` becomes `word,`. The space is only taken back while the cursor is still right after that word; moving the cursor, deleting, pressing Enter or selecting text leaves it alone.
- Added UI Automator tests that drive the real keyboard in the engineering screen's text field, typing Bopomofo, converting, choosing a candidate, Backspace, Enter and the mode switch to English, and walk the settings tabs and a preference switch.
- Added tests for what had none: a JVM unit test for the action queue behind `ImeAction` dispatch, now its own `ActionQueue` class, and instrumented tests for `PhysicalKeyInput`, `ChewingSettings` and every on-screen key.
- Split `GuilelessBopomofoService` by concern: hardware key handling moved to `PhysicalKeyInput`, the English suggestion strip to `EnglishSuggestions` (now covered by JVM unit tests with a fake text field), libchewing configuration to `ChewingSettings`, and stepping back out of the candidates or symbols to `KeyboardController`. No change in behavior.
- Replaced the `physicalKeyboardPresented` global in `GuilelessBopomofoEnv` with a `PhysicalKeyboard` object the service owns and hands to the classes that read it, and gave the suggestion strip an explicit `shutDown()` for teardown so nothing touches libchewing after its context is freed.
- Removed greenrobot EventBus. Everything a key or panel asks of the input method is now a sealed `ImeAction` handed to an `ImeDispatcher` the service implements; keys receive it as an `ImeHost` and Compose reads it from `LocalImeHost`. Actions asked for while one is being carried out still run after it, as nested EventBus posts did; dispatch is checked to be on the main thread, and a failing handler is logged by action kind rather than taking the keyboard down. The settings screens tell the service about a libchewing reconnection through a listener on `ChewingBridge`. Dropped the dependency, its ProGuard keep rules and its notice.

## 5.1.2
- Made a Shift tap toggle Bopomofo and English only when it is a quick press on its own: Shift pressed while Ctrl, Alt or another modifier is held, a held-down Shift that auto-repeats, or a Shift + mouse click no longer toggles the mode.
- Kept the tap timeout below the system long-press timeout.
- Cleared a Shift press left over when the input view closes, so it no longer swallows the next tap of the other Shift key.
- Passed Left Shift through to apps again, as before 5.1.1; only Right Shift, whose long press opens the frequently used candidates, is kept by the keyboard.
- Updated four instrumented tests to match libchewing 0.13.1's version, symbol list and user data file behavior.

## 5.1.1
- Added tapping either Shift key on its own to toggle Bopomofo and English, like Shift + Space. Chords such as Shift + Space or Shift + A, holding Shift, and long presses keep their previous behavior.
- Updated the physical keyboard tips to describe the shortcut.
- Added a Japanese translation to the README.
- Upgraded Android Gradle Plugin to 9.5.0, activity-compose to 1.13.0 and core-ktx to 1.19.1.

## 5.1.0
- Added optional English next-word prediction: with English word prediction on, the keyboard suggests the words that most often follow the last one after each space, built from Simple English Wikipedia. Nothing typed is stored.

## 5.0.1
- Fixed the keyboard sitting too close to the IME navigation bar (back and keyboard-switch buttons) on Android 15 and later with gesture navigation.

## 5.0.0
- Migrated the whole input method UI, the settings screens and engineering mode from Views and XML layouts to Jetpack Compose, keeping the existing look.
- Added the Compact DaChen keyboard: Bopomofo on phone keypad keys, tap for the main symbol and swipe for the others.
- Added a theme choice (Light, Dark or follow the system) with optional dynamic color, and filled every Material 3 color role from the Morandi and Wine palettes.
- Added Alt + Enter on physical keyboards to insert a line break.
- Made the JNI getters return empty values instead of null where Kotlin expects non-null results.
- Addressed the findings of code reviews and security audits of the input method runtime, settings, theme and build.
- Removed unused resources, dependencies (including Material Components) and the bottom navigation menu.
- Upgraded the Gradle wrapper to 9.8.0 and updated the GPG public key with its extended expiry.
