package com.picpay.gradlelint.versioncheck.remote.repositories

import com.google.gson.Gson
import com.picpay.gradlelint.versioncheck.extensions.firstReleaseVersion
import com.picpay.gradlelint.versioncheck.library.Library
import com.picpay.gradlelint.versioncheck.library.mapToNewVersionFromLibraryOrNull
import com.picpay.gradlelint.versioncheck.remote.api.ApiClient
import com.picpay.gradlelint.versioncheck.remote.api.MavenRemoteRequest
import java.net.URL

internal class Jitpack(client: ApiClient) : MavenRemoteRepository(client) {

    private data class JitpackResponse(val version: String)

    override fun getNewVersionOrNull(
        actualLibrary: Library,
        responseBody: String
    ): Library? = try {
        val response = Gson().fromJson(responseBody, JitpackResponse::class.java)
        listOf(response.version)
            .firstReleaseVersion()
            ?.mapToNewVersionFromLibraryOrNull(actualLibrary)
    } catch (e: Throwable) {
        null
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