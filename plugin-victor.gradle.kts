plugins {
    id(Libs.victor.artifact) version Libs.victor.version
}

val Any.extensions get() = (this as ExtensionAware).extensions

android {
    sourceSets {
        named("main") {
            val svgSourceSet = extensions["svg"] as SourceDirectorySet
            svgSourceSet.srcDir("src/main/svg")
        }
    }
}

victor {
    // Do not generate these densities for SVG assets
    excludeDensities = listOf("ldpi")
}
