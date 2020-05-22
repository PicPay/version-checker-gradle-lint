package com.picpay.gradlelint.versioncheck.extensions

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File

@RunWith(JUnit4::class)
internal class FileExtensionsTest {

    @Test
    fun `with parent dir as File Should get buildSrc path`() {
        val buildSrcInTest = "build"
        val parentDir = File("").absolutePath
        val projectDir = File(parentDir, "test")
        val expected = "$parentDir/build"

        val resultFile = projectDir.findBuildSrcFromProjectDir(buildSrcInTest)

        assertEquals(expected, resultFile!!.absolutePath)
    }

    @Test
    fun `findKotlinFilesWithSuffix with parent dir as File Should get kotlin files`() {
        val parentDir = File("").absolutePath
        val projectDir = File(parentDir, "src/test/resources")

        val pathVersionsFile = "${projectDir.absolutePath}/kotlin-files/version/Versions.kt"
        val pathLibFile = "${projectDir.absolutePath}/kotlin-files/lib/MyLibs.kt"

        val fileMap = projectDir.findKotlinFilesWithSuffix(listOf("Libs", "Versions"))

        assertEquals(
            "Check Versions File",
            pathVersionsFile,
            (fileMap["Versions"] ?: error("Versions not found")).absolutePath
        )
        assertEquals(
            "Check Lib File",
            pathLibFile, (fileMap["MyLibs"] ?: error("MyLibs not found")).absolutePath
        )
    }
}