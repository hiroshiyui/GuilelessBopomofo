---
name: release-engineering
description: Release engineering tasks including version bumping, building release APKs, creating git tags, writing changelogs, and preparing F-Droid releases. Use when the user asks to prepare a release, bump version, tag a release, or build for distribution.
argument-hint: task description
---

# Release Engineering

You are performing release engineering tasks for Guileless Bopomofo.

## Version Scheme

- **versionName**: semver-style `MAJOR.MINOR.PATCH` (e.g. `3.7.4`)
- **versionCode**: monotonically increasing integer (e.g. `199`)
- Both are defined in `app/build.gradle.kts`

Bump with:
```bash
./gradlew bumpPatchVersion   # increments both versionCode (+1) and patch (+1)
```

For minor or major bumps, edit `app/build.gradle.kts` manually — the task only handles patch.

## Release Process

The established release workflow follows this pattern:

1. **Ensure all changes are committed** on the working branch.
2. **Bump version** — run `./gradlew bumpPatchVersion` or edit `app/build.gradle.kts` manually for minor/major bumps.
3. **Write changelogs** for F-Droid (see Changelog section below).
4. **Build the release APK**:
   ```bash
   ./gradlew :app:assembleRelease
   ```
   Output: `app/build/outputs/apk/release/`
5. **Create release commit** — commit message format: `Release <versionName>` with `Signed-off-by` trailer.
6. **Tag the release** — lightweight tag matching versionName (e.g. `3.7.5`). Tags are NOT annotated.
7. **Push** the commit and tag when the user confirms.

### Git Conventions

- Release commit message: `Release X.Y.Z`
- Tag name: `X.Y.Z` (just the version, no `v` prefix)
- Tags are lightweight (not annotated): `git tag <versionName>`
- Commits are signed off: `--signoff`

## Changelogs (F-Droid)

Changelogs live at:
```
fastlane/metadata/android/en-US/changelogs/<versionCode>.txt
fastlane/metadata/android/zh-TW/changelogs/<versionCode>.txt
```

- Filename is the **versionCode** (integer), not the versionName.
- Max 500 characters per file.
- Written in a friendly, first-person tone from the app's perspective.
- Always create both en-US and zh-TW versions.
- Use `*` for bullet points in en-US, `＊` (fullwidth asterisk) in zh-TW.
- To determine what changed, review commits since the previous release tag:
  ```bash
  git log --oneline <previous-tag>..HEAD
  ```

## Build Commands Reference

```bash
./gradlew :app:assembleDebug       # Debug build
./gradlew :app:assembleRelease     # Release build (minified + shrunk)
./gradlew :app:test                # Unit tests
./gradlew :app:connectedAndroidTest # Instrumented tests
./gradlew :app:clean               # Clean (includes libchewing build artifacts)
```

## GPG Signing

Built APKs must be signed with GPG using a detached ASCII-armored signature:
```bash
gpg --detach-sign --armor <apk-file>
```

For example:
```bash
gpg --detach-sign --armor org.ghostsinthelab.apps.guilelessbopomofo_v3.7.5-release.apk
```

The APK filename follows the pattern `org.ghostsinthelab.apps.guilelessbopomofo_v<versionName>-<buildType>.apk` (configured by the `archivesName` setting in `app/build.gradle.kts`). This produces a `.asc` signature file alongside the APK.

## GitHub Release

After building and GPG-signing the APKs, create a GitHub Release using `gh`:

```bash
gh release create <versionName> \
  --title "<versionName>" \
  --generate-notes \
  --notes-start-tag <previous-tag> \
  <apk-and-asc-files...>
```

Upload **both** debug and release APKs along with their `.asc` signature files (4 assets total):
- `org.ghostsinthelab.apps.guilelessbopomofo_v<version>-debug.apk`
- `org.ghostsinthelab.apps.guilelessbopomofo_v<version>-debug.apk.asc`
- `org.ghostsinthelab.apps.guilelessbopomofo_v<version>-release.apk`
- `org.ghostsinthelab.apps.guilelessbopomofo_v<version>-release.apk.asc`

The release title matches the tag/versionName (e.g. `3.7.5`). Use `--generate-notes --notes-start-tag` to auto-generate the changelog from the previous release tag.

## Important Reminders

- Always confirm with the user before pushing commits/tags or creating GitHub releases.
- Run tests before tagging a release if the user hasn't already.
- The release APK requires a signing configuration — the user handles this outside of version control.
- F-Droid builds from source using the tagged commit, so the tag must point to a buildable state.

## Task: $ARGUMENTS
