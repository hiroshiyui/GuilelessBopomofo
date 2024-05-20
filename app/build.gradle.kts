val kotlinVersion = rootProject.extra.get("kotlin_version") as String
val androidxAppcompatVersion = rootProject.extra.get("androidx_appcompat_version") as String
val androidxEmoji2Version = rootProject.extra.get("androidx_emoji2_version") as String
val lifecycleVersion = rootProject.extra.get("lifecycle_version") as String

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 34
    buildToolsVersion = "34.0.0"
    namespace = "org.ghostsinthelab.apps.guilelessbopomofo"

    defaultConfig {
        applicationId = "org.ghostsinthelab.apps.guilelessbopomofo"
        minSdk = 23
        targetSdk = 34
        versionCode = 118
        versionName = "1.9.48"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                cppFlags += listOf("")
            }
        }
        vectorDrawables {
            useSupportLibrary = true
        }

        setProperty("archivesBaseName", "${applicationId}_v${versionName}")
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    externalNativeBuild {
        cmake {
        }
    }

    ndkVersion = "23.2.8568313"

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

//    val chewingLibraryPath: String = "${rootDir}/libchewing-android-module/src/main/cpp/libs/libchewing"
//
//    tasks.register<Exec>("prepareChewing") {
//        workingDir(chewingLibraryPath)
//        commandLine("cmake", "--preset", "c99-release", "-DBUILD_SHARED_LIBS=OFF", ".")
//    }
//
//    val chewingDataFiles =
//        listOf<String>("dictionary.dat", "index_tree.dat", "pinyin.tab", "swkb.dat", "symbols.dat")
//
//    tasks.register<Exec>("buildChewingData") {
//        dependsOn("prepareChewing")
//        workingDir("$chewingLibraryPath/build")
//        commandLine("make", "data", "all_static_data")
//    }
//
//    tasks.register<Copy>("copyChewingDataFiles") {
//        dependsOn("buildChewingData")
//        for (chewingDataFile in chewingDataFiles) {
//            from("$chewingLibraryPath/build/data/$chewingDataFile")
//            into("$rootDir/app/src/main/assets")
//        }
//    }
//
//    tasks.preBuild {
//        dependsOn("copyChewingDataFiles")
//    }
//
//    tasks.register<Delete>("cleanChewingDataFiles") {
//        for (chewingDataFile in chewingDataFiles) {
//            file("$rootDir/app/src/main/assets/$chewingDataFile").delete()
//        }
//    }
//
//    tasks.register<Exec>("execMakeClean") {
//        onlyIf { file("$chewingLibraryPath/build/Makefile").exists() }
//        workingDir("$chewingLibraryPath/build")
//        commandLine("make", "clean")
//        isIgnoreExitValue = true
//    }
//
//    tasks.clean {
//        dependsOn("cleanChewingDataFiles", "execMakeClean")
//    }
}
dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    implementation("androidx.appcompat:appcompat:$androidxAppcompatVersion")
    implementation("androidx.appcompat:appcompat-resources:$androidxAppcompatVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.github.google:flexbox-layout:2.0.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("androidx.emoji2:emoji2:$androidxEmoji2Version")
    implementation("androidx.emoji2:emoji2-views:$androidxEmoji2Version")
    implementation("androidx.emoji2:emoji2-views-helper:$androidxEmoji2Version")
    implementation("androidx.emoji2:emoji2-bundled:$androidxEmoji2Version")
    implementation("org.greenrobot:eventbus:3.3.1")
    implementation(project(":libchewing-android-module"))
    testImplementation("junit:junit:4.13.2")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-accessibility:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-web:3.5.1")
    androidTestImplementation("androidx.test.espresso.idling:idling-concurrent:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-idling-resource:3.5.1")
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
}

