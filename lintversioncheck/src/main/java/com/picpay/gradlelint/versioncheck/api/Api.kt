package com.picpay.gradlelint.versioncheck.api

import com.android.tools.lint.client.api.LintClient
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URLConnection
import java.nio.charset.StandardCharsets

@Suppress("UnstableApiUsage")
internal class Api(private val client: LintClient) : ApiClient {

    override fun executeRequest(request: MavenRemoteRequest): MavenRemoteResponse? {
        var response: String? = null
        try {
            val connection: URLConnection = client.openConnection(request.query) ?: return null

            try {
                val inputStream: InputStream = connection.getInputStream() ?: return null
                val bufferedReader = BufferedReader(
                    InputStreamReader(inputStream, StandardCharsets.UTF_8)
                )

                response = bufferedReader.useLines { lines ->
                    StringBuilder()
                        .apply {
                            with(lines.toList()) {
                                forEachIndexed { index, line ->
                                    append(line)
                                    if (index < lastIndex) append('\n')
                                }
                            }
                        }.toString()
                }
            } finally {
                client.closeConnection(connection)
            }
        } catch (ioe: IOException) {
            client.log(
                ioe, "Could not connect to ${request.query.host} to look up the " +
                        "latest available version"
            )
        }
        return response?.let { MavenRemoteResponse(response) }
    }
}