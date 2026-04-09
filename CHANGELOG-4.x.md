# Changelog — 4.x

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
