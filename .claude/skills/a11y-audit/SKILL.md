---
name: a11y-audit
description: Audit Guileless Bopomofo for accessibility (TalkBack, touch targets, contrast, text scaling, keyboard navigation) across the Compose input view and the settings screens. Use when the user asks for an accessibility or a11y audit, asks whether a change is accessible, or before a release.
argument-hint: optional scope — a file, a screen (keyboard, candidates, settings) or "diff" for the current changes
---

# Accessibility Audit

You are auditing Guileless Bopomofo for accessibility. An input method is used by
everyone who types, including people who rely on TalkBack, switch access, large text,
high contrast or a physical keyboard alone. Keep the audit honest: report what the code
and the device show, not what the project intends.

## Scope

Default to the whole app. With an argument, narrow to it:

- A file or directory: audit only that.
- `keyboard`, `candidates`, `settings`: the `ui/ime/` keyboard layouts and keypad, the
  `ui/ime/CandidatesPanel.kt` and compact bar, or `ui/settings/` + `MainActivity.kt`.
- `diff`: the uncommitted changes and the current branch's commits since `master`.

Everything the user sees is Jetpack Compose (no XML layouts), so Android Lint's
view-based accessibility checks do not apply. The audit is a reading of the Compose
tree plus checks on the development phone.

## Checklist

### 1. Labels for screen readers

- Every interactive element that shows only an icon has a `contentDescription` or
  `semantics { contentDescription = ... }` from a string resource. `contentDescription =
  null` is right only for decoration next to visible text.
- Soft keys (`KeyButton`, `KeySurface`, the compact keypad cells, candidate buttons):
  TalkBack must read what the key types or does. A Bopomofo key reads its symbol, a
  function key reads its action (Backspace, Space, Enter, Shift, switch to English, ...).
  The Enter key's label follows `ImeUiState.enterKeyBehavior` (Send, Search, Go, ...).
- Labels come from `res/values*/strings.xml` and exist in every translation the app ships.
- Candidates read their text and their selection number when a selection key applies.
- Nothing reads a `testTag`, a resource name or an English-only literal.

### 2. State and role

- Toggles use `toggleable(role = Role.Switch)` / `selectable(role = Role.RadioButton)`
  (see `ui/settings/SettingsComponents.kt`) rather than a bare `clickable`.
- Modal state on the keyboard is announced: Shift pressed/held (`ShiftKey.ShiftKeyState`),
  Bopomofo vs English, half vs full width, the pre-edit buffer's contents and cursor.
  Prefer `stateDescription`, `selected` or `toggleableState` over a changing label.
- Buffer changes (`preEditText`, candidates appearing) reach TalkBack through
  `liveRegion` or an announcement, not silently.

### 3. Touch targets

- Interactive elements are at least 48×48 dp. For keys, measure the touchable slot, not
  the drawn face: `KeySurface` answers to touch across its insets
  (`GuilelessDimens.KeyHorizontalInset`/`KeyVerticalInset`). Check the smallest key
  height the user can set, landscape and the compact keypad.
- Candidate buttons and compact bar indicators meet the same minimum.
- Adjacent targets do not overlap once insets are considered.

### 4. Contrast

- Text against its background: 4.5:1 (3:1 for large text). Icons and key outlines
  against their background: 3:1. Check each pairing in `ui/theme/GuilelessTheme.kt`
  (the `colors.xml` palette mapped onto Material 3), in both light and dark schemes,
  and the Shift key's three states.
- Compute the ratios rather than eyeballing: a short script with the WCAG relative
  luminance formula over the hex values in `res/values/colors.xml` and
  `res/values-night/colors.xml` if present.
- Nothing relies on colour alone (Shift state, active mode, selected candidate).

### 5. Text scaling and layout

- Text uses `sp` (or `MaterialTheme.typography`), not `dp`, so system font size applies.
  Where a fixed size is deliberate (glyphs on keys), say so and check they stay legible
  at the largest font scale and smallest display size.
