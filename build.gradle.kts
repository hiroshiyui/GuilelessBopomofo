// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    extra.apply {
        set("kotlin_version", "1.9.24")
        set("androidx_appcompat_version", "1.6.1")
        set("androidx_emoji2_version", "1.4.0")
        set("lifecycle_version", "2.8.0")
    }

    repositories {
        google()
        mavenCentral()
    }

    val kotlinVersion = extra.get("kotlin_version") as String

    dependencies {
        classpath("com.android.tools.build:gradle:8.4.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory.get())
}