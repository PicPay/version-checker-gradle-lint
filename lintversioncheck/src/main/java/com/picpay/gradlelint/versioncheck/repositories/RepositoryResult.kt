package com.picpay.gradlelint.versioncheck.repositories

import com.picpay.gradlelint.versioncheck.library.Library

internal sealed class RepositoryResult {

    object ArtifactNotFound: RepositoryResult()

    object NoUpdateFound: RepositoryResult()

    data class NewVersionAvailable(val version: Library): RepositoryResult()
}