- No clipped or overlapping text at font scale 2.0, in portrait and landscape, on a
  small phone and on sw600dp.
- Settings screens scroll; nothing is trapped off-screen.

### 6. Keyboard and switch navigation

- The settings screens are fully operable with a hardware keyboard: Tab order follows
  the visual order, focus is visible, every control activates with Enter or Space.
- The input view itself: candidates can be chosen with selection keys and arrows;
  nothing is reachable only by touch gestures. Long-press and swipe shortcuts have an
  alternative.

### 7. Motion, haptics and timing

- Vibration and animations respect the user's own settings (`Vibratable`, the
  vibration preference) and the system's reduced-motion / animator scale.
- Nothing times out: candidates and the suggestion strip wait for the user.

### 8. Privacy under accessibility services

- The IME exposes to accessibility services only what a sighted user sees. The pre-edit
  buffer is fine; committed text, passwords (`TYPE_TEXT_VARIATION_PASSWORD` and friends)
  and the suggestion strip's source are not read or logged.

## How to Run

### Static pass

```bash
# icon-only elements without a label
grep -rn "contentDescription = null" app/src/main/java --include='*.kt'
# interactive modifiers that may lack a role or label
grep -rn "clickable\|toggleable\|selectable\|keyGestures" app/src/main/java --include='*.kt'
# semantics the app sets today
grep -rn "semantics\|stateDescription\|liveRegion\|Role\." app/src/main/java --include='*.kt'
# text sizes in dp instead of sp
grep -rn "fontSize = [0-9.]*\.dp" app/src/main/java --include='*.kt'
```

Then read `ui/ime/Keyboard.kt`, `ui/ime/PhoneKeypad.kt`, `ui/ime/CandidatesPanel.kt`,
`ui/ime/ImeRoot.kt` and `ui/settings/` with the checklist in hand.

### On the development phone

The Nokia G50 is the development device; keep the debug build installed and never
uninstall it (that disables the IME). Ask the user to unlock the phone first.

```bash
./gradlew :app:installDebug
# dump the accessibility tree of what is on screen and inspect content-desc / class / bounds
adb exec-out uiautomator dump /dev/tty | python3 -c 'import sys,re; print(re.sub(r"><", ">\n<", sys.stdin.read()))' | grep -o 'content-desc="[^"]*"\|bounds="[^"]*"\|class="[^"]*"' | paste - - -
# font scale and display size extremes
adb shell settings put system font_scale 2.0      # back to 1.0 afterwards
adb shell wm density 540                           # `adb shell wm density reset` afterwards
```

For TalkBack itself, ask the user to switch it on (Settings → Accessibility → TalkBack)
and walk the keyboard, the candidates and each settings tab; `adb` cannot hear what
TalkBack says. The Accessibility Scanner app, if installed, gives a second opinion on
touch targets and contrast. Restore every setting you changed before you finish.

### Compose tests

Where a finding is testable, add or extend an instrumented test in
`app/src/androidTest/.../ui/` using `onNodeWithContentDescription`,
`assertContentDescriptionEquals`, `assertHasClickAction`, `assertIsToggleable`,
`assertTouchHeightIsEqualTo` / `assertTouchWidthIsEqualTo`. Run with
`-Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true`.

## Output Format

Lead with the verdict in one or two sentences. Then findings by severity, each with
`file:line`, what a user experiences, and the fix. Skip empty sections.

### Blocking
A user who depends on an assistive technology cannot complete a core task: type,
pick a candidate, commit, change a setting.

### Should fix
A task is possible but confusing, slow or error-prone: unlabeled secondary control,
a state that is not announced, a target under 48 dp, a contrast ratio under threshold.

### Nice to have
Polish: a better label, grouping, a live region.

### Checked and fine
A short list of what was verified and passed, so the next audit need not repeat it.

End with what was not checked (no TalkBack walk, no tablet, ...) and why. Do not apply
fixes unless the user asks; the audit is the deliverable.
