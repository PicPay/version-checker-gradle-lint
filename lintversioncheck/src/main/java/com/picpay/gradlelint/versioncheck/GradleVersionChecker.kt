package com.picpay.gradlelint.versioncheck

import com.android.tools.lint.detector.api.*
import java.io.File


@Suppress("UnstableApiUsage")
class GradleVersionChecker : Detector(), Detector.GradleScanner {

    override fun checkDslPropertyAssignment(
        context: GradleContext,
        property: String,
        value: String,
        parent: String,
        parentParent: String?,
        valueCookie: Any,
        statementCookie: Any
    ) {
        if (parent == DEPENDENCIES && isCustomDependencyDeclaration(value)) {

            val library = getLibraryFromDependency(context, value)

            VersionRemoteDataSource(context.client)
                .getNewVersionAvailable(library)
                ?.let { newLibrary ->
                    context.report(
                        REMOTE_VERSION,
                        context.getLocation(valueCookie),
                        "New version available: $newLibrary"
                    )
                }
        }
    }

    private fun getLibraryFromDependency(
        context: GradleContext,
        value: String
    ): Library {
        val currentProject = context.project.dir.absolutePath

        val root = File(currentProject).parentFile.absolutePath

        val buildSrc = "buildSrc/src/main/java/Dependencies.kt"

        val definition = mutableListOf<String>()

        File(root, buildSrc).readLines().forEach { line ->
            if (line.contains(value.split(".")[1])) {
                val cleared = line.split("=")[1].trim()
                val artifact = cleared.split("$").firstOrNull()
                if (artifact != null) {
                    definition.add(artifact.toString().replace("\"", ""))
                } else {
                    definition.add(cleared.replace("\"", ""))
                }
            }
        }
        return (definition[0] + definition[1]).toLibrary()
    }

    private fun isCustomDependencyDeclaration(value: String): Boolean {
        return value.startsWith("Dependencies.")
    }

    companion object {

        private const val DEPENDENCIES = "dependencies"

        private val IMPLEMENTATION = Implementation(
            GradleVersionChecker::class.java,
            Scope.GRADLE_SCOPE
        )

        @JvmField
        val REMOTE_VERSION = Issue.create(
            "PicPayVersionChecker",
            "Newer Library Versions Available",
            "This detector checks with a central repository to see if there are newer versions " +
                    "available for the dependencies used by this project. " +
                    "This is similar to the `GradleDependency` check, which checks for newer versions " +
                    "available in the Android SDK tools and libraries, but this works with any " +
                    "MavenCentral dependency, and connects to the library every time, which makes " +
                    "it more flexible but also *much* slower.",
            Category.MESSAGES,
            7,
            Severity.WARNING,
            IMPLEMENTATION
        ).setEnabledByDefault(true)

    }
}