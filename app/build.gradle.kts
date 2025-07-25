plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.android")
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
        versionCode = 175
        versionName = "3.4.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                cFlags("-Wno-unused-function", "-Wno-unused-but-set-variable")
                cppFlags += listOf("")
            }
        }
        vectorDrawables {
            useSupportLibrary = true
        }

        setProperty("archivesBaseName", "${applicationId}_v${versionName}")
    }

    buildFeatures {
        buildConfig = true
        dataBinding = true
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
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

    kotlinOptions {
        jvmTarget = "17"
    }

    ndkVersion = "28.1.13356709"

    val chewingLibraryPath: String = "${rootDir}/app/src/main/cpp/libs/libchewing"

    tasks.register<Exec>("prepareChewing") {
        workingDir(chewingLibraryPath)
        // This is just for task 'buildChewingData', other definitions are in cpp/CMakeLists.txt
        commandLine(
            "cmake",
            "-B",
            "build/",
            "-DBUILD_INFO=false",
            "-DBUILD_TESTING=false",
            "-DWITH_SQLITE3=false",
            "-DCMAKE_BUILD_TYPE=Release"
        )
    }

    val chewingDataFiles =
        listOf<String>("tsi.dat", "word.dat", "swkb.dat", "symbols.dat")

    tasks.register<Exec>("buildChewingData") {
        dependsOn("prepareChewing")
        workingDir("$chewingLibraryPath/build")
        commandLine("make", "data", "all_static_data")
    }

    tasks.register<Copy>("copyChewingDataFiles") {
        dependsOn("buildChewingData")
        for (chewingDataFile in chewingDataFiles) {
            from("$chewingLibraryPath/build/data/$chewingDataFile")
            into("$rootDir/app/src/main/assets")
        }
    }

    tasks.register<Exec>("rustupTargetAddAarch64LinuxAndroid") {
        onlyIf {
            try {
                val result = exec {
                    isIgnoreExitValue = true
                    commandLine("rustup", "-V")
                }
                result.exitValue != 0
            } catch (e: Exception) {
                return@onlyIf false
            }
        }
        commandLine("rustup", "target", "add", "aarch64-linux-android")
    }

    tasks.register<Exec>("rustupTargetAddArmv7LinuxAndroideabi") {
        onlyIf {
            try {
                val result = exec {
                    isIgnoreExitValue = true
                    commandLine("rustup", "-V")
                }
                result.exitValue != 0
            } catch (e: Exception) {
                return@onlyIf false
            }
        }
        commandLine("rustup", "target", "add", "armv7-linux-androideabi")
    }

    tasks.register<Exec>("rustupTargetAddI686LinuxAndroid") {
        onlyIf {
            try {
                val result = exec {
                    isIgnoreExitValue = true
                    commandLine("rustup", "-V")
                }
                result.exitValue != 0
            } catch (e: Exception) {
                return@onlyIf false
            }
        }
        commandLine("rustup", "target", "add", "i686-linux-android")
    }

    tasks.register<Exec>("rustupTargetAddX64LinuxAndroid") {
        onlyIf {
            try {
                val result = exec {
                    isIgnoreExitValue = true
                    commandLine("rustup", "-V")
                }
                result.exitValue != 0
            } catch (e: Exception) {
                return@onlyIf false
            }
        }
        commandLine("rustup", "target", "add", "x86_64-linux-android")
    }

    tasks.preBuild {
        dependsOn(
            "copyChewingDataFiles",
            "rustupTargetAddAarch64LinuxAndroid",
            "rustupTargetAddArmv7LinuxAndroideabi",
            "rustupTargetAddI686LinuxAndroid",
            "rustupTargetAddX64LinuxAndroid"
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

    tasks.clean {
        dependsOn(
            "cleanChewingDataFiles",
            "execMakeClean",
            "deleteChewingBuildDirectory",
        )
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
    implementation(libs.emoji2)
    implementation(libs.emoji2.bundled)
    implementation(libs.emoji2.views)
    implementation(libs.emoji2.views.helper)
    implementation(libs.eventbus)
    implementation(libs.flexbox.layout)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.material)
    implementation(libs.preference.ktx)
    testImplementation(libs.junit)
}