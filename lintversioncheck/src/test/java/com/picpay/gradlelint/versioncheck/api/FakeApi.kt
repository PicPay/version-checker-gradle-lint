package com.picpay.gradlelint.versioncheck.api

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
                readTextFromResourceFile("google_response.xml")
            }
            Source.MAVEN_CENTRAL -> {
                readTextFromResourceFile("maven_central_response.json")
            }
            Source.JCENTER -> {
                readTextFromResourceFile("jcenter_response.json")
            }
            Source.JITPACK -> {
                readTextFromResourceFile("jitpack_response.json")
            }
        }
        return MavenRemoteResponse(responseBody)
    }

    private fun readTextFromResourceFile(filename: String): String {
        return FakeApi::class.java.classLoader?.getResource(filename)!!.readText()
    }
}