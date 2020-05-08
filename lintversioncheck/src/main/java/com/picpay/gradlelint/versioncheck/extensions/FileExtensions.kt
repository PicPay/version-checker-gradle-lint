package com.picpay.gradlelint.versioncheck.extensions

import java.io.File

private const val BUILD_SRC_MODULE = "buildSrc"

internal fun File.findBuildSrcFromProjectDir(): File? {
    var dir: String? = parentFile?.absolutePath
    while (dir != null) {
        val currentDir = File(dir)

        val containsBuildSrc = currentDir.listFiles()
            ?.asList()
            ?.any { it.name == BUILD_SRC_MODULE }
            ?: false

        if (containsBuildSrc) {
            return File(currentDir.absolutePath, BUILD_SRC_MODULE)
        } else {
            dir = currentDir.parentFile?.absolutePath
        }
    }
    return null
}

internal fun File.getVarValueFromVersionsFile(versionVar: String): String {
    readLines().forEach { line ->
        if (line.contains(versionVar) && line.containsVersionNumber()) {
            return line.tokenize("=")
                .map { it.removeComments() }
                .last()
        }
    }
    throw IllegalArgumentException(
        "Version with name $versionVar not found in file" +
                " ${absolutePath}."
    )
}