package com.picpay.gradlelint.versioncheck

import com.android.tools.lint.client.api.LintClient
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.net.URLConnection
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Suppress("UnstableApiUsage")
internal class VersionRemoteDataSource(private val client: LintClient) {

    internal data class VersionResponse(val content: String)
    internal data class VersionRequest(
        val content: String,
        val artifact: String
    )

    fun getNewVersionAvailable(library: Library): Library? {
        val request = createRequestFrom(library) ?: return null
        val responseBody = fetchRemoteMaven(request)?.content ?: return null

        if (responseBody.isEmpty() ||
            !responseBody.contains(library.artifactId)
        ) return null

        return if (library.isGoogleLib()) {
            extractFromGoogleMaven(library, responseBody)
        } else {
            extractFromMaven(library, responseBody)
        }
    }

    private fun extractFromGoogleMaven(
        actualLibrary: Library,
        responseBody: String
    ): Library? {
        val newVersion = responseBody
            .getContentByTagName(actualLibrary.artifactId)
            .replace("\"", "")
            .split(",")
            .reversed()
            .first { it.matches("([0-9]+[.][0-9]+[.][0-9]+)".toRegex()) }

        return if (newVersion != actualLibrary.version) {
            actualLibrary.copy(version = newVersion)
        } else null
    }

    private fun extractFromMaven(
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

    private fun fetchRemoteMaven(request: VersionRequest): VersionResponse? {
        var response: String? = null
        try {
            val url = URL(request.content)
            val connection: URLConnection = client.openConnection(url) ?: return null

            try {
                val inputStream: InputStream = connection.getInputStream() ?: return null
                val bufferedReader = BufferedReader(
                    InputStreamReader(inputStream, StandardCharsets.UTF_8)
                )

                response = bufferedReader.use { reader ->
                    val sb = StringBuilder(1024)
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        sb.append(line)
                        sb.append('\n')
                    }
                    sb.toString()
                }
            } finally {
                client.closeConnection(connection)
            }
        } catch (ioe: IOException) {
            client.log(
                ioe, "Could not connect to maven central to look up the " +
                        "latest available version for %1\$s", request.artifact
            )
        }
        return response?.let { VersionResponse(response) }
    }

    private fun createRequestFrom(library: Library): VersionRequest? {
        val query: String = if (library.isGoogleLib()) {
            googleMaven(library)
        } else {
            maven(library)
        }
        return VersionRequest(
            content = query,
            artifact = library.toString()
        )
    }

    private fun googleMaven(library: Library): String {
        val groupIdParam = library.groupId.replace(".", "/")
        return "https://dl.google.com/dl/android/maven2/${groupIdParam}/group-index.xml"
    }

    private fun maven(library: Library): String {
        val encoding = "UTF-8"
        return StringBuilder()
            .append("http://search.maven.org/solrsearch/select?q=g:%22")
            .append(URLEncoder.encode(library.groupId, encoding))
            .append("%22+AND+a:%22")
            .append(URLEncoder.encode(library.artifactId, encoding))
            .append("%22&core=gav")
            .append("&rows=1")
            .append("&wt=json")
            .toString()
    }
}
