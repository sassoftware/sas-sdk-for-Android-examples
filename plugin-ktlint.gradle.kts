// kotlin-gradle-plugin must be applied for configuration below to work
// (see https://kotlinlang.org/docs/reference/using-gradle.html)

val ktlint by configurations.creating

dependencies {
    ktlint(Libs.ktlint) {
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        }
    }
    // ktlint(project(":custom-ktlint-ruleset")) // in case of custom ruleset
}

val disabledRules = listOf(
    // Compose functions are heavily annotated, and sometimes it makes sense to have them on a
    // single line
    "annotation"
)

val editorconfig = File("""#thisfile""")
    .parentFile
    .resolve("plugin-ktlint.editorconfig")
    .absolutePath

val commonArgs = listOf(
    "--android",
    "--reporter=plain",
    "--editorconfig=$editorconfig",
    "--experimental",

    // Hack: leading comma necessary, else some subset of rules list is ignored
    "--disabled_rules=,${disabledRules.joinToString(",")}",

    "src/**/*.{kt,kts}"
)

val ktlintCheck by tasks.creating(JavaExec::class) {
    description = "Check Kotlin code style"
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")

    // Use checkstyle prefix and reporting style FBO Jenkins checkstyle plugin
    val ktlintReport = "${project.buildDir}/reports/checkstyle-ktlint.xml"

    args = commonArgs + "--reporter=checkstyle,output=${ktlintReport}"
}

val ktlintFix by tasks.creating(JavaExec::class) {
    description = "Fix (some) Kotlin code style deviations"
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    args = commonArgs + "-F"
}
