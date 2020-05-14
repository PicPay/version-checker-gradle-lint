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
}