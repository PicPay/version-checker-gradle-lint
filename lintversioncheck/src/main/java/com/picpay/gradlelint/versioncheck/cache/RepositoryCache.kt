package com.picpay.gradlelint.versioncheck.cache

import com.picpay.gradlelint.versioncheck.library.Library
import com.picpay.gradlelint.versioncheck.library.toLibrary
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

private const val CACHE_DIR_NAME = "cache"
private const val DEFAULT_CACHE_PARENT_DIR = "/tmp/version-checker"

internal class RepositoryCache(
    cacheParentDir: String = DEFAULT_CACHE_PARENT_DIR,
    lifetime: Long = 60,
    timeUnit: TimeUnit = TimeUnit.MINUTES
) : LibraryCache {

    private val cacheDir: File = getOrCreateCacheDir(cacheParentDir, lifetime, timeUnit)

    @OptIn(ExperimentalTime::class)
    private fun getOrCreateCacheDir(
        parentDir: String,
        lifetime: Long,
        timeUnit: TimeUnit
    ): File {
        return File(parentDir, CACHE_DIR_NAME).apply {
            if (!exists()) {
                mkdirs()
            } else {
                val creationTime = Files
                    .readAttributes(toPath(), BasicFileAttributes::class.java)
                    .creationTime()
                    .toMillis()

                val currentTime = Date().time
                val cacheLifetime = (currentTime - creationTime)

                if (cacheLifetime > lifetime.toDuration(timeUnit).inMilliseconds) {
                    listFiles()?.forEach { file -> file.delete() }
                }
            }
        }
    }

    override fun get(groupId: String, artifactId: String): Library? {
        val cachedFile = File(cacheDir.absolutePath, getFileName(groupId, artifactId))
        return if (cachedFile.exists()) {
            cachedFile.readText().toLibrary()
        } else null
    }

    override fun add(library: Library) {
        with(File(cacheDir.absolutePath, getFileName(library.groupId, library.artifactId))) {
            writeText(library.toString())
        }
    }

    override fun clear(groupId: String, artifactId: String) {
        with(File(cacheDir.absolutePath, getFileName(groupId, artifactId))) {
            if (exists()) delete()
        }
    }

    override fun clearAll() {
        cacheDir.deleteRecursively()
    }

    private fun getFileName(groupId: String, artifactId: String) = "${groupId}.${artifactId}"

}