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
}