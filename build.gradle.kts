plugins {
    id("com.android.application") version libs.versions.gradle apply false
    // AGP's built-in Kotlin compiles with whichever Kotlin Gradle plugin is on the build
    // classpath, which is otherwise the older one AGP depends on. Keep it in step with the
    // Kotlin libraries, or the compiler can not read their metadata.
    id("org.jetbrains.kotlin.android") version libs.versions.kotlinVersion apply false
}
