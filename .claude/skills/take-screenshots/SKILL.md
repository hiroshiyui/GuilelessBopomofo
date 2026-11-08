---
name: take-screenshots
description: Retake the app screenshots (phone, 7" tablet, 10" tablet) on the current build and refresh them in README.md and the fastlane store listing. Use when the user asks to take, retake or update screenshots.
argument-hint: optional scope, e.g. "phone only" or "tablets only"
---

# Take Screenshots

You are refreshing the screenshots of Guileless Bopomofo. Every capture is a raw
`adb exec-out screencap -p`; no device frame is added.

## Where the screenshots live

| Set | Device | Files |
|-----|--------|-------|
| Phone | Nokia G50 (the user's dev phone, 720x1640) | `media/Screenshot_YYYYMMDD_HHMMSS.png`, embedded in `README.md`; copied to `fastlane/metadata/android/{en-US,zh-TW}/images/phoneScreenshots/N_<locale>.png` |
| 7" tablet | AVD `Tablet7` (7" Tablet profile, 1080x1920 portrait) | `fastlane/metadata/android/{en-US,zh-TW}/images/sevenInchScreenshots/{1,2}_<locale>.png` |
| 10" tablet | AVD `PixelTablet` (Pixel Tablet profile, 2560x1600 landscape) | `fastlane/metadata/android/{en-US,zh-TW}/images/tenInchScreenshots/{1,2}_<locale>.png` |

The en-US and zh-TW copies are identical files; the app UI is always shown in
Traditional Chinese. Phone set order: 1 general settings, 2 keyboard composing
text, 3 interface settings tab, 4 physical keyboard settings tab, 5 user
vocabulary tab. Tablet sets have only 1 (general settings) and 2 (keyboard).

When replacing README images, `git rm` the old dated files and update the
`<img src="./media/..." width="160">` tags inside the centred `<p>` block
(HTML tags, not Markdown images, so the width is capped on desktop browsers).
Leave unrelated images in `media/` alone.

## Shared setup

1. Build the debug APK: `./gradlew :app:assembleDebug -q`. Its versionName must
   match `app/build.gradle.kts`; the version line is visible in the first shot.
2. Install with `adb -s <serial> install -r <apk>`. **Never uninstall** on the
   phone: that drops the IME from the enabled keyboards.
3. Make the IME current on the target:
   ```bash
   adb -s <serial> shell ime enable org.ghostsinthelab.apps.guilelessbopomofo/.GuilelessBopomofoService
   adb -s <serial> shell ime set    org.ghostsinthelab.apps.guilelessbopomofo/.GuilelessBopomofoService
   adb -s <serial> shell settings put secure default_input_method org.ghostsinthelab.apps.guilelessbopomofo/.GuilelessBopomofoService
   ```
   `ime set` alone did not stick on the emulator; the `settings put` is what held.
4. Work in the scratchpad directory, then copy the final PNGs into the repo.

## Capturing the general settings screen

`adb shell am start -n org.ghostsinthelab.apps.guilelessbopomofo/.MainActivity`,
wait ~3 s, screencap. The bottom tab bar (phone, y≈1525) has the four tabs at
x≈84 / 267 / 450 / 633; tap each and capture for shots 3 to 5.

## Capturing the keyboard shot

The phrase shown is 貓在鋼琴上昏倒 with a pending ㄉ, typed on the **Dachen**
soft keyboard by tapping on-screen keys. Dachen Bopomofo-key sequence
(`sp` = space, which confirms tone 1):

```
貓 ㄇ ㄠ sp | 在 ㄗ ㄞ ˋ | 鋼 ㄍ ㄤ sp | 琴 ㄑ ㄧ ㄣ ˊ
上 ㄕ ㄤ ˋ | 昏 ㄏ ㄨ ㄣ sp | 倒 ㄉ ㄠ ˇ | then ㄉ
```

