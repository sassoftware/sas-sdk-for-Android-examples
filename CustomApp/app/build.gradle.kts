#include android-app.gradle.kts
#include plugin-ktlint.gradle.kts

android {
    defaultConfig {
        val appVersionMajor = 1
        val appVersionMinor = 2
        val appVersionMicro = 0

        // appVersion{Minor,Micro} each get two digits
        versionCode = 10000 * appVersionMajor + 100 * appVersionMinor + 1 * appVersionMicro
        versionName = "$appVersionMajor.$appVersionMinor.$appVersionMicro"
    }
}

dependencies {
    implementation(Libs.androidx.appcompat)
    implementation(Libs.androidx.constraintlayout)
    implementation(Libs.androidx.recyclerview)
    implementation(Libs.google.androidMaterial)
    implementation(Libs.sas.sdk.toolkit)
}
