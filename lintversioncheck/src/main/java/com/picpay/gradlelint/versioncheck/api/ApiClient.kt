package com.picpay.gradlelint.versioncheck.api

internal interface ApiClient {
    fun executeRequest(request: MavenRemoteRequest): MavenRemoteResponse?
}