Dachen grid, rows top to bottom (10 columns each):
```
ㄅ ㄉ ˇ ˋ ㄓ ˊ ˙ ㄚ ㄞ ㄢ
ㄆ ㄊ ㄍ ㄐ ㄔ ㄗ ㄧ ㄛ ㄟ ㄣ
ㄇ ㄋ ㄎ ㄑ ㄕ ㄘ ㄨ ㄜ ㄠ ㄤ
ㄈ ㄌ ㄏ ㄒ ㄖ ㄙ ㄩ ㄝ ㄡ ㄥ
```
Take one screencap with the keyboard open first and read the key centres from
it; they differ per device. Known centres:
- Phone 720x1640: columns x = 36 + 72·c, rows y = 1140 / 1231 / 1322 / 1413, space (252,1503), backspace (550,1503).
- Tablet7 1080x1920: columns x = 53 + 108·c, rows y = 1350 / 1453 / 1557 / 1661, space (380,1766).
- PixelTablet 2560x1600: columns x = 128 + 256·c, rows y = 1030 / 1133 / 1238 / 1341, space (960,1444).

Sleep ~0.4 s between taps. Afterwards clear the composition with backspace taps.

**Do not use `adb shell input text`.** It arrives as hardware key events, so the
IME switches to its compact physical-keyboard bar and the layout mapping is
wrong.

### Text field to type into

- Phone: Chrome on `about:blank` (`am start -a android.intent.action.VIEW -d about:blank com.android.chrome`), then tap the omnibox at (300,100). Chrome's normal new-tab page is full of personal feed content and the incognito page blocks screen capture, so use a blank tab.
- Emulators: the app's own user-vocabulary tab search field (fourth tab, then tap the search box). Chrome on a fresh emulator shows first-run dialogs.

## Emulators

Both AVDs use the installed image
`system-images;android-36;google_apis_playstore_ps16k;x86_64`. Recreate if
missing:
```bash
AVD=$ANDROID_HOME/cmdline-tools/latest/bin/avdmanager
echo no | $AVD create avd -n Tablet7     -k "system-images;android-36;google_apis_playstore_ps16k;x86_64" -d '7" Tablet'  -f
echo no | $AVD create avd -n PixelTablet -k "system-images;android-36;google_apis_playstore_ps16k;x86_64" -d pixel_tablet -f
```
Boot headless, one at a time, and poll `sys.boot_completed`:
```bash
nohup $ANDROID_HOME/emulator/emulator -avd Tablet7 -no-window -no-audio -no-boot-anim -gpu swiftshader_indirect -port 5554 >emu.log 2>&1 &
```
Kill with `adb -s emulator-5554 emu kill` when done.

Emulator specifics:
- The Play image is a user build, so the system locale cannot be changed over adb. Set a per-app locale instead and restart the app: `adb shell cmd locale set-app-locales org.ghostsinthelab.apps.guilelessbopomofo --locales zh-Hant-TW`. The status bar stays English; that is expected.
- On PixelTablet (landscape) the IME would open in full-screen extract mode with a system "Done" button. In the app's interface settings tab, switch off "當畫面橫置時，啟用全螢幕模式" before the keyboard shot so the keyboard appears over the app.
- Wake and unlock before launching: `input keyevent KEYCODE_WAKEUP`, `wm dismiss-keyguard`.

## Phone specifics

- The phone must be unlocked and awake. Ask the user if it is not.
- Shell quoting: `E="adb -s X"` does not word-split in zsh; use a function `E(){ adb -s X "$@"; }`.
- The user's preferred soft layout is **Hsu**. If the phone is on another layout, restore it afterwards through the app's layout dropdown (general settings tab), not by editing preferences.
- `input keyevent BACK` may leave the app entirely; relaunch rather than assume.

## Finishing

Inspect every capture by reading the PNG before copying it into the repo. Then
offer to commit with `/commit-and-push` (subject like
"Update screenshots for X.Y.Z") and to fast-forward `master`.
