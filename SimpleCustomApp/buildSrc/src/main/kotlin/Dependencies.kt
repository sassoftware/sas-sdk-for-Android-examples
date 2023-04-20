import org.gradle.api.JavaVersion

object Versions {
    object android {
        object sdk {
            const val compile = 33
            const val min = 24
            const val target = 33
        }
    }

    val java = JavaVersion.VERSION_11

    object kotlin {
        const val lang = "1.8.10"
    }
}

object Libs {
    object android {
        object plugin {
            const val version = "7.4.1"
            const val plugin = "com.android.tools.build:gradle:$version"
        }
    }

    object androidx {
        const val appcompat = "androidx.appcompat:appcompat:1.6.1"
        const val constraintlayout = "androidx.constraintlayout:constraintlayout:2.1.4"
    }
}
