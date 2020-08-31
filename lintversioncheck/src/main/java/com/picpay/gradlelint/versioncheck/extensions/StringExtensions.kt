package com.picpay.gradlelint.versioncheck.extensions

internal fun String.tokenize(delimiter: String = " "): List<String> {
    return this.split(delimiter)
        .map { it.replace("\n", "").trim() }
}

internal fun String.tokenizeCodeLine(): List<String> {
    return this.replace("String","")
        .replace(":","")
        .replace("const ","")
        .replace("val ","")
        .replace("var ","")
        .replace("\n", "")
        .tokenize()
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

internal fun List<String>.getVarValueFromVersionsFileLines(versionVar: String): String {
    forEach { line ->
        if (line.contains(versionVar) && line.removeComments().containsVersionNumber()) {
            return line.tokenize("=")
                .map { it.removeComments() }
                .last()
        }
    }
    throw IllegalArgumentException("Version with name $versionVar not found in file")
}
