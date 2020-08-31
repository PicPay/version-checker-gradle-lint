package com.picpay.gradlelint.versioncheck.extensions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
internal class StringExtensionsTest {

    @Test
    fun `tokenize Should return string list cleaned When called with unformatted string`() {
        val source = "value1\n, value2\n, value3"

        val expected = listOf("value1", "value2", "value3")

        val list = source.tokenize(",")

        assertEquals(expected, list)
    }

    @Test
    fun `tokenizeCodeLine Should return string list cleaned When called with unformatted string`() {
        val source = "const val value1: String\nvar value2 \nval value3"

        val expected = listOf("value1", "value2", "value3")

        val list = source.tokenizeCodeLine()

        assertEquals(expected, list)
    }

    @Test
    fun `removeComments Should return string with comments When called with comments`() {
        val source = "value1 // value 2"

        val expected = "value1"

        val cleaned = source.removeComments()

        assertEquals(expected, cleaned)
    }

    @Test
    fun `containsVersionNumber Should return true When called with string that contains version`() {
        val source = "val myVersion = \"12.3.5\""

        assertTrue(source.containsVersionNumber())
    }

    @Test
    fun `getVarValueFromVersionsFileLines Should return value of variable with version`() {
        val lines = listOf(
            "const val libraryVersion1 = \"1.1.1\" // any comment here",
            "const val libraryVersion2 = \"2.2.2\" // any comment here",
            "const val libraryVersion3 = \"3.3.3\" // any comment here",
            "const val libraryVersion4 = \"4.4.4\" // any comment here",
            "const val libraryVersion5 = \"5.5.5\" // any comment here"
        )

        val expected = "\"3.3.3\""

        val extracted = lines.getVarValueFromVersionsFileLines("libraryVersion3")

        assertEquals(expected, extracted)
    }
}
