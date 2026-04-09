---
name: docs-engineering
description: Writing/updating project documentation (README, PRIVACY-POLICY, NOTICES, changelogs) and maintaining F-Droid metadata. Use when the user asks to update docs, write changelogs, or modify F-Droid store listings.
argument-hint: task description
---

# Document Engineering

You are performing documentation tasks for Guileless Bopomofo.

## Bilingual Convention

This project maintains documentation in both **English (en-US)** and **Traditional Chinese (zh-TW)**. When creating or updating user-facing text, always produce both language versions unless instructed otherwise.

- README.md uses interleaved bilingual sections (English heading/paragraph followed by Chinese equivalent).
- F-Droid metadata keeps separate locale directories.

## Project Documentation Files

| File | Purpose |
|------|---------|
| `README.md` | Project overview, usage guide, build instructions (bilingual) |
| `PRIVACY-POLICY.md` | Privacy policy |
| `NOTICES.md` | Third-party license notices |
| `CLAUDE.md` | Guidance for Claude Code |

When updating `NOTICES.md`, check `gradle/libs.versions.toml` and `app/build.gradle.kts` for current dependencies and their licenses.

## F-Droid Metadata

Metadata lives under `fastlane/metadata/android/` with two locales:

```
fastlane/metadata/android/
  en-US/
    title.txt              # App name (max 50 chars)
    short_description.txt  # Store tagline (max 80 chars)
    full_description.txt   # Full store listing (max 4000 chars)
    changelogs/<versionCode>.txt  # Per-version changelog
    images/                # Screenshots and graphics
  zh-TW/
    (same structure)
```

### Changelogs

- Changelog filenames use **versionCode** (integer), not versionName. Find the current versionCode in `app/build.gradle.kts` (`versionCode = ...`).
- Write changelogs in a friendly, first-person tone consistent with existing entries (see `fastlane/metadata/android/en-US/changelogs/` for examples).
- Always create both `en-US` and `zh-TW` versions.
- Max 500 characters per changelog file.

### Store Descriptions

- `full_description.txt` is written in first person from the app's perspective ("Hello, it's me, Guileless Bopomofo Keyboard.").
- Use `*` for bullet points in en-US, `＊` (fullwidth asterisk) in zh-TW.

## Writing Style

- Keep language clear and approachable.
- For zh-TW text, use Traditional Chinese characters only. Do not use Simplified Chinese.
- Preserve the existing tone: the app "speaks" in first person in store listings and changelogs; README and other docs use standard third-person technical writing.
- When referencing keyboard layouts, use their established names: Dachen (大千), Hsu (許氏), E-Ten (倚天), Dachen CP26 (大千26鍵).

## Task: $ARGUMENTS
