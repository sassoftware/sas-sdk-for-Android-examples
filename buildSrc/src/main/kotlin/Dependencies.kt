import org.gradle.api.JavaVersion

object Versions {
    object android {
        object sdk {
            const val compile = 32
            const val min = 24
            const val target = 32
        }
    }

    val java = JavaVersion.VERSION_1_8

    object kotlin {
        // These are not identical, but should be kept in lockstep; see
        // https://kotlinlang.org/docs/releases.html#release-details
        const val lang = "1.7.10"
        const val coroutines = "1.6.3"
    }
}

object Libs {
    object android {
        object plugin {
            const val version = "7.2.1"
            const val plugin = "com.android.tools.build:gradle:$version"
        }
    }

    object androidx {
        const val appcompat = "androidx.appcompat:appcompat:1.4.2"
        const val cardview = "androidx.cardview:cardview:1.0.0"
        const val constraintlayout = "androidx.constraintlayout:constraintlayout:2.1.4"
        const val coreKtx = "androidx.core:core-ktx:1.8.0"

        object lifecycle {
            private const val version = "2.5.0"
            const val extensions = "androidx.lifecycle:lifecycle-extensions:2.2.0"
            const val livedata = "androidx.lifecycle:lifecycle-livedata:$version"
            const val livedataKtx = "androidx.lifecycle:lifecycle-livedata-ktx:$version"
            const val runtimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:$version"
            const val viewmodel = "androidx.lifecycle:lifecycle-viewmodel:$version"
            const val viewmodelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
        }

        object navigation {
            private const val version = "2.5.0"
            const val fragmentKtx = "androidx.navigation:navigation-fragment-ktx:$version"
        }

        const val preference = "androidx.preference:preference:1.2.0"
        const val recyclerview = "androidx.recyclerview:recyclerview:1.2.1"

        const val viewpager2 = "androidx.viewpager2:viewpager2:1.0.0"
    }

    object google {
        const val androidMaterial = "com.google.android.material:material:1.6.1"
        const val gson = "com.google.code.gson:gson:2.9.0"
        const val playServices = "com.google.android.gms:play-services-location:20.0.0"
    }

    object kotlin {
        object coroutines {
            const val core =
                "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlin.coroutines}"
            const val android =
                "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlin.coroutines}"
        }
    }

    const val ktlint = "com.pinterest:ktlint:0.47.1"

    object sas {
        object sdk {
            const val groupId = "com.sas.android.visualanalytics"
            const val version = "30.0.0"
            const val appbase = "$groupId:appbase:$version"
            const val toolkit = "$groupId:toolkit:$version"
        }
    }

    object victor {
        const val version = "1.1.2"
        const val artifact = "com.trello.victor"
    }
}
