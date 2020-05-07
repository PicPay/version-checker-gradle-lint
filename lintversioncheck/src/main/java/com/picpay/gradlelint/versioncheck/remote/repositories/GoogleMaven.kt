package com.picpay.gradlelint.versioncheck.remote.repositories

import com.android.utils.XmlUtils
import com.picpay.gradlelint.versioncheck.library.Library
import com.picpay.gradlelint.versioncheck.remote.api.ApiClient
import com.picpay.gradlelint.versioncheck.remote.api.MavenRemoteRequest
import java.net.URL

internal class GoogleMaven(client: ApiClient) : MavenRemoteRepository(client) {

    override fun getNewVersionOrNull(
        actualLibrary: Library,
        responseBody: String
    ): Library? {
        val newVersion = responseBody
            .getLibraryVersionFromGoogleMaven(actualLibrary.artifactId)
        return if (newVersion != actualLibrary.version) {
            actualLibrary.copy(version = newVersion)
        } else null
    }

    override fun createQueryFromLibrary(library: Library): MavenRemoteRequest {
        val groupIdParam = library.groupId.replace(".", "/")
        return MavenRemoteRequest(
            query = URL(
                "https://dl.google.com/dl/android/maven2/${groupIdParam}/group-index.xml"
            )
        )
    }

    private fun String.getLibraryVersionFromGoogleMaven(artifactId: String): String {
        val document = XmlUtils.parseDocumentSilently(this, true)
        return document.getElementsByTagName(artifactId).item(0)
            .attributes
            .getNamedItem("versions")
            .nodeValue
            .replace("\"", "")
            .split(",")
            .reversed()
            .first { it.matches("([0-9]+[.][0-9]+[.][0-9]+)".toRegex()) }
    }
}