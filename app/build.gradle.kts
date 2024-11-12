val kotlinVersion = "2.0.0"
val androidxAppcompatVersion = "1.7.0"
val androidxEmoji2Version = "1.5.0"
val lifecycleVersion = "2.8.7"

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 35
    buildToolsVersion = "34.0.0"
    namespace = "org.ghostsinthelab.apps.guilelessbopomofo"

    defaultConfig {
        applicationId = "org.ghostsinthelab.apps.guilelessbopomofo"
        minSdk = 23
        targetSdk = 35
        versionCode = 125
        versionName = "2.1.0"

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
}
dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    implementation("androidx.appcompat:appcompat:$androidxAppcompatVersion")
    implementation("androidx.appcompat:appcompat-resources:$androidxAppcompatVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.core:core-ktx:1.15.0")
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
    testImplementation("junit:junit:4.13.2")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-accessibility:3.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-web:3.6.1")
    androidTestImplementation("androidx.test.espresso.idling:idling-concurrent:3.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-idling-resource:3.6.1")
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
}

