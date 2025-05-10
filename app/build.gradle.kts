plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 35
    buildToolsVersion = "35.0.0"
    namespace = "org.ghostsinthelab.apps.guilelessbopomofo"

    defaultConfig {
        applicationId = "org.ghostsinthelab.apps.guilelessbopomofo"
        minSdk = 23
        targetSdk = 35
        versionCode = 130
        versionName = "2.2.0"

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
        buildConfig = true
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
    debugImplementation(libs.leakcanary.android)
    androidTestImplementation(libs.androidx.core)
    androidTestImplementation(libs.androidx.espresso.idling.resource)
    androidTestImplementation(libs.androidx.espresso.web)
    androidTestImplementation(libs.androidx.idling.concurrent)
    androidTestImplementation(libs.androidx.uiautomator)
    androidTestImplementation(libs.espresso.accessibility)
    androidTestImplementation(libs.espresso.contrib)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.espresso.intents)
    androidTestImplementation(libs.ext.junit)
}