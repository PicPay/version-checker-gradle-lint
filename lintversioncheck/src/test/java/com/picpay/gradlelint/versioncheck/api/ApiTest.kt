package com.picpay.gradlelint.versioncheck.api

import com.picpay.gradlelint.versioncheck.helpers.createURLFromResourceFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.net.URL

@RunWith(JUnit4::class)
internal class ApiTest {

    @Test
    fun `executeRequest Should return string response when called from valid request`() {
        val api = Api(FakeLintClient())
        val url: URL = createURLFromResourceFile("response/maven_central_response.json")
        val expectedBody: String = url.readText()
        val request = MavenRemoteRequest(url)

        val response: MavenRemoteResponse? = api.executeRequest(request)

        assertNotNull(response)
        assertEquals(expectedBody, response!!.body)
    }

    @Test
    fun `executeRequest Should return null response when called from invalid request`() {
        val api = Api(FakeLintClient())
        val url = URL("http://localhost")
        val request = MavenRemoteRequest(url)

        val response: MavenRemoteResponse? = api.executeRequest(request)

        assertNull(response)
    }
}
