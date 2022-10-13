import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions
import org.gradle.api.InvalidUserDataException

class BuildFiles(private val from: File, private val to: File) {
    /*
     * Properties/init
     */

    var debug = false
    private val incDirs = mutableListOf<File>()
    private val updateFiles = mutableListOf<File>()
    private val seen = mutableSetOf<String>()
    private val lines = mutableListOf<String>()
    private var needsUpdate = false
    private var needsUpdateReason: String? = null

    val incRegex = """^(\s*)#include\s+(.+)""".toRegex()

    /*
     * BuildFiles methods
     */

    // Add a directory to search for #included scripts. The directory of the including file is
    // searched by default. If [incDir] is relative, it is relative to [from].
    fun addIncDir(incDir: String) {
        incDirs += from.resolveSibling(incDir)
    }

    // Add a file to compare against when determining whether a generated script is out-of-date. If
    // the given file is newer than the generated script, it will be regenerated.
    fun addUpdateFile(file: File) {
        updateFiles += file
    }

    /**
     * Update [to] from the template [from], if the former doesn't exist or is out-of-date
     * relative to the latter.
     *
     * @param force
     *     if true, update regardless
     */
    fun update(force: Boolean = false) {
        require(from.exists()) {
            "no such file: $from"
        }

        // This method should only be called once
        check(seen.isEmpty())

        seen.clear()
        lines.clear()

        // Initial value
        needsUpdate = force.also {
            if (it) {
                needsUpdateReason = "forced"
            }
        } || !to.exists().also {
            if (!it) {
                needsUpdateReason = "doesn't exist"
            }
        } || updateFiles.any { updateFile ->
            (updateFile.lastModified() > to.lastModified()).also {
                if (it) {
                    needsUpdateReason = "$updateFile is newer"
                }
            }
        }

        read(listOf(), from, "")

        if (needsUpdate) {
            debug("Updating: $to")
            debug("  Reason: $needsUpdateReason")
            write(to)
        } else {
            debug("Already up-to-date: $to")
        }
    }

    /*
     * Private methods
     */

    private fun debug(message: String) {
        if (debug) {
            System.err.println("${this::class.simpleName}: $message")
        }
    }

    /**
     * Finds the given file if it exists; returns null otherwise.
     */
    private fun findIncFile(incFileName: String, relativeToFile: File): File? {
        // If file name is absolute…
        File(incFileName).takeIf { it.isAbsolute() }?.let { incFile ->
            return incFile.takeIf { it.exists() }
        }

        // …else search known dirs
        for (incDir in
            listOf(
                // Directory of including script
                relativeToFile.parentFile,

                // Directory of including script, after resolving symlinks
                relativeToFile.canonicalFile.parentFile
            ) +

            // Pre-defined script directories
            incDirs
        ) {
            val incFile = File(incDir, incFileName)
            if (incFile.exists()) {
                return incFile
            }
        }
        return null
    }

    private fun read(incStack: List<String>, templateFile: File, indent: String) {
        val templatePath = templateFile.canonicalPath

        // Check for include cycle
        if (incStack.any { it == templatePath }) {
            throw InvalidUserDataException(
                "#include cycle detected:\n  " + (incStack + templatePath).joinToString(" ->\n  ")
            )
        }

//      if (!seen.add(templatePath)) {
//          // templateFile has already been seen; skip
//          debug("$templateFile already seen; skipping…")
//          return
//      }

        if (!needsUpdate && templateFile.lastModified() > to.lastModified()) {
            needsUpdate = true
            needsUpdateReason = "$templateFile is newer"
        }

        templateFile.useLines {
            for (line in it) {
                // #include <incFileName>
                incRegex.find(line)?.destructured?.let { (incIndent, incFileName) ->
                    findIncFile(incFileName, templateFile)?.let { incFile ->
                        val newIndent = indent + incIndent
                        lines += "$newIndent// BEGIN: $incFileName"
                        read(incStack + templatePath, incFile, newIndent)
                        lines += "$newIndent// END: $incFileName"
                    } ?: throw InvalidUserDataException("included file not found: $incFileName")
                } ?: run {
                    // Normal line
                    lines += if (line.isEmpty()) "" else
                        indent + line.replaceFirst("#thisfile", templateFile.absolutePath)
                }
            }
        }
    }

    private fun write(out: File) {
        // Delete beforehand in case file exists and is unwritable
        out.delete()

        out.printWriter().use { pout ->
            // Destructured groups: identifier
            val importRegex = """^\s*import\s+(\S+)""".toRegex()

            // Imported identifiers
            val imports = linkedSetOf<String>()

            val pluginsRegex = """^plugins\s*\{""".toRegex()

            // Lines within plugin {} blocks
            val pluginLines = linkedSetOf<String>()

            // All other lines
            val other = mutableListOf<String>()

            val iterator = lines.iterator()
            while (iterator.hasNext()) {
                val line = iterator.next()

                importRegex.find(line)?.destructured?.let { (identifier) ->
                    imports += identifier
                } ?:

                pluginsRegex.find(line)?.let {
                    while (iterator.hasNext()) {
                        val pluginLine = iterator.next()
                        // Crudely detect end of plugins block
                        if (pluginLine.startsWith("}")) {
                            break
                        }
                        pluginLines += pluginLine
                    }
                } ?:

                run {
                    other += line
                }
            }

            (
                listOf(
                    "//",
                    "// Generated from ${from.toRelativeString(to.parentFile)}. Do not edit.",
                    "//",
                    ""
                ) +

                imports.map {
                    "import $it"
                } +

                pluginLines.let { list ->
                    if (list.isEmpty()) {
                        list
                    } else {
                        listOf("plugins {") + list + "}"
                    }
                } +

                other
            ).forEach {
                pout.println(it)
            }
        }

        // Attempt to set file as read-only to discourage editing
        try {
            Files.setPosixFilePermissions(
                out.toPath(), PosixFilePermissions.fromString("r--r--r--")
            )
        } catch (_: UnsupportedOperationException) {
            // ignore
        }
    }
}

// Copied from buildSrc/…/Exts.kt
fun ExtraPropertiesExtension.getBooleanExt(name: String, defaultVal: Boolean) =
    try {
        when (val propVal = this[name]) {
            is Boolean -> propVal
            null -> defaultVal
            else -> propVal.toString().toBoolean()
        }
    } catch (e: ExtraPropertiesExtension.UnknownPropertyException) {
        defaultVal
    }

// gradle -PbuildFilesForceUpdate=true to force update of build files
val buildFilesForceUpdate = extra.getBooleanExt("buildFilesForceUpdate", false)

// gradle -PbuildFilesDebug=true to get details on stderr
val buildFilesDebug = extra.getBooleanExt("buildFilesDebug", false)

val thisScript = buildscript.sourceFile!!.canonicalFile

// Set and generate/update the buildFile of each root/child ProjectDescriptor
(rootProject.children + rootProject).forEach { project ->
    // Treat each build file as a template
    val templateFile = project.buildFile

    // Put the generated build file in build/ (this changes the value of project.buildFile)
    project.buildFileName = "build/${project.buildFileName}"

    // Ensure build dir exists
    project.buildFile.parentFile.mkdirs()

    // Generate buildFile from template in project dir
    BuildFiles(templateFile, project.buildFile).apply {
        // Allow "#include <file>" to find <file> in this script's directory
        addIncDir(thisScript.parent)

        // Regenerate all build files if this script is updated
        addUpdateFile(thisScript)

        debug = buildFilesDebug

        update(buildFilesForceUpdate)
    }
}
