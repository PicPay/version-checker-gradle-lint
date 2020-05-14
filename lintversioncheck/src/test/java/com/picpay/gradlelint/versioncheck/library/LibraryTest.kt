package com.picpay.gradlelint.versioncheck.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
internal class LibraryTest {

    @Test
    fun `toString from library Should return formatted artifact string`() {
        val library = Library(
            groupId = "librarygroupId",
            artifactId = "libraryartifactId",
            version = "1.0"
        )

        val expected = "librarygroupId:libraryartifactId:1.0"

        assertEquals(expected, library.toString())
    }

    @Test
    fun `toLibrary with formatted artifact string Should return library`() {
        val expected = Library(
            groupId = "librarygroupId",
            artifactId = "libraryartifactId",
            version = "1.0"
        )

        val resultLib = "librarygroupId:libraryartifactId:1.0".toLibrary()

        assertEquals(expected, resultLib)
    }

    @Test
    fun `when library starts with android Should return true if call isGoogleLib`() {
        val lib = Library(
            groupId = "androidx.appcompat",
            artifactId = "appcompat",
            version = "1.1.0"
        )

        assertTrue(lib.isGoogleLib())
    }

    @Test
    fun `when library starts with com-google Should return true if call isGoogleLib`() {
        val lib = Library(
            groupId = "com.google",
            artifactId = "guava",
            version = "1.0.0"
        )

        assertTrue(lib.isGoogleLib())
    }

    @Test
    fun `when library starts with com-github Should return true if call isJitpackLib`() {
        val lib = Library(
            groupId = "com.github.PhilJay",
            artifactId = "MPAndroidChart",
            version = "1.0.0"
        )

        assertTrue(lib.isJitpackLib())
    }
}