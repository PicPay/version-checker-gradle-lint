package com.picpay.gradlelint.versioncheck.api

import com.picpay.gradlelint.versioncheck.helpers.readTextFromResourceFile

internal class FakeApi(
    private val source: Source,
    private val emptyResponse: String? = null
) : ApiClient {

    enum class Source {
        GOOGLE,
        MAVEN_CENTRAL,
        JCENTER,
        JITPACK
    }

    override fun executeRequest(request: MavenRemoteRequest): MavenRemoteResponse? {
        if (emptyResponse != null) return MavenRemoteResponse(body = emptyResponse)

        val responseBody = when (source) {
            Source.GOOGLE -> {
                readTextFromResourceFile("response/google_response.xml")
            }
            Source.MAVEN_CENTRAL -> {
                readTextFromResourceFile("response/maven_central_response.json")
            }
            Source.JCENTER -> {
                readTextFromResourceFile("response/jcenter_response.json")
            }
            Source.JITPACK -> {
                readTextFromResourceFile("response/jitpack_response.json")
            }
        }
        return MavenRemoteResponse(responseBody)
    }
}
