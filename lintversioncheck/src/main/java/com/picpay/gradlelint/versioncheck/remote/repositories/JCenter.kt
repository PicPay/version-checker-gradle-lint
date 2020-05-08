package com.picpay.gradlelint.versioncheck.remote.repositories

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.picpay.gradlelint.versioncheck.extensions.firstReleaseVersion
import com.picpay.gradlelint.versioncheck.library.Library
import com.picpay.gradlelint.versioncheck.library.mapToNewVersionFromLibraryOrNull
import com.picpay.gradlelint.versioncheck.remote.api.ApiClient
import com.picpay.gradlelint.versioncheck.remote.api.MavenRemoteRequest
import java.net.URL

internal class JCenter(client: ApiClient) : MavenRemoteRepository(client) {

    private data class JCenterResponse(val versions: List<String>)

    override fun getNewVersionOrNull(
        actualLibrary: Library,
        responseBody: String
    ): Library? {
        val body = JsonParser().parse(responseBody).asJsonArray.get(0).toString()
        return Gson().fromJson(body, JCenterResponse::class.java)
            .versions.firstReleaseVersion()
            ?.mapToNewVersionFromLibraryOrNull(actualLibrary)
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