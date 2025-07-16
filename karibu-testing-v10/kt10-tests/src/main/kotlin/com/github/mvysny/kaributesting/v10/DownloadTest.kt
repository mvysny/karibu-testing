@file:Suppress("DEPRECATION")

package com.github.mvysny.kaributesting.v10

import com.github.mvysny.karibudsl.v10.anchor
import com.github.mvysny.karibudsl.v10.image
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.html.AttachmentType
import com.vaadin.flow.server.InputStreamFactory
import com.vaadin.flow.server.StreamResource
import com.vaadin.flow.server.streams.DownloadHandler
import com.vaadin.flow.server.streams.DownloadResponse
import com.vaadin.flow.server.streams.InputStreamDownloadCallback
import com.vaadin.flow.server.streams.InputStreamDownloadHandler
import com.vaadin.flow.server.streams.TransferContext
import com.vaadin.flow.server.streams.TransferProgressListener
import org.apache.commons.io.input.BrokenInputStream
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import kotlin.test.expect

abstract class AbstractDownloadTests() {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Nested inner class link {
        @Test fun simple() {
            val link = UI.getCurrent().anchor("")
            val streamRes = StreamResource("foo.txt", InputStreamFactory { "yadda".byteInputStream() })
            link.setHref(streamRes)
            expect("yadda") { link._download().toString(Charsets.UTF_8) }
        }

        @Test fun `no href`() {
            val link = UI.getCurrent().anchor("")
            expectThrows(AssertionError::class, "href hasn't been set for Anchor[href='']") {
                link._download()
            }
        }
    }

    @Nested inner class image {
        @Test fun simple() {
            val link = UI.getCurrent().image("")
            val streamRes = StreamResource("foo.txt", InputStreamFactory { "yadda".byteInputStream() })
            link.setSrc(streamRes)
            expect("yadda") { link.download().toString(Charsets.UTF_8) }
        }
        @Test fun `no src`() {
            val link = UI.getCurrent().image("")
            expectThrows(AssertionError::class, "src hasn't been set for Image[]") {
                link.download()
            }
        }
    }

    @Nested inner class downloadResource {
        @Test fun `fails if there is no such resource`() {
            expectThrows(AssertionError::class, "No such StreamResource registered: 'foo'. Available resources: []") {
                downloadResource("foo")
            }
        }
        @Test fun `fails and lists resources if there is no such resource`() {
            val link = UI.getCurrent().anchor("")
            val streamRes = StreamResource("foo.txt", InputStreamFactory { "yadda".byteInputStream() })
            link.setHref(streamRes)
            expectThrows(AssertionError::class, "No such StreamResource registered: 'foo'. Available resources: [VAADIN/dynamic/resource/1/") {
                downloadResource("foo")
            }
        }
    }

    /**
     * The new Vaadin 24.8 API
     */
    @Nested inner class handlers {
        @TempDir lateinit var tempDir: File
        lateinit var tempFile: File
        @BeforeEach fun createTempFile() {
            tempFile = File(tempDir, "foo.txt")
            tempFile.writeText("Hello!")
        }

        @Nested inner class anchor {
            @Test fun `file-handler-simple`() {
                val link = UI.getCurrent().anchor("")
                link.setHref(DownloadHandler.forFile(tempFile), AttachmentType.DOWNLOAD)
                expect("Hello!") { link._download().toString(Charsets.UTF_8) }
            }
            @Test fun `file-handler-simple-inline`() {
                val link = UI.getCurrent().anchor("")
                link.setHref(DownloadHandler.forFile(tempFile), AttachmentType.INLINE)
                expect("Hello!") { link._download().toString(Charsets.UTF_8) }
            }
            @Test fun `in-memory-handler-simple`() {
                val link = UI.getCurrent().anchor("")
                link.setHref(DownloadHandler("Hello!".toByteArray(), "greeting.txt"), AttachmentType.DOWNLOAD)
                expect("Hello!") { link._download().toString(Charsets.UTF_8) }
            }
        }
        @Nested inner class image {
            @Test fun `file-handler-simple`() {
                val link = UI.getCurrent().image()
                link.setSrc(DownloadHandler.forFile(tempFile))
                expect("Hello!") { link.download().toString(Charsets.UTF_8) }
            }
            @Test fun `in-memory-handler-simple`() {
                val link = UI.getCurrent().image()
                link.setSrc(DownloadHandler("Hello!".toByteArray(), "greeting.txt"))
                expect("Hello!") { link.download().toString(Charsets.UTF_8) }
            }
        }

        @Test fun `progress-notifications`() {
            val link = UI.getCurrent().anchor("")
            val progress = TestTransferProgressListener()
            val h = DownloadHandler("Hello!".toByteArray(), "greeting.txt", progressListener = progress)
            link.setHref(h, AttachmentType.DOWNLOAD)
            expect(false) { progress.started }
            expect(false) { progress.progressReported }
            expect(null) { progress.error }
            expect(null) { progress.completedBytes }

            expect("Hello!") { link.download().toString(Charsets.UTF_8) }
            // notifications happen asynchronously
            MockVaadin.clientRoundtrip()

            expect(true) { progress.started }
            expect(true) { progress.progressReported }
            expect(null) { progress.error }
            expect(6L) { progress.completedBytes }
        }
    }

