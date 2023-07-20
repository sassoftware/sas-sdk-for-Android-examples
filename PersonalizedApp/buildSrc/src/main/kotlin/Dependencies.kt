import org.gradle.api.JavaVersion

object Versions {
    object android {
        object sdk {
            const val compile = 33
            const val min = 24
            const val target = 33
        }
    }

    val java = JavaVersion.VERSION_17

    object kotlin {
        const val lang = "1.8.21"
    }
}

object Libs {
    object android {
        object plugin {
            const val version = "8.0.2"
            const val plugin = "com.android.tools.build:gradle:$version"
        }
    }
}
