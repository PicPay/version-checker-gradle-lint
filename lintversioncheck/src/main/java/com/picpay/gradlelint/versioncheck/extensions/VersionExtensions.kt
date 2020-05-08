package com.picpay.gradlelint.versioncheck.extensions

import java.util.Locale

internal fun List<String>.firstReleaseVersion(enablePreReleaseCheck: Boolean = false): String? {
    return firstOrNull { it.trim().isVersionNumber(enablePreReleaseCheck) }
}

internal fun String.isVersionNumber(enablePreReleaseCheck: Boolean = false): Boolean {
    val isRelease = matches("([0-9]+[.][0-9]+)".toRegex()) ||
            matches("([0-9]+[.][0-9]+[.][0-9]+)".toRegex())
    return isRelease || (enablePreReleaseCheck && isVersionPreRelease())
}

internal fun String.isVersionPreRelease(): Boolean {
    return this.toLowerCase(Locale.getDefault())
        .contains("-alpha") || contains("-rc") || contains("-beta")
}

internal fun String.getVarNameInVersionDeclaration(): String {
    return replace("{", "")
        .replace("}", "")
        .replace("\"", "")
        .split(".")[1]
        .trim()
}
