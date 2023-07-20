plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

android {
    namespace = "com.example.customapp"
    compileSdk = Versions.android.sdk.compile

    defaultConfig {
        minSdk = Versions.android.sdk.min
        targetSdk = Versions.android.sdk.target

        val appVersionMajor = 1
        val appVersionMinor = 2
        val appVersionMicro = 0

        // appVersion{Minor,Micro} each get two digits
        versionCode = 10000 * appVersionMajor + 100 * appVersionMinor + 1 * appVersionMicro
        versionName = "$appVersionMajor.$appVersionMinor.$appVersionMicro"
    }

    val cfgJavaVersion = Versions.java

    compileOptions {
        sourceCompatibility(cfgJavaVersion)
        targetCompatibility(cfgJavaVersion)
    }

    kotlinOptions {
        jvmTarget = cfgJavaVersion.toString()
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    implementation(Libs.androidx.appcompat)
    implementation(Libs.androidx.constraintlayout)
    implementation(Libs.androidx.recyclerview)
    implementation(Libs.google.androidMaterial)
    implementation(Sas.sdk.toolkit)
}
