plugins {
    id("com.android.application")
    id("kotlin-parcelize")
}

android {
    compileSdk = 36
    buildToolsVersion = "36.0.0"
    namespace = "org.ghostsinthelab.apps.guilelessbopomofo"

    androidResources {
        generateLocaleConfig = true
    }

    defaultConfig {
        applicationId = "org.ghostsinthelab.apps.guilelessbopomofo"
        minSdk = 23
        targetSdk = 36
        versionCode = 199
        versionName = "3.7.4"

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
        viewBinding = true
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

    ndkVersion = "28.1.13356709"
}

base {
    archivesName = "${android.defaultConfig.applicationId}_v${android.defaultConfig.versionName}"
}

val chewingLibraryPath: String = "${rootDir}/app/src/main/cpp/libs/libchewing"
val chewingDataFiles = listOf("tsi.dat", "word.dat", "swkb.dat", "symbols.dat")

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
    commandLine("make", "dict_chewing", "misc")
}

tasks.register<Copy>("copyChewingDataFiles") {
    dependsOn("buildChewingData")
    from("$chewingLibraryPath/build/data/dict/chewing") {
        include("tsi.dat")
        include("word.dat")
    }
    from("$chewingLibraryPath/build/data/misc") {
        include("swkb.dat")
        include("symbols.dat")
    }
    from("$chewingLibraryPath/data/dict/chewing") {
        include("word.csv")
    }
    into("$rootDir/app/src/main/assets")
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
    for (chewingDataFile in chewingDataFiles) {
        file("$rootDir/app/src/main/assets/$chewingDataFile").delete()
    }
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
    androidTestImplementation(libs.androidx.core)
    androidTestImplementation(libs.androidx.espresso.idling.resource)
    androidTestImplementation(libs.androidx.espresso.web)
    androidTestImplementation(libs.androidx.idling.concurrent)
    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.test.services)
    androidTestImplementation(libs.androidx.uiautomator)
    androidTestImplementation(libs.espresso.accessibility)
    androidTestImplementation(libs.espresso.contrib)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.espresso.intents)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.mockito.core)
    debugImplementation(libs.leakcanary.android)
    implementation(libs.appcompat)
    implementation(libs.appcompat.resources)
    implementation(libs.constraintlayout)
    implementation(libs.core.ktx)
    implementation(libs.eventbus)
    implementation(libs.flexbox.layout)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.material)
    implementation(libs.preference.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
}
