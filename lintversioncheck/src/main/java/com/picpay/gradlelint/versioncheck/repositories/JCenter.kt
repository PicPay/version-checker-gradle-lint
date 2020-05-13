package com.picpay.gradlelint.versioncheck.repositories

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.picpay.gradlelint.versioncheck.extensions.firstReleaseVersion
import com.picpay.gradlelint.versioncheck.library.Library
import com.picpay.gradlelint.versioncheck.api.ApiClient
import com.picpay.gradlelint.versioncheck.api.MavenRemoteRequest
import java.net.URL

internal class JCenter(client: ApiClient) : MavenRemoteRepository(client) {

    private data class JCenterResponse(val versions: List<String>)

    override fun getNewVersionOrNull(
        actualLibrary: Library,
        responseBody: String
    ): RepositoryResult = try {
        val responseAsArray: JsonArray = JsonParser().parse(responseBody).asJsonArray

        val versions = responseAsArray.map { item ->
            Gson().fromJson(
                item.toString(),
                JCenterResponse::class.java
            ).versions
        }.firstOrNull { it.contains(actualLibrary.version) }

        if (versions == null) {
            RepositoryResult.ArtifactNotFound
        } else {
            val latestVersion = versions.firstReleaseVersion()
            when {
                latestVersion == actualLibrary.version -> {
                    RepositoryResult.NoUpdateFound
                }
                latestVersion != null -> {
                    RepositoryResult.NewVersionAvailable(actualLibrary.copy(version = latestVersion))
                }
                else -> {
                    RepositoryResult.ArtifactNotFound
                }
            }
        }
    } catch (e: Throwable) {
        RepositoryResult.ArtifactNotFound
    }

    override fun createQueryFromLibrary(library: Library): MavenRemoteRequest {
        val query = StringBuilder()
            .append("https://api.bintray.com/search/packages/maven")
            .append("?g=")
            .append(library.groupId)
            .append("&a=")
            .append(library.artifactId)
        return MavenRemoteRequest(query = URL(query.toString()))
    }
}