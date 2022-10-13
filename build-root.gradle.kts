buildscript {
    // Required maven directory where SAS VA project artifacts are published
    extra["cfgSasVaMavenRepo"] = System.getenv("MOBILEBI_MAVEN_REPO_URL")

    if (extra["cfgSasVaMavenRepo"] == null) {
        throw InvalidUserDataException("MOBILEBI_MAVEN_REPO_URL must be defined")
    }

    // Optional maven URL where external dependencies are published; unset to pull dependencies
    // from internet
    extra["cfgMavenRepoUrlExternal"] = System.getenv("EXTERNAL_MAVEN_REPO_URL")

    repositories {
        val cfgMavenRepoUrlExternal: String? by project
        cfgMavenRepoUrlExternal?.let {
            maven(url = it)
        } ?: run {
            google()
            mavenCentral()
            gradlePluginPortal()
        }
    }

    dependencies {
        classpath(Libs.android.plugin.plugin)
        classpath(kotlin("gradle-plugin", version = Versions.kotlin.lang))
    }
}

allprojects {
    repositories {
        val cfgSasVaMavenRepo: String? by project
        cfgSasVaMavenRepo?.let {
            maven(url = it)
        }

        val cfgMavenRepoUrlExternal: String? by project
        cfgMavenRepoUrlExternal?.let {
            maven(url = it)
        } ?: run {
            google()
            mavenCentral()
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
