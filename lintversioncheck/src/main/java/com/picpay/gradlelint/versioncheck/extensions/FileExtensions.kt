package com.picpay.gradlelint.versioncheck.extensions

import java.io.File

private const val BUILD_SRC_MODULE = "buildSrc"

internal fun File.findBuildSrcFromProjectDir(buildSrcModuleName: String = BUILD_SRC_MODULE): File? {
    var dir: String? = parentFile?.absolutePath
    while (dir != null) {
        val currentDir = File(dir)

        val containsBuildSrc = currentDir.listFiles()
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

internal fun File.findKotlinFilesWithSuffix(suffix: List<String>): Map<String, File> {
    val kotlinFilesMap = mutableMapOf<String, File>()
    return if (!this.isDirectory) emptyMap()
    else {
        listFiles()?.forEach { file ->
            if (file.isDirectory) {
                kotlinFilesMap.putAll(file.findKotlinFilesWithSuffix(suffix))
            } else if (file.isFile && suffix.any { file.name.endsWith("$it.kt") }) {
                kotlinFilesMap[file.nameWithoutExtension] = file
            }
        }
        kotlinFilesMap
    }
}
