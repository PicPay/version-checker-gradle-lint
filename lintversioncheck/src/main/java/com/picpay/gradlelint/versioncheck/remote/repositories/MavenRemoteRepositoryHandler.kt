package com.picpay.gradlelint.versioncheck.remote.repositories

import com.picpay.gradlelint.versioncheck.library.Library
import com.picpay.gradlelint.versioncheck.library.isGoogleLib
import com.picpay.gradlelint.versioncheck.library.isJitpackLib
import com.picpay.gradlelint.versioncheck.remote.api.ApiClient

@Suppress("UnstableApiUsage")
internal class MavenRemoteRepositoryHandler(private val apiClient: ApiClient) {

    private val google by lazy { GoogleMaven(apiClient) }
    private val mavenCentral by lazy { MavenCentral(apiClient) }
    private val jcenter by lazy { JCenter(apiClient) }
    private val jitpack by lazy { Jitpack(apiClient) }

    fun getNewVersionAvailable(library: Library): Library? {
        return if (library.isGoogleLib()) {
            google.findNewVersionToLibrary(library)
        } else {
            mavenCentral.findNewVersionToLibrary(library)
                ?: jcenter.findNewVersionToLibrary(library)
                ?: run {
                    if (library.isJitpackLib())
                        jitpack.findNewVersionToLibrary(library)
                    else null
                }
        }
    }
}
