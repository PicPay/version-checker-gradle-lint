package com.picpay.gradlelint.versioncheck.repositories

import com.picpay.gradlelint.versioncheck.library.Library
import com.picpay.gradlelint.versioncheck.api.ApiClient
import com.picpay.gradlelint.versioncheck.api.MavenRemoteRequest

internal abstract class MavenRemoteRepository(private val client: ApiClient) {

    fun findNewVersionFromLibrary(library: Library): RepositoryResult {
        val request = createQueryFromLibrary(library)
        val response = client.executeRequest(request)
        return if (response != null) {
            getNewVersionOrNull(library, response.body)
        } else {
            RepositoryResult.ArtifactNotFound
        }
    }

    protected abstract fun getNewVersionOrNull(
        actualLibrary: Library,
        responseBody: String
    ): RepositoryResult

    protected abstract fun createQueryFromLibrary(library: Library): MavenRemoteRequest
}