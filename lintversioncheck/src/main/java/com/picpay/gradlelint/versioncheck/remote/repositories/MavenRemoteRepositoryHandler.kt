package com.picpay.gradlelint.versioncheck.remote.repositories

import com.android.tools.lint.client.api.LintClient
import com.picpay.gradlelint.versioncheck.library.Library
import com.picpay.gradlelint.versioncheck.library.isGoogleLib
import com.picpay.gradlelint.versioncheck.remote.api.ApiClient

@Suppress("UnstableApiUsage")
internal class MavenRemoteRepositoryHandler(private val client: LintClient) {

    fun getNewVersionAvailable(library: Library): Library? {
        val apiClient = ApiClient(client)
        val mavenRepository: MavenRemoteRepository = if (library.isGoogleLib()) {
            GoogleMaven(apiClient)
        } else {
            MavenCentral(apiClient)
        }

        return mavenRepository.findNewVersionToLibrary(library)
    }

}
