# Changelog — 4.x

## 4.0.9
- Reverted the offline in-app announcements facility introduced in 4.0.8.

## 4.0.8
- Added an offline in-app announcements facility: a bell icon in the top-right of the main screen opens an Announcements list, with a small dot indicating unread items.
- Unread announcements auto-popup one at a time on app launch; tap "Got it" to mark as read.
- All announcement content is bundled with the app and rendered from Markdown — no network access required.

## 4.0.7
- Added a separate conversion engine preference for when a physical keyboard is attached, so the on-screen keyboard and the hardware keyboard can use different conversion engines (resolves issue #69).

## 4.0.6
- Upgraded compile/target SDK to 37.
- Upgraded Android Gradle Plugin to 9.2.1 and Gradle Wrapper to 9.4.1.
- Bumped Kotlin, kotlinx-coroutines, Material Components, AndroidX core-ktx, and Mockito to their latest versions.
- Updated the bundled libchewing library.

## 4.0.5
- Re-added the single-character filter in the User Phrase Manager so the list only shows phrases the user intentionally created.
- Hid the app header in landscape to give settings more vertical room.
- Fixed the dark-theme bottom navigation bar so the selected icon and its backdrop render correctly.
- Fixed the backup / restore / reset button labels so they stay readable in dark theme.

## 4.0.4
- Added a reset button in the User Phrase Manager to clear all user phrases and auto-learned history, with double confirmation to prevent accidental data loss.
- Fixed the edge-to-edge layout to pass the Play Console compatibility check.
- Updated the bundled libchewing library.

## 4.0.3
- Restored visibility of single-character user phrases in the User Phrase Manager. libchewing upstream has resolved the exclusion dictionary behavior, so the previous workaround is no longer needed.
- Updated the bundled libchewing library.

## 4.0.2
- Updated the bundled libchewing library.

## 4.0.1
- Added search to the User Phrase Manager: quickly find user phrases by phrase text or bopomofo.
- Hid single-character auto-learned entries from the phrase list to avoid confusion and work around a libchewing exclusion dictionary issue.

## 4.0.0
- Added User Phrase Manager: browse, add, and delete user phrases with bopomofo phonetic analysis, plus backup and restore support.
- Redesigned settings screen with bottom tab navigation for easier access.
- Added JNI methods for chewing userphrase functions.
- Fixed user phrases not persisting after force close.
- Fixed user phrase enumeration skipping every other entry.
- Conducted comprehensive security and code quality audit with all identified issues resolved.
- Added unit tests for events, ChewingUtil actions, and physical key handlers.
- Removed lateinit anti-pattern in KeyboardPanel.
- Upgraded libchewing to 0.13.0 and AGP to 9.1.0.
