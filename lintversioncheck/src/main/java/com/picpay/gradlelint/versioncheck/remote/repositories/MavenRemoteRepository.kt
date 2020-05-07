package com.picpay.gradlelint.versioncheck.remote.repositories

import com.picpay.gradlelint.versioncheck.library.Library
import com.picpay.gradlelint.versioncheck.remote.api.ApiClient
import com.picpay.gradlelint.versioncheck.remote.api.MavenRemoteRequest

internal abstract class MavenRemoteRepository(private val client: ApiClient) {

    fun findNewVersionToLibrary(library: Library): Library? {
        val request = createQueryFromLibrary(library)
        return client.executeRequest(request)?.let { response ->
            getNewVersionOrNull(library, response.body)
        }
    }

    protected abstract fun getNewVersionOrNull(
        actualLibrary: Library,
        responseBody: String
    ): Library?

    protected abstract fun createQueryFromLibrary(library: Library): MavenRemoteRequest
}