buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }

    dependencies {
        classpath(Libs.android.plugin.plugin)
        classpath(kotlin("gradle-plugin", version = Versions.kotlin.lang))
    }
}

allprojects {
    repositories {
        maven(
            // Required maven directory where SAS VA project artifacts are published
            url = System.getenv("MOBILEBI_MAVEN_REPO_URL") ?:
                error("MOBILEBI_MAVEN_REPO_URL must be defined")
        )

        google()
        mavenCentral()
    }
}
