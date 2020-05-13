package com.picpay.gradlelint.versioncheck.repositories

import com.google.gson.Gson
import com.picpay.gradlelint.versioncheck.extensions.firstReleaseVersion
import com.picpay.gradlelint.versioncheck.library.Library
import com.picpay.gradlelint.versioncheck.api.ApiClient
import com.picpay.gradlelint.versioncheck.api.MavenRemoteRequest
import java.net.URL

internal class MavenCentral(client: ApiClient) : MavenRemoteRepository(client) {

    private data class MavenCentralResponse(val response: MavenCentralResponseDocs)

    private data class MavenCentralResponseDocs(val docs: List<MavenCentralVersion>)

    private data class MavenCentralVersion(val v: String)

    override fun getNewVersionOrNull(
        actualLibrary: Library,
        responseBody: String
    ): RepositoryResult = try {

        val mavenCentralResponse = Gson().fromJson(responseBody, MavenCentralResponse::class.java)
        val responseVersions = mavenCentralResponse.response.docs

        if (responseVersions.isEmpty() || responseVersions.none { it.v == actualLibrary.version }) {
            RepositoryResult.ArtifactNotFound
        } else {
            val releaseVersion = responseVersions.map { version -> version.v }.firstReleaseVersion()
            when {
                releaseVersion == actualLibrary.version -> RepositoryResult.NoUpdateFound
                releaseVersion != null -> {
                    RepositoryResult.NewVersionAvailable(
                        actualLibrary.copy(version = releaseVersion)
                    )
                }
                else -> RepositoryResult.ArtifactNotFound
            }
        }
    } catch (e: Throwable) {
        RepositoryResult.ArtifactNotFound
    }

    override fun createQueryFromLibrary(library: Library): MavenRemoteRequest {
        val query = StringBuilder()
            .append("http://search.maven.org/solrsearch/select?q=g:%22")
            .append(library.groupId)
            .append("%22+AND+a:%22")
            .append(library.artifactId)
            .append("%22&core=gav")
            .append("&wt=json")
            .toString()
        return MavenRemoteRequest(query = URL(query))
    }
}