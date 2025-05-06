plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "nl.iprosoft.autotawafcounter"
    compileSdk = 35

    defaultConfig {
        applicationId = "nl.iprosoft.autotawafcounter"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
    }
}



dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.activity)
    // Mapbox Core
    implementation("com.mapbox.maps:android:11.11.0")
    // Mapbox Annotation Plugin
    implementation("com.mapbox.plugin:maps-animation:11.11.0")
    implementation("com.mapbox.extension:maps-androidauto:11.11.0")
    implementation(libs.animation.graphics.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}