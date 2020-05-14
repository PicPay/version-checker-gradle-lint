package com.picpay.gradlelint.versioncheck.cache

import com.picpay.gradlelint.versioncheck.library.Library
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File
import java.util.concurrent.TimeUnit

private const val CACHE_PARENT_DIR = "build/tmp"
private const val CACHE_DIR = "$CACHE_PARENT_DIR/cache"

@RunWith(JUnit4::class)
internal class RepositoryCacheTest {

    @Test
    fun `add method Should save file from library in cache dir`() {
        val cache = createCache()

        val lib = Library(groupId = "group", artifactId = "artifact", version = "1.0")
        val expectCreatedFilePath = "$CACHE_DIR/group.artifact"

        cache.add(lib)

        assertTrue(File(expectCreatedFilePath).exists())
    }

    @Test
    fun `get method Should return library from file in cache dir`() {
        val cache = createCache()

        val expectedResult = Library(groupId = "group", artifactId = "artifact", version = "1.0")

        File("$CACHE_DIR/group.artifact")
            .writeText("group:artifact:1.0")

        val cachedLib = cache.get("group", "artifact")

        assertEquals(expectedResult, cachedLib)
    }

    @Test
    fun `clear method Should delete file in cache dir`() {
        val cache = createCache()

        val cacheFile = File("$CACHE_DIR/group.artifact")
            .apply { writeText("group:artifact:1.0") }

        cache.clear("group", "artifact")

        assertFalse(cacheFile.exists())
    }

    @Test
    fun `clearAll method Should delete cache dir`() {
        val cache = createCache()

        cache.clearAll()

        assertFalse(File(CACHE_DIR).exists())
    }

    @Test
    fun `with outdated cache should delete content in cache dir`() {
        // create initial cache dir
        createCache()

        val cacheDir = File(CACHE_DIR)
        val fileInCacheDir = File("$CACHE_DIR/file.test")
            .apply { writeText("test") }

        assertTrue(fileInCacheDir.exists())
        assertTrue(cacheDir.listFiles()!!.isNotEmpty())

        // clean old cache files
        createCache(lifetime = 2, timeUnit = TimeUnit.NANOSECONDS)

        assertTrue(cacheDir.listFiles().isNullOrEmpty())
        assertFalse(fileInCacheDir.exists())
    }

    private fun createCache(
        lifetime: Long = 1,
        timeUnit: TimeUnit = TimeUnit.MILLISECONDS
    ): RepositoryCache {
        return RepositoryCache(
            cacheParentDir = CACHE_PARENT_DIR,
            lifetime = lifetime,
            timeUnit = timeUnit
        )
    }

    @After
    fun finish() {
        File(CACHE_DIR).deleteRecursively()
    }
}