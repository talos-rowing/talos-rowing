plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "org.nargila.robostroke.android.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "org.nargila.robostroke.android.app"
        minSdk = 30
        targetSdk = 34
        versionCode = 332
        versionName = "0.9.7.332"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    flavorDimensions += "version"
    productFlavors {
        create("v2") {
            dimension = "version"
            applicationIdSuffix = ".v2"
            versionNameSuffix = "-v2"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core:1.13.1")
    implementation("de.mindpipe.android:android-logging-log4j:1.0.3")
    implementation("org.slf4j:slf4j-log4j12:1.7.21")
    implementation(project(":common"))
    implementation(project(":android-common"))
    testImplementation(libs.junit)
}
