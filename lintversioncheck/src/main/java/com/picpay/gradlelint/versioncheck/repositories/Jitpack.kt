package com.picpay.gradlelint.versioncheck.repositories

import com.google.gson.Gson
import com.picpay.gradlelint.versioncheck.extensions.isVersionNumber
import com.picpay.gradlelint.versioncheck.library.Library
import com.picpay.gradlelint.versioncheck.api.ApiClient
import com.picpay.gradlelint.versioncheck.api.MavenRemoteRequest
import java.net.URL

internal class Jitpack(client: ApiClient) : MavenRemoteRepository(client) {

    private data class JitpackResponse(val version: String)

    override fun getNewVersionOrNull(
        actualLibrary: Library,
        responseBody: String
    ): RepositoryResult = try {
        val latestVersion = Gson().fromJson(responseBody, JitpackResponse::class.java).version
        when {
            latestVersion == actualLibrary.version -> RepositoryResult.NoUpdateFound
            latestVersion.isVersionNumber() -> {
                RepositoryResult.NewVersionAvailable(actualLibrary.copy(version = latestVersion))
            }
            else -> RepositoryResult.ArtifactNotFound
        }
    } catch (e: Throwable) {
        RepositoryResult.ArtifactNotFound
    }

    override fun createQueryFromLibrary(library: Library): MavenRemoteRequest {
        val query = StringBuilder()
            .append("https://jitpack.io/api/builds/")
            .append(library.groupId)
            .append("/")
            .append(library.artifactId)
            .append("/latest")
        return MavenRemoteRequest(query = URL(query.toString()))
    }
}