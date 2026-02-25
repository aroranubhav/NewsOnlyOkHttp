import java.util.Base64
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.maxi.newsonlyokhttp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.maxi.newsonlyokhttp"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val properties = Properties()
        .apply {
            rootProject.file("local.properties")
                .takeIf {
                    it.exists()
                }?.reader()
                .use {
                    load(it)
                }
        }

    val apiKey = properties["API_KEY"] ?: error("API Key not found!")
    val encodedApiKey = Base64.getEncoder().encodeToString(apiKey.toString().toByteArray())
    val userAgent = properties["USER_AGENT"] ?: error("Missing User Agent")

    buildTypes {
        all {
            buildConfigField("String", "API_KEY", "\"$encodedApiKey\"")
            buildConfigField("String", "USER_AGENT", "\"$userAgent\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundaion)
    implementation(libs.androidx.compose.material3)

    //lifecycle
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime)

    //compose navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    //hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    //retrofit
    implementation(libs.retrofit)

    //kotlinx-serialization
    implementation(libs.koltinx.serialization.json)
    implementation(libs.kotlinx.serialization.converter)

    //room
    implementation(libs.room.ktx)
    implementation(libs.room)
    implementation(libs.room.paging)
    ksp(libs.room.compiler)

    //interceptor
    implementation(libs.logging.interceptor)

    //browser
    implementation(libs.browser)

    //swipe refresh layout
    implementation(libs.androidx.swipe.refresh)

    //glide
    implementation(libs.glide)

    //worker + hilt-work
    implementation(libs.work.runtime)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    //datastore
    implementation(libs.data.store)

    //paging
    implementation(libs.androidx.paging)

    //app-startup
    implementation(libs.app.startup)

    //testing
    testImplementation(libs.junit) //Provides the core unit testing framework for running and structuring tests

    testImplementation(libs.mockk) //Mocks classes, functions, and coroutines to isolate the unit under test.

    testImplementation(libs.kotlinx.coroutines.test) //Allows controlled testing of suspend functions and coroutine behavior.

    testImplementation(libs.turbine) //Tests Kotlin Flow emissions in a clean, structured, and deterministic way.

    testImplementation(libs.square.mockwebserver) //Simulates HTTP server responses to test Retrofit/OkHttp networking code.

    testImplementation(libs.google.truth) //Provides human-readable, fluent assertions for clearer test expectations.

    androidTestImplementation(libs.androidx.junit) //Runs instrumentation tests and automates UI interactions on Android.
    androidTestImplementation(libs.androidx.espresso.core)

    androidTestImplementation(libs.androidx.compose.bom)
    androidTestImplementation(libs.hilt.android.testing) //Injects test-specific dependencies in Android tests using Hilt.
    testImplementation(kotlin("test"))

    testImplementation(libs.androidx.test.core) //gives a valid Context instance in JVM tests.
    testImplementation(libs.robolectric) //run Android-dependent code on the JVM without needing an emulator or a physical device
}