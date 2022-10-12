// Pull plugins exclusively from the offline maven repo when $EXTERNAL_MAVEN_REPO_URL is set
pluginManagement {
    repositories {
        System.getenv("EXTERNAL_MAVEN_REPO_URL")?.let {
            maven(url = it)
        } ?: run {
            gradlePluginPortal()
            google()
            mavenCentral()
        }
    }
}
