package com.picpay.gradlelint.versioncheck

fun String.getContentByTagName(tag: String): String {
    val xml = this.replace("\n", "")
        .replace("\t", "")
        .replace("\r", "")
        .trimStart()
        .trimEnd()

    val tagIndex = xml.indexOf("<$tag versions")
    return xml.substring(tagIndex).split("/").first().split("=")[1]
}