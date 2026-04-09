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
