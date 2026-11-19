plugins {
    id("com.android.application")
    id("kotlin-parcelize")
    alias(libs.plugins.compose.compiler)
}

// The libchewing commit this build ships, which pins its dictionary data too. Installs compare
// it with what they last copied, so new dictionaries reach them without a version bump.
val chewingRevision: String = providers.exec {
    isIgnoreExitValue = true
    commandLine("git", "-C", "${rootDir}/app/src/main/cpp/libs/libchewing", "rev-parse", "HEAD")
}.standardOutput.asText.map { it.trim() }.getOrElse("").ifEmpty { "unknown" }

android {
    compileSdk = 37
    buildToolsVersion = "37.0.0"
    namespace = "org.ghostsinthelab.apps.guilelessbopomofo"

    androidResources {
        generateLocaleConfig = true
    }

    defaultConfig {
        applicationId = "org.ghostsinthelab.apps.guilelessbopomofo"
        minSdk = 23
        targetSdk = 37
        versionCode = 225
        versionName = "5.2.1"

        buildConfigField("String", "CHEWING_REVISION", "\"$chewingRevision\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                // Suppress warnings from libchewing (third-party submodule)
                cFlags("-Wno-unused-function", "-Wno-unused-but-set-variable")
                cppFlags += listOf("")
            }
        }
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    buildTypes {
        release {
            // Enables code-related app optimization.
            isMinifyEnabled = true

            // Enables resource shrinking.
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
        debug {
            isMinifyEnabled = false
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.24.0+"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    compileOptions {
        targetCompatibility = JavaVersion.VERSION_17
        sourceCompatibility = JavaVersion.VERSION_17
    }

    ndkVersion = "29.0.14206865"
    compileSdkMinor = 0
}

base {
    archivesName = "${android.defaultConfig.applicationId}_v${android.defaultConfig.versionName}"
}

// The APK task owns its whole output directory, so Gradle clears out older APKs, but the task
// writing the final App Bundle (sign<Variant>Bundle) owns only its one .aab. With the version
// in the file name, bundles of earlier versions would pile up, so remove them before a new one.
tasks.matching { it.name.matches(Regex("sign\\w+Bundle")) }.configureEach {
    val variant = name.removePrefix("sign").removeSuffix("Bundle").replaceFirstChar { it.lowercase() }
    val bundleDir = layout.buildDirectory.dir("outputs/bundle/$variant")
    doFirst {
        bundleDir.get().asFile.listFiles { file -> file.extension == "aab" }?.forEach { it.delete() }
    }
}

val chewingLibraryPath: String = "${rootDir}/app/src/main/cpp/libs/libchewing"
val chewingDataFiles = listOf(
    "static_dict.bin", "rare_dict.bin", "static_words.bin", "static_lm.bin", "swkb.dat", "symbols.dat"
)
// What libchewing 0.13 and earlier read. Git ignores them, so a checkout that built those still
// has them in the assets, and they would go into every APK unread.
val obsoleteChewingDataFiles = listOf("tsi.dat", "word.dat")
val chewingAssetsPath: String = "${rootDir}/app/src/main/assets"

val rustupAvailable = providers.exec {
    isIgnoreExitValue = true
    commandLine("rustup", "-V")
}.result.map { it.exitValue == 0 }.getOrElse(false)

tasks.register<Exec>("prepareChewing") {
    workingDir(chewingLibraryPath)
    // This is just for task 'buildChewingData', other definitions are in cpp/CMakeLists.txt
    commandLine(
        "cmake", "-B", "build/", "-DBUILD_INFO=false", "-DBUILD_TESTING=false", "-DWITH_SQLITE3=false", "-DCMAKE_BUILD_TYPE=Release"
    )
}

tasks.register<Exec>("buildChewingData") {
    dependsOn("prepareChewing")
    workingDir("$chewingLibraryPath/build")
    commandLine("make", "dict_chewing_v4", "misc")
}

tasks.register<Delete>("deleteObsoleteChewingDataFiles") {
    delete(obsoleteChewingDataFiles.map { "$chewingAssetsPath/$it" })
}

tasks.register<Copy>("copyChewingDataFiles") {
    dependsOn("buildChewingData", "deleteObsoleteChewingDataFiles")
    from("$chewingLibraryPath/build/data/dict/chewing_v4") {
        include("static_dict.bin")
        include("rare_dict.bin")
        include("static_words.bin")
        include("static_lm.bin")
    }
    from("$chewingLibraryPath/build/data/misc") {
        include("swkb.dat")
        include("symbols.dat")
    }
    from("$chewingLibraryPath/data/dict/chewing") {
        include("word.csv")
    }
    into(chewingAssetsPath)
}

tasks.register<Exec>("installRustup") {
    onlyIf { !rustupAvailable }
    commandLine(
        "curl", "--proto", "'=https'", "--tlsv1.2", "-sSf", "https://sh.rustup.rs", "|", "sh", "-s", "--", "--default-toolchain", "none"
    )
}

tasks.register<Exec>("installSpecifiedRustToolchain") {
    dependsOn("installRustup")
    onlyIf { !rustupAvailable }
    // follows rust-toolchain.toml
    commandLine("rustup", "install")
}

tasks.preBuild {
    dependsOn(
        "installSpecifiedRustToolchain", "copyChewingDataFiles"
    )
}

tasks.register<Delete>("cleanChewingDataFiles") {
    delete((chewingDataFiles + obsoleteChewingDataFiles).map { "$chewingAssetsPath/$it" })
}

tasks.register<Exec>("execMakeClean") {
    onlyIf { file("$chewingLibraryPath/build/Makefile").exists() }
    workingDir("$chewingLibraryPath/build")
    commandLine("make", "clean")
    isIgnoreExitValue = true
}

tasks.register<Delete>("deleteChewingBuildDirectory") {
    onlyIf { file("$chewingLibraryPath/build/Makefile").exists() }
    delete("$chewingLibraryPath/build")
}

tasks.register<Delete>("deleteAppDotCxxDirectory") {
    delete("$rootDir/app/.cxx")
}

tasks.clean {
    dependsOn(
        "cleanChewingDataFiles", "execMakeClean", "deleteChewingBuildDirectory", "deleteAppDotCxxDirectory"
    )
}

tasks.register("bumpPatchVersion") {
    description = "Increment versionCode and versionName patch level"
    doLast {
        val buildFile = file("build.gradle.kts")
        var content = buildFile.readText()

        val versionCodeRegex = Regex("""versionCode\s*=\s*(\d+)""")
        val versionNameRegex = Regex("""versionName\s*=\s*"(\d+)\.(\d+)\.(\d+)"""")

        val codeMatch = versionCodeRegex.find(content)
            ?: throw GradleException("versionCode not found")
        val nameMatch = versionNameRegex.find(content)
            ?: throw GradleException("versionName not found")

        val oldCode = codeMatch.groupValues[1].toInt()
        val major = nameMatch.groupValues[1]
        val minor = nameMatch.groupValues[2]
        val oldPatch = nameMatch.groupValues[3].toInt()

        val newCode = oldCode + 1
        val newPatch = oldPatch + 1
        val newVersionName = "$major.$minor.$newPatch"

        content = content
            .replace(codeMatch.value, "versionCode = $newCode")
            .replace(nameMatch.value, """versionName = "$newVersionName"""")

        buildFile.writeText(content)
        println("Version bumped: $oldCode -> $newCode, ${nameMatch.groupValues[1]}.${nameMatch.groupValues[2]}.$oldPatch -> $newVersionName")
    }
}

dependencies {
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.core)
    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.uiautomator)
    androidTestImplementation(libs.ext.junit)
    debugImplementation(libs.compose.ui.test.manifest)
    debugImplementation(libs.leakcanary.android)
    implementation(platform(libs.compose.bom))
    implementation(libs.activity.compose)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
}
