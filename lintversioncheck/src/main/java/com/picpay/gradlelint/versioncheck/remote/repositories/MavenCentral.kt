package com.picpay.gradlelint.versioncheck.remote.repositories

import com.picpay.gradlelint.versioncheck.library.Library
import com.picpay.gradlelint.versioncheck.remote.api.ApiClient
import com.picpay.gradlelint.versioncheck.remote.api.MavenRemoteRequest
import java.net.URL
import java.net.URLEncoder

internal class MavenCentral(client: ApiClient) : MavenRemoteRepository(client) {

    override fun getNewVersionOrNull(
        actualLibrary: Library,
        responseBody: String
    ): Library? {
        var index = responseBody.indexOf("\"response\"")

        while (index != -1) {
            index = responseBody.indexOf("\"v\":", index)
            if (index != -1) {
                index += 4
                val start = responseBody.indexOf('"', index) + 1
                val end = responseBody.indexOf('"', start + 1)
                if (start in 0 until end) {
                    val versionAvailable = (responseBody.substring(start, end))
                    if (versionAvailable != actualLibrary.version) {
                        return actualLibrary.copy(version = versionAvailable)
                    }
                }
            }
        }
        return null
    }

    override fun createQueryFromLibrary(library: Library): MavenRemoteRequest {
        val encoding = "UTF-8"
        val query = StringBuilder()
            .append("http://search.maven.org/solrsearch/select?q=g:%22")
            .append(URLEncoder.encode(library.groupId, encoding))
            .append("%22+AND+a:%22")
            .append(URLEncoder.encode(library.artifactId, encoding))
            .append("%22&core=gav")
            .append("&rows=1")
            .append("&wt=json")
            .toString()
        return MavenRemoteRequest(query = URL(query))
    }
}