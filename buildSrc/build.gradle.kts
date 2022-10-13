// Pull build dependencies exclusively from the offline maven repo when $EXTERNAL_MAVEN_REPO_URL is
// set
repositories {
    System.getenv("EXTERNAL_MAVEN_REPO_URL")?.let {
        maven(url = it)
    } ?: run {
        google()
        mavenCentral()
    }
}

plugins {
    `kotlin-dsl`
}
