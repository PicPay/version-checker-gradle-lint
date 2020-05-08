package com.picpay.gradlelint.versioncheck.remote.repositories

import com.android.utils.XmlUtils
import com.picpay.gradlelint.versioncheck.library.Library
import com.picpay.gradlelint.versioncheck.remote.api.MavenRemoteRequest
import com.picpay.gradlelint.versioncheck.extensions.firstReleaseVersion
import com.picpay.gradlelint.versioncheck.library.mapToNewVersionFromLibraryOrNull
import com.picpay.gradlelint.versioncheck.remote.api.ApiClient
import java.net.URL

internal class GoogleMaven(client: ApiClient) : MavenRemoteRepository(client) {

    override fun getNewVersionOrNull(
        actualLibrary: Library,
        responseBody: String
    ): Library? {
        return responseBody
            .getLibraryVersionFromGoogleMaven(actualLibrary.artifactId)
            ?.mapToNewVersionFromLibraryOrNull(actualLibrary)
    }

    override fun createQueryFromLibrary(library: Library): MavenRemoteRequest {
        val query = StringBuilder()
            .append("https://dl.google.com/dl/android/maven2/")
            .append(library.groupId.replace(".", "/"))
            .append("/group-index.xml")
        return MavenRemoteRequest(query = URL(query.toString()))
    }

    private fun String.getLibraryVersionFromGoogleMaven(artifactId: String): String? {
        val document = XmlUtils.parseDocumentSilently(this, true)
        return document.getElementsByTagName(artifactId).item(0)
            .attributes
            .getNamedItem("versions")
            .nodeValue
            .replace("\"", "")
            .split(",")
            .reversed()
            .firstReleaseVersion()
    }
}