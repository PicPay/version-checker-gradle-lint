package com.picpay.gradlelint.versioncheck.remote.api

internal interface ApiClient {
    fun executeRequest(request: MavenRemoteRequest): MavenRemoteResponse?
}