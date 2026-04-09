# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Guileless Bopomofo is an Android input method editor (IME) for Bopomofo/Zhuyin phonetic input, powered by [libchewing](http://chewing.im/). It supports multiple keyboard layouts (Dachen, Hsu, E-Ten, etc.) for both virtual and physical keyboards. Licensed under GPLv3, distributed via F-Droid.

## Build Commands

```bash
# Clone (requires --recursive for libchewing submodule)
git clone --recursive https://github.com/hiroshiyui/GuilelessBopomofo.git

# Build
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease

# Tests
./gradlew :app:test                     # Unit tests
./gradlew :app:connectedAndroidTest     # Instrumented tests (requires device/emulator)

# Version management
./gradlew bumpPatchVersion              # Increment versionCode and patch in versionName
```

**Prerequisites**: Rust toolchain (installed automatically via `rustup` if missing during build). CMake 3.24+ and NDK 28.1.13356709 (configured in Android SDK).

**Pre-build steps run automatically**: `installSpecifiedRustToolchain` and `copyChewingDataFiles` are wired as `preBuild` dependencies. The latter triggers `prepareChewing` -> `buildChewingData` -> `copyChewingDataFiles`, which compiles libchewing dictionary data and copies `tsi.dat`, `word.dat`, `swkb.dat`, `symbols.dat` into `app/src/main/assets/`.

## Architecture

### Native Layer (libchewing integration)

- **`app/src/main/cpp/libs/libchewing`** - Git submodule (Rust library with C FFI, from codeberg.org/chewing)
- **`app/src/main/cpp/CMakeLists.txt`** - Builds JNI shared library, links libchewing statically via Corrosion (Rust-CMake bridge)
- **`app/src/main/cpp/libchewing_android_jni.cpp`** - JNI bridge exposing libchewing C API to Kotlin

### Kotlin Layer

- **`Chewing.kt`** - JNI wrapper class with `external` function declarations; loads `libchewing_android_jni` native library
- **`ChewingBridge.kt`** - Singleton holding the global `Chewing` instance; defines enums for input modes (`ChiEngMode`, `ShapeMode`) and keyboard layouts
- **`GuilelessBopomofoService.kt`** - Main `InputMethodService` implementation; handles key events, composition, candidate selection for both virtual and physical keyboards
- **`KeyboardPanel.kt`** - Dynamically constructs virtual keyboard UI with layout-specific button arrangements
- **`MainActivity.kt`** - Settings/preferences activity

### Data Flow

User input -> `GuilelessBopomofoService` -> `Chewing` (JNI) -> libchewing (native) -> returns candidates/composition -> Service updates UI -> text committed to app

### Key Patterns

- **EventBus** (greenrobot) for decoupled event communication between components. ProGuard rules preserve `@Subscribe` methods.
- **Key handlers** are split: `keys/virtual/` for soft keyboard keys, `keys/physical/` for hardware keyboard keys.
- **`GuilelessBopomofoEnv`** holds SharedPreferences keys and device state constants.
- **View binding** is enabled (`buildFeatures.viewBinding = true`).

## Build Configuration

- Kotlin (no separate Kotlin Gradle plugin — uses AGP's built-in Kotlin support)
- Compile/Target SDK 36, Min SDK 23
- Version catalog at `gradle/libs.versions.toml`
- Release builds enable R8 minification and resource shrinking

## Developer Principles

### Code Quality

- **Null safety**: use Kotlin null-safe operators properly; avoid `!!` unless justified.
- **Thread safety**: correct use of coroutines; synchronized access to shared state.
- **Resource management**: no leaked cursors, streams, or native resources.
- **Error handling**: appropriate try/catch usage; never silently swallow exceptions.
- **Consistency**: follow existing codebase patterns (EventBus for events, ViewBinding for views, etc.).
- **No dead code**: remove unused imports, unreachable logic, and redundant code.

### Code Smells to Avoid

- Long methods doing too many things — break into smaller, focused functions.
- Large classes with too many responsibilities — split by concern.
- Duplicated code — extract shared logic into common functions.
- Deep nesting — prefer early returns or extraction over excessive `if`/`when`/`try` nesting.
- Magic numbers/strings — use named constants.
- Mutable shared state — prefer immutable data and local state; flag unnecessary `var` or global mutable collections.

### Refactoring Guidance

- Use Kotlin idioms over Java-style patterns — scope functions (`let`/`apply`/`also`), destructuring, extension functions, `buildList`/`buildString`.
- Replace complex `when`/`if-else` chains that switch on type with polymorphic dispatch.
- Model related constants/states with sealed classes or enums instead of loose strings/ints.
- Consolidate duplicate logic between `keys/virtual/` and `keys/physical/` handlers where appropriate.
- Reduce coupling via dependency injection, EventBus, or interface abstractions.
- Structure code for testability — avoid direct static calls and hidden dependencies.

### JNI / Native Safety

- **Buffer overflows**: ensure JNI string and array operations use correct lengths.
- **Null pointer dereference**: validate JNI references (`FindClass`, `GetMethodID` return values) before use.
- **Reference leaks**: release JNI local references when no longer needed, especially in loops.
- **Input validation**: validate data crossing the JNI boundary on both sides.
- **Memory management**: proper allocation/deallocation of native memory.

### Security

- **IME privacy**: as an IME handling all user keystrokes, never log or leak user input.
- **No hardcoded secrets**: no API keys, credentials, or secrets in source.
- **SharedPreferences**: no sensitive data stored in plain text.
- **Intent safety**: validate intent extras; guard against intent injection.
- **Export controls**: ensure components (activities, services, receivers) are not inadvertently exported.
- **No command injection**: avoid unsafe `Runtime.exec()` or `ProcessBuilder` usage.
- **No path traversal**: validate file paths in file operations.
- **ProGuard/R8**: ensure obfuscation rules don't strip security-critical code.
