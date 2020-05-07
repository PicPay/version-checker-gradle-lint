package com.picpay.gradlelint.versioncheck.cleaner

internal fun String.tokenize(delimiter: String = " "): List<String> {
    return this.split(delimiter)
        .map { it.replace("\n", "").trim() }
}

internal fun String.removeComments(): String {
    return if (contains("//")) {
        split("//")[0].trim()
    } else {
        this
    }
}