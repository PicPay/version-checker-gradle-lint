package com.picpay.gradlelint.versioncheck.extensions

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

internal fun String.containsVersionNumber(): Boolean {
    val tokenized = tokenize("=")
    return if (tokenized.size > 1) {
        tokenized.last()
            .replace("\"", "")
            .isVersionNumber(enablePreReleaseCheck = true)
    } else {
        false
    }
}

