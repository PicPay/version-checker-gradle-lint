package com.picpay.gradlelint.versioncheck

internal data class Library(
    val groupId: String,
    val artifactId: String,
    val version: String
) {
    override fun toString(): String {
        return "$groupId:$artifactId:$version"
    }
}

internal fun String.toLibrary(): Library {
    val definition = this.split(":")
    return Library(
        groupId = definition[0],
        artifactId = definition[1],
        version = definition[2]
    )
}
