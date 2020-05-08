package com.picpay.gradlelint.versioncheck.remote.repositories

import com.google.gson.Gson
import com.picpay.gradlelint.versioncheck.extensions.firstReleaseVersion
import com.picpay.gradlelint.versioncheck.library.Library
import com.picpay.gradlelint.versioncheck.library.mapToNewVersionFromLibraryOrNull
import com.picpay.gradlelint.versioncheck.remote.api.ApiClient
import com.picpay.gradlelint.versioncheck.remote.api.MavenRemoteRequest
import java.net.URL

internal class MavenCentral(client: ApiClient) : MavenRemoteRepository(client) {

    private data class MavenCentralResponse(val response: MavenCentralResponseDocs)

    private data class MavenCentralResponseDocs(val docs: List<MavenCentralVersion>)

    private data class MavenCentralVersion(val v: String)

    override fun getNewVersionOrNull(
        actualLibrary: Library,
        responseBody: String
    ): Library? = try {

        val mavenCentralResponse = Gson().fromJson(responseBody, MavenCentralResponse::class.java)
        val versions = mavenCentralResponse.response.docs

        if (versions.isEmpty()) null
        else {
            versions.map { version -> version.v }
                .firstReleaseVersion()
                ?.mapToNewVersionFromLibraryOrNull(actualLibrary)
        }
    } catch (e: Throwable) {
        null
    }

    override fun createQueryFromLibrary(library: Library): MavenRemoteRequest {
        val query = StringBuilder()
            .append("http://search.maven.org/solrsearch/select?q=g:%22")
            .append(library.groupId)
            .append("%22+AND+a:%22")
            .append(library.artifactId)
            .append("%22&core=gav")
            .append("&rows=1")
            .append("&wt=json")
            .toString()
        return MavenRemoteRequest(query = URL(query))
    }
}