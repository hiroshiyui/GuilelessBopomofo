---
name: code-review-and-security-audit
description: Review code for quality, correctness, and security vulnerabilities. Use when the user asks to review code, audit for security issues, or check for bugs and anti-patterns.
argument-hint: file path, component name, or scope of review
---

# Code Review and Security Audit

You are performing code review and security auditing for Guileless Bopomofo.

## Scope

This skill covers two complementary concerns:

1. **Code Review** — correctness, readability, maintainability, and adherence to project conventions.
2. **Security Audit** — identifying vulnerabilities, unsafe patterns, and potential attack surfaces.

## Review Checklist

### Code Quality

- Null safety: proper use of Kotlin null-safe operators; avoid `!!` unless justified.
- Thread safety: correct use of coroutines, synchronized access to shared state.
- Resource management: no leaked cursors, streams, or native resources.
- Error handling: appropriate use of try/catch; no silently swallowed exceptions.
- Consistency: follows existing patterns in the codebase (EventBus for events, ViewBinding for views, etc.).
- No dead code, unused imports, or redundant logic.

### Code Smells

- **Long methods**: methods doing too many things; should be broken into smaller, focused functions.
- **Large classes**: classes with too many responsibilities; consider splitting by concern.
- **Duplicated code**: repeated logic that should be extracted into a shared function.
- **Deep nesting**: excessive `if`/`when`/`try` nesting that harms readability; consider early returns or extraction.
- **Magic numbers/strings**: unexplained literal values that should be named constants.
- **Feature envy**: a method that uses another class's data more than its own.
- **Inappropriate intimacy**: classes that depend too heavily on each other's internals.
- **Primitive obsession**: overuse of primitives where a domain type (enum, data class) would be clearer.
- **Long parameter lists**: functions with many parameters; consider grouping into a data class or builder.
- **Mutable shared state**: prefer immutable data and local state; flag unnecessary `var` or global mutable collections.

### Code Refactoring Suggestions

- **Extract method**: identify blocks of code within a method that perform a distinct task and can be extracted.
- **Extract class/interface**: when a class handles multiple responsibilities, suggest splitting into focused classes or introducing an interface.
- **Replace conditional with polymorphism**: complex `when`/`if-else` chains that switch on type can often be replaced with polymorphic dispatch.
- **Introduce sealed class/enum**: when a set of related constants or states is represented loosely (strings, ints), suggest modeling with a sealed class or enum.
- **Use Kotlin idioms**: replace Java-style patterns with idiomatic Kotlin — e.g., `let`/`apply`/`also` scope functions, destructuring, extension functions, `buildList`/`buildString`.
- **Simplify lifecycle management**: leverage `lifecycleScope`, `repeatOnLifecycle`, or `LifecycleObserver` to reduce manual lifecycle bookkeeping.
- **Reduce coupling**: identify tight coupling between components and suggest dependency injection, event-driven patterns (EventBus is already used), or interface abstractions.
- **Consolidate duplicate logic**: when similar logic appears in virtual and physical key handlers (`keys/virtual/` vs `keys/physical/`), suggest a shared base or utility.
- **Improve testability**: flag code that is hard to unit test (e.g., direct static calls, hidden dependencies) and suggest restructuring for easier testing.

### JNI / Native Security

This project bridges Kotlin and native C/C++ via JNI. Pay special attention to:

- **Buffer overflows**: check that JNI string and array operations use correct lengths.
- **Null pointer dereference**: validate JNI references before use (e.g., `FindClass`, `GetMethodID` return values).
- **JNI reference leaks**: ensure local references are released when no longer needed, especially in loops.
- **Input validation**: verify that data crossing the JNI boundary is validated on both sides.
- **Memory management**: check for proper allocation/deallocation of native memory.
- Review `app/src/main/cpp/libchewing_android_jni.cpp` as the primary JNI surface.

### Android-Specific Security

- **Input method security**: as an IME, this app handles all user keystrokes. Ensure no logging or leaking of user input.
- **SharedPreferences**: verify no sensitive data is stored in plain text.
- **Intent handling**: check for intent injection or unvalidated intent extras.
- **WebView** (if applicable): check for JavaScript injection, insecure `addJavascriptInterface` usage.
- **Export controls**: verify that components (activities, services, receivers) are not inadvertently exported.
- **ProGuard/R8**: ensure obfuscation rules don't strip security-critical code.

### General Security

- No hardcoded secrets, API keys, or credentials.
- No command injection via `Runtime.exec()` or `ProcessBuilder`.
- No path traversal vulnerabilities in file operations.
- No insecure random number generation for security-sensitive operations.
- Dependencies: check for known vulnerabilities in third-party libraries.

## Output Format

Report findings using this structure:

### Critical / High

Issues that must be fixed — security vulnerabilities, crashes, data loss risks.

### Medium

Issues that should be fixed — logic bugs, thread safety concerns, code smell.

### Low / Informational

Suggestions for improvement — style, readability, minor optimizations.

For each finding, include:
- **File and line number** (e.g., `Chewing.kt:42`)
- **Description** of the issue
- **Impact** — what could go wrong
- **Recommendation** — how to fix it, with code if appropriate

## How to Run

When invoked without arguments, review recently changed files:
```bash
git diff --name-only HEAD~5
```

When invoked with a specific scope (file, directory, or component name), focus the review on that area.

For a full audit, systematically review:
1. JNI layer (`app/src/main/cpp/`)
2. Service layer (`GuilelessBopomofoService.kt`)
3. Data handling (`Chewing.kt`, `ChewingBridge.kt`)
4. UI layer (key handlers, `KeyboardPanel.kt`)
5. Configuration (`AndroidManifest.xml`, ProGuard rules)
6. Dependencies (`gradle/libs.versions.toml`)

## Task: $ARGUMENTS
