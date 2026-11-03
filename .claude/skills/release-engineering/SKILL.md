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
4. **Run the tests** — `./gradlew :app:test`, so the tag lands on a known good state.
5. **Create release commit** — commit message format: `Release <versionName>` with `Signed-off-by` trailer.
6. **Tag the release** — lightweight tag matching versionName (e.g. `3.7.5`). Tags are NOT annotated.
7. **Build signed APKs** — only now, after the commit and tag: F-Droid builds reproducibly from the tagged commit, so the published APKs have to be built from exactly that commit. Signing requires a keystore passphrase, so **do not run the build automatically**. Prompt the user to build the signed APKs manually in Android Studio (Build > Generate Signed APK) or via the command line. Output: `app/build/outputs/apk/release/` (debug builds go to `app/build/outputs/apk/debug/`). Android Studio may also leave copies in `app/release/` and `app/debug/`, but the user signs the ones under `app/build/outputs/apk/`. Wait for the user to confirm the build is complete before proceeding.
8. **GPG-sign** the APKs (see GPG Signing below). Once the user confirms signing and the signatures verify, **push** the release branch and tag to `origin` and `gitlab` without asking. Push only to these two remotes, even if `git remote` lists others, then create the GitHub Release.
9. **Merge `current` into `master`** — once the GitHub Release is published, bring `master` up to the release and push everything to both remotes without asking:
   ```bash
   git checkout master
   git merge --ff-only current
   git push origin master && git push gitlab master
   git checkout current
   ```
   `master` has always been fast-forwarded to `current`, so it never gets merge commits of its own. If `--ff-only` fails, `master` has commits that `current` lacks: stop and ask the user instead of creating a merge commit. Afterwards `current`, `master` and the release tag should be on `origin` and `gitlab`, all at the release commit (`git ls-remote origin` and `git ls-remote gitlab` to check).

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

GPG signing requires interactive passphrase entry — **do not run `gpg` commands automatically**. Instead, prompt the user to sign the APKs manually with the commands below.

The APK filename follows the pattern `org.ghostsinthelab.apps.guilelessbopomofo_v<versionName>-<buildType>.apk` (configured by the `archivesName` setting in `app/build.gradle.kts`).

Provide the user with the exact commands to run:
```bash
gpg --detach-sign --armor <apk-file>
```

For example, from the project root:
```bash
gpg --detach-sign --armor app/build/outputs/apk/release/org.ghostsinthelab.apps.guilelessbopomofo_v3.7.5-release.apk
gpg --detach-sign --armor app/build/outputs/apk/debug/org.ghostsinthelab.apps.guilelessbopomofo_v3.7.5-debug.apk
```

Wait for the user to confirm signing is complete before proceeding to the GitHub Release step.

Verifying the signatures does not need a passphrase, so do it before uploading. gpg prints in the user's locale, so force English output and look for `GOODSIG`:
```bash
LC_ALL=C gpg --status-fd 1 --verify <apk-file>.asc <apk-file> | grep GOODSIG
```

## GitHub Release

After building and GPG-signing the APKs, create a GitHub Release using `gh`.

### Release Notes

Write a human-friendly release note body — do **not** rely solely on `--generate-notes`. The note should:

- Start with both the **en-US** and **zh-TW** F-Droid changelogs (already written earlier in the process), separated by a blank line.
- Append the auto-generated changelog (commit list) below as a "Full Changelog" section for completeness.

Structure:
```markdown
<en-US changelog content>

<zh-TW changelog content>

<!-- auto-generated below -->
**Full Changelog**: https://github.com/hiroshiyui/GuilelessBopomofo/compare/<previous-tag>...<versionName>
```

### Creating the Release

```bash
gh release create <versionName> \
  --title "<versionName>" \
  --notes "$(cat <<'EOF'
<release notes body as described above>
EOF
)" \
  <apk-and-asc-files...>
```

Upload **both** debug and release APKs along with their `.asc` signature files (4 assets total), all from `app/build/outputs/apk/{debug,release}/`:
- `org.ghostsinthelab.apps.guilelessbopomofo_v<version>-debug.apk`
- `org.ghostsinthelab.apps.guilelessbopomofo_v<version>-debug.apk.asc`
- `org.ghostsinthelab.apps.guilelessbopomofo_v<version>-release.apk`
- `org.ghostsinthelab.apps.guilelessbopomofo_v<version>-release.apk.asc`

The release title matches the tag/versionName (e.g. `3.7.5`).

## Important Reminders

- Push the release commit, the tag, and the fast-forwarded `master` to `origin` and `gitlab` without asking for confirmation (ask before pushing to any other remote); the user has pre-approved this for releases. Never force-push.
- Create the GitHub Release once the user confirms the APKs are built and signed.
- Run tests before tagging a release if the user hasn't already.
- The release APK requires a signing configuration — the user handles this outside of version control.
- F-Droid builds from source using the tagged commit, so the tag must point to a buildable state.

## Task: $ARGUMENTS
