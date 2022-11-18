import org.gradle.api.JavaVersion

object Versions {
    object android {
        object sdk {
            const val compile = 33
            const val min = 24
            const val target = 33
        }
    }

    val java = JavaVersion.VERSION_1_8

    object kotlin {
        // These are not identical, but should be kept in lockstep; see
        // https://kotlinlang.org/docs/releases.html#release-details
        const val lang = "1.7.10"
        const val coroutines = "1.6.4"
    }
}

object Libs {
    object android {
        object plugin {
            const val version = "7.3.0"
            const val plugin = "com.android.tools.build:gradle:$version"
        }
    }

    object androidx {
        const val appcompat = "androidx.appcompat:appcompat:1.5.1"
        const val constraintlayout = "androidx.constraintlayout:constraintlayout:2.1.4"
    }
}
