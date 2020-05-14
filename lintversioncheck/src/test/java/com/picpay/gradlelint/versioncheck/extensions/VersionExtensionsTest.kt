package com.picpay.gradlelint.versioncheck.extensions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class VersionExtensionsTest {

    @Test
    fun `with release list Should return first version When match pattern`() {
        val versions = listOf("10.4.0", "3.0.0", "2.3", "0.12.0")

        val expected = "10.4.0"

        assertEquals(expected, versions.firstReleaseVersion())
    }

    @Test
    fun `with release list Should return first version When match pre-release pattern`() {
        val versions = listOf("3.0.0-rc01", "2.3-alpha", "0.12.0-beta")

        val expected = "3.0.0-rc01"

        assertEquals(expected, versions.firstReleaseVersion(enablePreReleaseCheck = true))
    }

    @Test
    fun `with pre-release list Should return null When disaable pre-release check`() {
        val versions = listOf("3.0.0-rc01", "2.3-alpha", "0.12.0-beta")

        val release = versions.firstReleaseVersion(enablePreReleaseCheck = false)

        assertNull(release)
    }

    @Test
    fun `with pre-release and release list Should return first release version`() {
        val versions = listOf("3.0.0-rc01", "2.3-alpha", "1.0.0", "0.12.0-beta")

        val expected = "1.0.0"

        assertEquals(expected, versions.firstReleaseVersion())
    }

    @Test
    fun `with string of version reference Should extract variable name`() {
        val expected = "libraryName"

        val varName = "{Versions.libraryName}".getVarNameInVersionDeclaration()

        assertEquals(expected, varName)
    }
}