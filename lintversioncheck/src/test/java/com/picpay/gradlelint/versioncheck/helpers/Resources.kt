package com.picpay.gradlelint.versioncheck.helpers

import java.net.URL

inline fun <reified T> T.createURLFromResourceFile(filename: String): URL {
    return T::class.java.classLoader.getResource(filename)!!
}

inline fun <reified T> T.readTextFromResourceFile(filename: String): String {
    return createURLFromResourceFile(filename).readText()
}