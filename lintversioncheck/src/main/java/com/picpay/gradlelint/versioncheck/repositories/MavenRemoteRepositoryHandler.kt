package com.picpay.gradlelint.versioncheck.repositories

import com.picpay.gradlelint.versioncheck.api.ApiClient
import com.picpay.gradlelint.versioncheck.cache.LibraryCache
import com.picpay.gradlelint.versioncheck.library.Library
import com.picpay.gradlelint.versioncheck.library.isGoogleLib
import com.picpay.gradlelint.versioncheck.library.isJitpackLib
import com.picpay.gradlelint.versioncheck.repositories.RepositoryResult.ArtifactNotFound
import com.picpay.gradlelint.versioncheck.repositories.RepositoryResult.NewVersionAvailable

@Suppress("UnstableApiUsage")
internal class MavenRemoteRepositoryHandler(
    private val apiClient: ApiClient,
    private val cache: LibraryCache
) {

    private val google by lazy { GoogleMaven(apiClient) }
    private val mavenCentral by lazy { MavenCentral(apiClient) }
    private val jcenter by lazy { JCenter(apiClient) }
    private val jitpack by lazy { Jitpack(apiClient) }

    fun getNewVersionAvailable(library: Library): RepositoryResult {
        val cachedLibrary = cache.get(library.groupId, library.artifactId)
        if (cachedLibrary != null && cachedLibrary != library) {
            return NewVersionAvailable(cachedLibrary)
        }

        val remoteResult = if (library.isGoogleLib()) {
            google.findNewVersionFromLibrary(library)
        } else {
            mavenCentral.findNewVersionFromLibrary(library)
                .tryIfArtifactNotFound { jcenter.findNewVersionFromLibrary(library) }
                .tryIfArtifactNotFound(additionalCondition = library.isJitpackLib()) {
                    jitpack.findNewVersionFromLibrary(library)
                }
        }

        if (remoteResult is NewVersionAvailable) cache.add(remoteResult.version)

        return remoteResult
    }

    private fun RepositoryResult.tryIfArtifactNotFound(
        additionalCondition: Boolean = true,
        action: () -> RepositoryResult
    ): RepositoryResult =
        if (this is ArtifactNotFound && additionalCondition) {
            action()
        } else {
            this
        }
}
