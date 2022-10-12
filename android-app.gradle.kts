plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

android {
    compileSdkVersion(Versions.android.sdk.compile)

    defaultConfig {
        minSdk = Versions.android.sdk.min
        targetSdk = Versions.android.sdk.target
    }

    val cfgJavaVersion = Versions.java

    compileOptions {
        sourceCompatibility(cfgJavaVersion)
        targetCompatibility(cfgJavaVersion)
    }

    kotlinOptions {
        jvmTarget = cfgJavaVersion.toString()
    }

    signingConfigs {
        create("release") {
            System.getenv("KEYSTORE_FILE")?.let { storeFileName ->
                storeFile = file(storeFileName)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (System.getenv("KEYSTORE_FILE") != null) {
                signingConfig = signingConfigs["release"]
            }
        }
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}
