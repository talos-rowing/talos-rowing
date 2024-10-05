plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "org.nargila.robostroke.android.remote"
    compileSdk = 34

    defaultConfig {
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
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    implementation("de.mindpipe.android:android-logging-log4j:1.0.3")
    implementation("org.slf4j:slf4j-log4j12:1.7.21")
    implementation("ch.acra:acra:4.4.0")
    implementation(project(":common"))
    implementation(project(":android-common"))
    testImplementation(libs.junit)
}
