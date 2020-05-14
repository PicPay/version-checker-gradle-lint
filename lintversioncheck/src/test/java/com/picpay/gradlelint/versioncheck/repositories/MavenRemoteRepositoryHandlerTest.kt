package com.picpay.gradlelint.versioncheck.repositories

import com.picpay.gradlelint.versioncheck.api.FakeApi
import com.picpay.gradlelint.versioncheck.cache.LibraryCache
import com.picpay.gradlelint.versioncheck.library.Library
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
internal class MavenRemoteRepositoryHandlerTest {

    @Test
    fun `with Google repository Should get new version from library when old version is send`() {
        val repositoryHandler = MavenRemoteRepositoryHandler(
            apiClient = FakeApi(FakeApi.Source.GOOGLE),
            cache = EmptyCache()
        )

        val googleLib = Library(
            groupId = "com.google.firebase",
            artifactId = "firebase-messaging",
            version = "10.0.1"
        )

        val result = repositoryHandler.getNewVersionAvailable(googleLib)

        assertThat(result, instanceOf(RepositoryResult.NewVersionAvailable::class.java))

        val newVersionAvailable = result as RepositoryResult.NewVersionAvailable

        assertEquals(googleLib.copy(version = "20.1.7"), newVersionAvailable.version)
    }

    @Test
    fun `with Maven Central repository Should get new version from old version library`() {
        val repositoryHandler = MavenRemoteRepositoryHandler(
            apiClient = FakeApi(FakeApi.Source.MAVEN_CENTRAL),
            cache = EmptyCache()
        )

        val mavenCentralLib = Library(
            groupId = "com.squareup.retrofit2",
            artifactId = "retrofit",
            version = "2.6.0"
        )

        val result = repositoryHandler.getNewVersionAvailable(mavenCentralLib)

        assertThat(result, instanceOf(RepositoryResult.NewVersionAvailable::class.java))

        val newVersionAvailable = result as RepositoryResult.NewVersionAvailable

        assertEquals(mavenCentralLib.copy(version = "2.8.1"), newVersionAvailable.version)
    }

    @Test
    fun `with JCenter repository Should get new version from old version library`() {
        val repositoryHandler = MavenRemoteRepositoryHandler(
            apiClient = FakeApi(FakeApi.Source.JCENTER),
            cache = EmptyCache()
        )

        val jcenterLib = Library(
            groupId = "com.makerame",
            artifactId = "roundedimageview",
            version = "2.0.0"
        )

        val result = repositoryHandler.getNewVersionAvailable(jcenterLib)

        assertThat(result, instanceOf(RepositoryResult.NewVersionAvailable::class.java))

        val newVersionAvailable = result as RepositoryResult.NewVersionAvailable

        assertEquals(jcenterLib.copy(version = "2.1.1"), newVersionAvailable.version)
    }

    @Test
    fun `with Jitpack repository Should get new version from old version library`() {
        val repositoryHandler = MavenRemoteRepositoryHandler(
            apiClient = FakeApi(FakeApi.Source.JITPACK),
            cache = EmptyCache()
        )

        val jitpackLib = Library(
            groupId = "com.github.example",
            artifactId = "library-name",
            version = "2.0.0"
        )

        val result = repositoryHandler.getNewVersionAvailable(jitpackLib)

        assertThat(result, instanceOf(RepositoryResult.NewVersionAvailable::class.java))

        val newVersionAvailable = result as RepositoryResult.NewVersionAvailable

        assertEquals(jitpackLib.copy(version = "3.1.0"), newVersionAvailable.version)
    }

    @Test
    fun `with Google repository Should get ArtifactNotFound when receive empty response`() {
        val repositoryHandler = MavenRemoteRepositoryHandler(
            apiClient = FakeApi(FakeApi.Source.GOOGLE, emptyResponse = "<empty/>"),
            cache = EmptyCache()
        )

        val googleLib = Library(
            groupId = "com.google.firebase",
            artifactId = "firebase-messaging",
            version = "10.0.1"
        )

        val result = repositoryHandler.getNewVersionAvailable(googleLib)

        assertThat(result, instanceOf(RepositoryResult.ArtifactNotFound::class.java))
    }

    @Test
    fun `with Maven Central repository Should get ArtifactNotFound when receive empty response`() {
        val repositoryHandler = MavenRemoteRepositoryHandler(
            apiClient = FakeApi(FakeApi.Source.GOOGLE, emptyResponse = "{}"),
            cache = EmptyCache()
        )

        val mavenCentralLib = Library(
            groupId = "com.squareup.retrofit2",
            artifactId = "retrofit",
            version = "2.6.0"
        )

        val result = repositoryHandler.getNewVersionAvailable(mavenCentralLib)

        assertThat(result, instanceOf(RepositoryResult.ArtifactNotFound::class.java))
    }

    @Test
    fun `with JCenter repository Should get ArtifactNotFound when receive empty response`() {
        val repositoryHandler = MavenRemoteRepositoryHandler(
            apiClient = FakeApi(FakeApi.Source.JCENTER, emptyResponse = "[]"),
            cache = EmptyCache()
        )

        val jcenterLib = Library(
            groupId = "com.makerame",
            artifactId = "roundedimageview",
            version = "2.0.0"
        )

        val result = repositoryHandler.getNewVersionAvailable(jcenterLib)

        assertThat(result, instanceOf(RepositoryResult.ArtifactNotFound::class.java))
    }

    @Test
    fun `with Jitpack repository Should get ArtifactNotFound when receive empty response`() {
        val repositoryHandler = MavenRemoteRepositoryHandler(
            apiClient = FakeApi(FakeApi.Source.JITPACK, emptyResponse = "{}"),
            cache = EmptyCache()
        )

        val jitpackLib = Library(
            groupId = "com.github.example",
            artifactId = "library-name",
            version = "2.0.0"
        )

        val result = repositoryHandler.getNewVersionAvailable(jitpackLib)

        assertThat(result, instanceOf(RepositoryResult.ArtifactNotFound::class.java))
    }

    inner class EmptyCache : LibraryCache {

        override fun get(groupId: String, artifactId: String): Library? = null

        override fun add(library: Library) = Unit

        override fun clear(groupId: String, artifactId: String) = Unit

        override fun clearAll() = Unit

    }
}