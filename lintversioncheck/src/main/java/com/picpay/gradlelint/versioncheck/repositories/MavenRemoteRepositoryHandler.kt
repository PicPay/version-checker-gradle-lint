package com.picpay.gradlelint.versioncheck.repositories

import com.picpay.gradlelint.versioncheck.library.Library
import com.picpay.gradlelint.versioncheck.library.isGoogleLib
import com.picpay.gradlelint.versioncheck.library.isJitpackLib
import com.picpay.gradlelint.versioncheck.library.toLibrary
import com.picpay.gradlelint.versioncheck.api.ApiClient
import java.io.File

@Suppress("UnstableApiUsage")
internal class MavenRemoteRepositoryHandler(
    private val apiClient: ApiClient,
    cacheParentDir: File
) {

    private val google by lazy { GoogleMaven(apiClient) }
    private val mavenCentral by lazy { MavenCentral(apiClient) }
    private val jcenter by lazy { JCenter(apiClient) }
    private val jitpack by lazy { Jitpack(apiClient) }

    private val cacheDir: File = File(cacheParentDir.absolutePath, "cache")
        .also { dir -> if (!dir.exists()) dir.mkdirs() }

    fun getNewVersionAvailable(library: Library): RepositoryResult {
        val versionFromCache = getVersionFromCacheOrNull(library)
        if (versionFromCache != null && versionFromCache != library) {
            return RepositoryResult.NewVersionAvailable(versionFromCache)
        }

        val repositoryResult = if (library.isGoogleLib()) {
            google.findNewVersionToLibrary(library)
        } else {
            mavenCentral.findNewVersionToLibrary(library)
                .tryIfArtifactNotFound { jcenter.findNewVersionToLibrary(library) }
                .tryIfArtifactNotFound(additionalCondition = library.isJitpackLib()) {
                    jitpack.findNewVersionToLibrary(library)
                }
        }

        if (repositoryResult is RepositoryResult.NewVersionAvailable) {
            saveOnCacheDir(repositoryResult.version)
        }

        return repositoryResult
    }

    private fun saveOnCacheDir(library: Library) {
        File(cacheDir, library.groupId + ":" + library.artifactId)
            .apply { writeText(library.toString()) }
    }

    private fun getVersionFromCacheOrNull(library: Library): Library? {
        return File(cacheDir, library.groupId + ":" + library.artifactId).run {
            if (exists()) readText().toLibrary()
            else null
        }
    }

    private fun RepositoryResult.tryIfArtifactNotFound(
        additionalCondition: Boolean = true,
        action: () -> RepositoryResult
    ): RepositoryResult =
        if (this is RepositoryResult.ArtifactNotFound && additionalCondition) {
            action()
        } else {
            this
        }
}
