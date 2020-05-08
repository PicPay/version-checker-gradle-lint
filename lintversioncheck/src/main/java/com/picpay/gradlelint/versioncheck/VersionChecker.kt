package com.picpay.gradlelint.versioncheck

import com.android.tools.lint.client.api.LintClient
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.GradleContext
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.picpay.gradlelint.versioncheck.extensions.containsVersionNumber
import com.picpay.gradlelint.versioncheck.extensions.findBuildSrcFromProjectDir
import com.picpay.gradlelint.versioncheck.extensions.getVarNameInVersionDeclaration
import com.picpay.gradlelint.versioncheck.extensions.getVarValueFromVersionsFile
import com.picpay.gradlelint.versioncheck.extensions.removeComments
import com.picpay.gradlelint.versioncheck.extensions.tokenize
import com.picpay.gradlelint.versioncheck.library.Library
import com.picpay.gradlelint.versioncheck.library.toLibrary
import com.picpay.gradlelint.versioncheck.remote.api.Api
import com.picpay.gradlelint.versioncheck.remote.repositories.MavenRemoteRepositoryHandler
import java.io.File
import java.util.Properties


@Suppress("UnstableApiUsage")
class VersionChecker : Detector(), Detector.GradleScanner {

    private var repositoryHandler: MavenRemoteRepositoryHandler? = null

    override fun checkDslPropertyAssignment(
        context: GradleContext,
        property: String,
        value: String,
        parent: String,
        parentParent: String?,
        valueCookie: Any,
        statementCookie: Any
    ) {
        if (parent == DEPENDENCIES && isCustomDependencyDeclaration(context, value)) {
            try {
                val library = getLibraryFromDependency(context, value)
                getRepositoryHandler(context.client).getNewVersionAvailable(library)
                    ?.let { newLibrary ->
                        context.report(
                            REMOTE_VERSION,
                            context.getLocation(valueCookie),
                            "New version available: $newLibrary\nActual: $library"
                        )
                    }
            } catch (e: Throwable) {
                context.report(
                    REMOTE_VERSION,
                    context.getLocation(valueCookie),
                    e.toString()
                )
            }
        }
    }

    private fun getLibraryFromDependency(
        context: GradleContext,
        value: String
    ): Library {

        val buildSrc = context.project.dir.findBuildSrcFromProjectDir()

        checkNotNull(buildSrc) { "buildSrc module not found." }

        val properties = readVersionLintProperties(context.project.dir)
        val dependenciesFileName = properties.getProperty(LINT_DEPENDENCIES_PROPERTY)
        val versionsFile = properties.getProperty(LINT_VERSIONS_PROPERTY)
        val enableCheckForPreReleases = properties.getProperty(LINT_ENABLE_CHECK_PRE_RELEASES)
            ?.toBoolean() ?: false

        if (versionsFile != dependenciesFileName) {
            //TODO: fazer a busca em arquivos diferentes
        }

        val dependenciesFile = File(
            buildSrc.absolutePath,
            "src/main/java/$dependenciesFileName.kt"
        )

        val definition = mutableListOf<String>()
        val dependenciesFileLines = dependenciesFile.readLines()

        dependenciesFileLines.forEachIndexed { index, line ->
            val dependencyVarName = value.split(".")[1]

            if (line.tokenize().contains(dependencyVarName) && !line.containsVersionNumber()) {

                val dependency = if (!line.contains("$")) {
                    dependenciesFileLines[index + 1].removeComments()
                } else {
                    line.split("=")[1].removeComments()
                }
                val dependencyCleaned = dependency.split("$")

                definition.add(dependencyCleaned[0].replace("\"", "").trim())

                val versionVarName = dependencyCleaned[1].getVarNameInVersionDeclaration()
                val versionNumber = dependenciesFile.getVarValueFromVersionsFile(versionVarName)

                definition.add(versionNumber.replace("\"", ""))
                return@forEachIndexed
            }
        }
        return (definition[0] + definition[1]).toLibrary()
    }

    private fun readVersionLintProperties(projectDir: File): Properties {
        val versionLintProperties = File(
            projectDir.findBuildSrcFromProjectDir(),
            LINT_PROPERTIES
        )
        return Properties().apply {
            if (!versionLintProperties.exists()) {
                put(LINT_DEPENDENCIES_PROPERTY, "Dependencies")
                put(LINT_SUFFIX_PROPERTY, "Libs")
                put(LINT_ENABLE_CHECK_PRE_RELEASES, "false")
                store(versionLintProperties.outputStream(), "Gradle Versions Lint")
            }
            load(versionLintProperties.inputStream())
        }
    }

    private fun isCustomDependencyDeclaration(context: GradleContext, value: String): Boolean {
        val suffix = readVersionLintProperties(context.project.dir)
            .getProperty(LINT_SUFFIX_PROPERTY)
        return value.contains("$suffix.")
    }

    private fun getRepositoryHandler(client: LintClient): MavenRemoteRepositoryHandler {
        return repositoryHandler ?: run {
            MavenRemoteRepositoryHandler(Api(client)).also { handler ->
                repositoryHandler = handler
            }
        }
    }

    companion object {

        private const val LINT_PROPERTIES = "versionlint.properties"
        private const val LINT_DEPENDENCIES_PROPERTY = "versionlint.dependencies.file"
        private const val LINT_SUFFIX_PROPERTY = "versionlint.dependencies.suffix"
        private const val LINT_VERSIONS_PROPERTY = "versionlint.versions.file"
        private const val LINT_ENABLE_CHECK_PRE_RELEASES = "versionlint.prerelease.enable"

        private const val DEPENDENCIES = "dependencies"

        private val IMPLEMENTATION = Implementation(
            VersionChecker::class.java,
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