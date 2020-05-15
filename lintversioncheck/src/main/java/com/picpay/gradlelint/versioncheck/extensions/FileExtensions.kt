package com.picpay.gradlelint.versioncheck.extensions

import java.io.File

private const val BUILD_SRC_MODULE = "buildSrc"

internal fun File.findBuildSrcFromProjectDir(buildSrcModuleName: String = BUILD_SRC_MODULE): File? {
    var dir: String? = parentFile?.absolutePath
    while (dir != null) {
        val currentDir = File(dir)

        val containsBuildSrc = currentDir.listFiles()
            ?.asList()
            ?.any { it.name == buildSrcModuleName }
            ?: false

        if (containsBuildSrc) {
            return File(currentDir.absolutePath, buildSrcModuleName)
        } else {
            dir = currentDir.parentFile?.absolutePath
        }
    }
    return null
}
