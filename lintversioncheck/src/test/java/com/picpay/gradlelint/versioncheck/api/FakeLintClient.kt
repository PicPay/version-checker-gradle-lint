package com.picpay.gradlelint.versioncheck.api

import com.android.tools.lint.client.api.GradleVisitor
import com.android.tools.lint.client.api.LintClient
import com.android.tools.lint.client.api.UastParser
import com.android.tools.lint.client.api.XmlParser
import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.GradleContext
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Location
import com.android.tools.lint.detector.api.Project
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.TextFormat
import com.picpay.gradlelint.versioncheck.helpers.createURLFromResourceFile
import java.io.File
import java.net.URL
import java.net.URLConnection

@Suppress("UnstableApiUsage")
internal class FakeLintClient(private val fileToCreateURL: String = "") : LintClient() {

    private val receivedMessages = mutableListOf<String>()
    val messagesFromContext: List<String> = receivedMessages

    override val xmlParser: XmlParser
        get() = TODO("Not yet implemented")

    override fun openConnection(url: URL): URLConnection? {
        var urlToConnection = url
        if (fileToCreateURL.isNotEmpty()) {
            urlToConnection = createURLFromResourceFile(fileToCreateURL)
        }
        return super.openConnection(urlToConnection)
    }

    override fun getGradleVisitor() = FakeGradleVisitor()

    override fun getUastParser(project: Project?): UastParser {
        TODO("Not yet implemented")
    }

    override fun log(
        severity: Severity,
        exception: Throwable?,
        format: String?,
        vararg args: Any
    ) = Unit

    override fun readFile(file: File): CharSequence = ""

    override fun report(
        context: Context,
        issue: Issue,
        severity: Severity,
        location: Location,
        message: String,
        format: TextFormat,
        fix: LintFix?
    ) {
        receivedMessages.add(message)
    }

    inner class FakeGradleVisitor : GradleVisitor() {

        override fun createLocation(context: GradleContext, cookie: Any): Location {
            return Location.Companion.create(File(""))
        }
    }

}