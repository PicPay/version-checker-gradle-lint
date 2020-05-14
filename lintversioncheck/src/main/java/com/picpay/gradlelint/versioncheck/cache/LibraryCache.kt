package com.picpay.gradlelint.versioncheck.cache

import com.picpay.gradlelint.versioncheck.library.Library

internal interface LibraryCache {

    fun get(groupId: String, artifactId: String): Library?

    fun add(library: Library)

    fun clear(groupId: String, artifactId: String)

    fun clearAll()
}