    @Test fun `progress-notifications-error`() {
        val link = UI.getCurrent().anchor("")
        val progress = TestTransferProgressListener()
        val h = failingDownloadHandler("greeting.txt", progressListener = progress)
        link.setHref(h, AttachmentType.DOWNLOAD)
        expect(false) { progress.started }
        expect(false) { progress.progressReported }
        expect(null) { progress.error }
        expect(null) { progress.completedBytes }

        // exceptions are rethrown
        expectThrows<IOException> { link.download().toString(Charsets.UTF_8) }
        // notifications happen asynchronously
        MockVaadin.clientRoundtrip()

        expect(true) { progress.started }
        expect(false) { progress.progressReported }
        expect(IOException::class.java) { progress.error!!.javaClass }
        expect(null) { progress.completedBytes }
    }
}

/**
 * Tracks the status of the download and provides insight via [started],
 * [progressReported], [error] etc.
 * @property error not-null if an exception was thrown while reading from an input stream.
 */
class TestTransferProgressListener : TransferProgressListener {
    var started = false
    var progressReported = false
    var error: IOException? = null
    var completedBytes: Long? = null
    val completed: Boolean get() = completedBytes != null

    override fun onStart(context: TransferContext) {
        started = true
    }

    override fun onProgress(context: TransferContext, transferredBytes: Long, totalBytes: Long) {
        progressReported = true
    }

    override fun onError(
        context: TransferContext,
        reason: IOException
    ) {
        error = reason
    }

    override fun onComplete(
        context: TransferContext,
        transferredBytes: Long
    ) {
        completedBytes = transferredBytes
    }

    override fun progressReportInterval(): Long = 1L
}

/**
 * Creates an in-memory [DownloadHandler] producing given [bytes].
 */
fun DownloadHandler(
    bytes: ByteArray,
    fileName: String,
    contentType: String = currentService.getMimeType(fileName) ?: "application/octet-stream",
    progressListener: TransferProgressListener? = null
): InputStreamDownloadHandler {
    val callback = InputStreamDownloadCallback {
        DownloadResponse(
            bytes.inputStream(),
            fileName,
            contentType,
            bytes.size.toLong()
        )
    }
    return if (progressListener != null) {
        DownloadHandler.fromInputStream(callback, progressListener)
    } else {
        DownloadHandler.fromInputStream(callback)
    }
}

/**
 * Produces a [DownloadHandler] which always fails - never downloads anything successfully.
 */
fun failingDownloadHandler(
    fileName: String,
    contentType: String = currentService.getMimeType(fileName) ?: "application/octet-stream",
    progressListener: TransferProgressListener
): InputStreamDownloadHandler {
    val callback = InputStreamDownloadCallback {
        DownloadResponse(
            BrokenInputStream(),
            fileName,
            contentType,
            200L
        )
    }
    return DownloadHandler.fromInputStream(callback, progressListener)
}