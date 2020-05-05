package com.picpay.gradlelint.versioncheck

import com.android.tools.lint.client.api.LintClient
import java.io.*
import java.net.URL
import java.net.URLConnection
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Suppress("UnstableApiUsage")
internal class VersionRemoteDataSource(private val client: LintClient) {

    internal data class VersionRequest(val content: String, val artifact: String)
    internal data class VersionResponse(val content: String)

    fun getNewVersionAvailable(library: Library): Library? {
        val request = createRequestFrom(library) ?: return null
        val response = fetchRemoteMaven(request)?.content ?: return null

        var index = response.indexOf("\"response\"")

        while (index != -1) {
            index = response.indexOf("\"v\":", index)
            if (index != -1) {
                index += 4
                val start = response.indexOf('"', index) + 1
                val end = response.indexOf('"', start + 1)
                if (start in 0 until end) {
                    val versionAvailable = (response.substring(start, end))
                    if (versionAvailable != library.version) {
                        return library.copy(version = versionAvailable)
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
                    val sb = java.lang.StringBuilder(500)
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
        val query = StringBuilder()
        val encoding = "UTF-8"
        try {
            query.append("http://search.maven.org/solrsearch/select?q=g:%22")
            query.append(URLEncoder.encode(library.groupId, encoding))
            query.append("%22+AND+a:%22")
            query.append(URLEncoder.encode(library.artifactId, encoding))
        } catch (ee: UnsupportedEncodingException) {
            return null
        }

        query.append("%22&core=gav")
        query.append("&rows=1")
        query.append("&wt=json")

        return VersionRequest(
            content = query.toString(),
            artifact = library.toString()
        )
    }

}