package com.picpay.gradlelint.versioncheck

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.GradleContext
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.picpay.gradlelint.versioncheck.api.Api
import com.picpay.gradlelint.versioncheck.cache.RepositoryCache
import com.picpay.gradlelint.versioncheck.extensions.containsVersionNumber
import com.picpay.gradlelint.versioncheck.extensions.findBuildSrcFromProjectDir
import com.picpay.gradlelint.versioncheck.extensions.getVarNameInVersionDeclaration
import com.picpay.gradlelint.versioncheck.extensions.getVarValueFromVersionsFile
import com.picpay.gradlelint.versioncheck.extensions.isVersionNumber
import com.picpay.gradlelint.versioncheck.extensions.removeComments
import com.picpay.gradlelint.versioncheck.extensions.tokenize
import com.picpay.gradlelint.versioncheck.library.Library
import com.picpay.gradlelint.versioncheck.library.toLibrary
import com.picpay.gradlelint.versioncheck.repositories.MavenRemoteRepositoryHandler
import com.picpay.gradlelint.versioncheck.repositories.RepositoryResult
import java.io.File
import java.util.Properties


@Suppress("UnstableApiUsage")
class VersionChecker : Detector(), Detector.GradleScanner {

    private var repositoryHandler: MavenRemoteRepositoryHandler? = null
    private var buildSrcDir: File? = null
    private var versionsProperties: Properties? = null
    private var dependenciesFileLines = emptyList<String>()

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
                val library: Library = getLibraryFromDependency(context, value) ?: return

                val cacheLifetime = readVersionCheckerProperties(context.project.dir)
                    .getProperty(LINT_CACHE_LIFETIME).toLong()

                val result = getRepositoryHandler(context, cacheLifetime)
                    .getNewVersionAvailable(library)

                if (result is RepositoryResult.NewVersionAvailable) {
                    context.report(
                        REMOTE_VERSION,
                        context.getLocation(valueCookie),
                        "New version available: ${result.version}\nActual: $library"
                    )
                }

            } catch (e: Throwable) {
                context.log(e, "VersionChecker - Error")
            }
        }
    }

    private fun getLibraryFromDependency(
        context: GradleContext,
        value: String
    ): Library? {

        val buildSrc = getBuildSrcDir(context.project.dir)

        val properties = readVersionCheckerProperties(context.project.dir)
        val dependenciesFileName = properties.getProperty(LINT_DEPENDENCIES_PROPERTY)
        val versionsFile = properties.getProperty(LINT_VERSIONS_PROPERTY)

        val enableCheckForPreReleases: Boolean = properties
            .getProperty(LINT_ENABLE_CHECK_PRE_RELEASES)
            ?.toBoolean() ?: false

        if (versionsFile != dependenciesFileName) {
            //TODO: fazer a busca em arquivos diferentes
        }

        val dependenciesFile = File(
            buildSrc.absolutePath,
            "src/main/java/$dependenciesFileName.kt"
        )

        var actualLibrary: Library? = null
        val fileLines = getDependenciesFileLines(dependenciesFile)

        fileLines.forEachIndexed { index, line ->

            val dependencyVarName = value.split(".")[1]

            if (line.tokenize().contains(dependencyVarName) && !line.containsVersionNumber()) {

                val dependency = if (!line.contains("$")) {
                    dependenciesFileLines[index + 1].removeComments()
                } else {
                    line.split("=")[1].removeComments()
                }

                val dependencyCleaned = dependency.split("$")
                val groupAndArtifactId = dependencyCleaned[0]
                    .replace("\"", "")
                    .trim()

                val versionVarName = dependencyCleaned[1].getVarNameInVersionDeclaration()
                val versionNumber = fileLines.getVarValueFromVersionsFile(versionVarName)
                val version = versionNumber.replace("\"", "")

                if (version.isVersionNumber(enableCheckForPreReleases)) {
                    actualLibrary = (groupAndArtifactId + version).toLibrary()
                }
                return@forEachIndexed
            }
        }
        return actualLibrary
    }

    private fun getDependenciesFileLines(dependenciesFile: File): List<String> {
        if (dependenciesFileLines.isEmpty()) {
            dependenciesFileLines = dependenciesFile.readLines()
        }
        return dependenciesFileLines
    }

    private fun readVersionCheckerProperties(projectDir: File): Properties {
        return versionsProperties ?: run {
            val versionLintPropertiesFile = File(
                projectDir.findBuildSrcFromProjectDir(),
                LINT_PROPERTIES
            )
            Properties().apply {
                if (!versionLintPropertiesFile.exists()) {
                    put(LINT_DEPENDENCIES_PROPERTY, "Dependencies")
                    put(LINT_SUFFIX_PROPERTY, "Libs")
                    put(LINT_ENABLE_CHECK_PRE_RELEASES, "false")
                    put(LINT_CACHE_LIFETIME, "60")
                    store(
                        versionLintPropertiesFile.outputStream(),
                        "Gradle Versions Lint"
                    )
                }
                load(versionLintPropertiesFile.inputStream())
                versionsProperties = this
            }
        }
    }

    private fun isCustomDependencyDeclaration(context: GradleContext, value: String): Boolean {
        val suffix = readVersionCheckerProperties(context.project.dir)
            .getProperty(LINT_SUFFIX_PROPERTY)
        return value.contains("$suffix.")
    }

    private fun getRepositoryHandler(
        context: GradleContext,
        cacheLifetime: Long
    ): MavenRemoteRepositoryHandler {
        return repositoryHandler ?: run {
            MavenRemoteRepositoryHandler(
                Api(context.client),
                RepositoryCache(lifetime = cacheLifetime)
            ).also { handler -> repositoryHandler = handler }
        }
    }

    private fun getBuildSrcDir(parentDir: File): File {
        return buildSrcDir ?: run {
            val buildSrc = parentDir.findBuildSrcFromProjectDir()
            checkNotNull(buildSrc) { "buildSrc module not found." }
            buildSrcDir = buildSrc
            return buildSrc
        }
    }

    companion object {

        private const val LINT_PROPERTIES = "versionlint.properties"
        private const val LINT_DEPENDENCIES_PROPERTY = "versionlint.dependencies.file"
        private const val LINT_SUFFIX_PROPERTY = "versionlint.dependencies.suffix"
        private const val LINT_VERSIONS_PROPERTY = "versionlint.versions.file"
        private const val LINT_ENABLE_CHECK_PRE_RELEASES = "versionlint.prerelease.enable"
        private const val LINT_CACHE_LIFETIME = "versionlint.cache.time.minutes"

        private const val DEPENDENCIES = "dependencies"

        private val IMPLEMENTATION = Implementation(
            VersionChecker::class.java,
            Scope.GRADLE_SCOPE
        )

        @JvmField
        val REMOTE_VERSION = Issue.create(
            "PicPayVersionChecker",
            "Newer Library Versions Available",
            "This detector checks with a central repository to see if there are newer" +
                    " versions available for the dependencies used by this project. " +
                    "This is similar to the `GradleDependency` check, which checks for newer" +
                    " versions available. This works with any Google, MavenCentral, JCenter or" +
                    " Jitpack dependency, and connects to the remote library if the reference not" +
                    " exists in local cache. The cache lifetime default is 60 minutes, but can " +
                    "be modified in `versionlint.properties` into `buildSrc` module.",
            Category.MESSAGES,
            7,
            Severity.WARNING,
            IMPLEMENTATION
        ).setEnabledByDefault(true)

    }
}