@file:Suppress("DEPRECATION")

package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.upload.Receiver
import com.vaadin.flow.component.upload.Upload
import com.vaadin.flow.component.upload.receivers.MemoryBuffer
import com.vaadin.flow.server.streams.*
import org.apache.commons.io.output.BrokenOutputStream
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import kotlin.test.expect
import kotlin.test.fail


abstract class AbstractUploadTests() {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Test fun smoke() {
        UI.getCurrent().add(Upload())
        _expectOne<Upload>()
    }

    @Test fun uploadButtonDiscoverable() {
        val upload = Upload()
        upload.uploadButton = Span("Hello!")
        UI.getCurrent().add(upload)
        _expectOne<Span> { text = "Hello!" }
    }

    @Test fun `successful upload`() {
        val upload = Upload()
        val memoryBuffer = MemoryBuffer()
        upload.receiver = memoryBuffer
        var startedCalled = false
        var succeededCalled = false
        var finishedCalled = false
        upload.addFailedListener { throw RuntimeException("Should not be called", it.reason) }
        upload.addStartedListener {
            expect("hello.txt") { it.fileName }
            expect("text/plain") { it.mimeType }
            expect(false) { succeededCalled }
            expect(false) { finishedCalled }
            startedCalled = true
        }
        upload.addSucceededListener {
            expect("hello.txt") { it.fileName }
            expect("text/plain") { it.mimeType }
            expect(false) { finishedCalled }
            succeededCalled = true
        }
        upload.addFinishedListener {
            expect("hello.txt") { it.fileName }
            expect("text/plain") { it.mimeType }
            finishedCalled = true
        }
        upload._upload("hello.txt", "text/plain", "Hello world!".toByteArray())
        expect(true) { startedCalled }
        expect(true) { succeededCalled }
        expect(true) { finishedCalled }
        expect("Hello world!") { memoryBuffer.inputStream.reader().readText() }
        expect("hello.txt") { memoryBuffer.fileName }
        expect("text/plain") { memoryBuffer.fileData.mimeType }
    }

    /**
     * Tests [_upload] for cases when the receiver throws an exception.
     */
    @Test fun `unsuccessful upload`() {
        val upload = Upload()
        upload.receiver = Receiver { fileName, mimeType ->
            expect("hello.txt") { fileName }
            expect("text/plain") { mimeType }
            throw IOException("simulated")
        }
        var startedCalled = false
        var failedCalled = false
        var finishedCalled = false
        upload.addFailedListener {
            expect("hello.txt") { it.fileName }
            expect("text/plain") { it.mimeType }
            expect("java.io.IOException: simulated") { it.reason.toString() }
            failedCalled = true
        }
        upload.addStartedListener {
            expect("hello.txt") { it.fileName }
            expect("text/plain") { it.mimeType }
            expect(false) { finishedCalled }
            expect(false) { failedCalled }
            startedCalled = true
        }
        upload.addSucceededListener { fail("shouldn't be called") }
        upload.addFinishedListener {
            expect("hello.txt") { it.fileName }
            expect("text/plain") { it.mimeType }
            finishedCalled = true
        }
        expectThrows(IOException::class) {
            upload._upload("hello.txt", "text/plain", "Hello world!".toByteArray())
        }
        expect(true) { startedCalled }
        expect(true) { failedCalled }
        expect(true) { finishedCalled }
    }

    /**
     * Tests [_uploadFail].
     */
    @Test fun `failed upload`() {
        val upload = Upload()
        upload.receiver = Receiver { fileName, mimeType ->
            expect("hello.txt") { fileName }
            expect("text/plain") { mimeType }
            ByteArrayOutputStream()
        }
        var startedCalled = false
        var failedCalled = false
        var finishedCalled = false
        val exception = RuntimeException("simulated")
        upload.addFailedListener {
            expect("hello.txt") { it.fileName }
            expect("text/plain") { it.mimeType }
            expect(exception) { it.reason }
            failedCalled = true
        }
        upload.addStartedListener {
            expect("hello.txt") { it.fileName }
            expect("text/plain") { it.mimeType }
            expect(false) { finishedCalled }
            expect(false) { failedCalled }
            startedCalled = true
        }
        upload.addSucceededListener { fail("shouldn't be called") }
        upload.addFinishedListener {
            expect("hello.txt") { it.fileName }
            expect("text/plain") { it.mimeType }
            finishedCalled = true
        }
        upload._uploadFail(
            fileName = "hello.txt",
            exception = exception
        )
        expect(true) { startedCalled }
        expect(true) { failedCalled }
        expect(true) { finishedCalled }
    }

    /**
     * The new Vaadin 24.8 [UploadHandler].
     */
    @Nested inner class handlers {
        @Test fun `successful upload`() {
            val upload = Upload()
            UI.getCurrent().add(upload)
            var successCalled = false
            upload.setUploadHandler(UploadHandler.inMemory { metadata, data ->
                successCalled = true
                expect("Hello!") { data.toString(Charsets.UTF_8) }
                expect("hello.txt") { metadata.fileName }
                expect("text/plain") { metadata.contentType }
                expect(6L) { metadata.contentLength }
            })
            upload._upload("hello.txt", file = "Hello!".toByteArray())
            expect(true) { successCalled }
        }

        @Test fun `when using UploadHandler, old listeners aren't called`() {
            val upload = Upload()
            UI.getCurrent().add(upload)
            var successCalled = false
            upload.setUploadHandler(UploadHandler.inMemory { metadata, data ->
                successCalled = true
                expect("Hello!") { data.toString(Charsets.UTF_8) }
                expect("hello.txt") { metadata.fileName }
                expect("text/plain") { metadata.contentType }
                expect(6L) { metadata.contentLength }
            })
            upload.addFailedListener { fail("shouldn't be called") }
            upload.addStartedListener { fail("shouldn't be called") }
            upload.addSucceededListener { fail("shouldn't be called") }
            upload.addFinishedListener { fail("shouldn't be called") }

            upload._upload("hello.txt", file = "Hello!".toByteArray())
            expect(true) { successCalled }
        }

        @Test fun events() {
            val upload = Upload()
            UI.getCurrent().add(upload)
            var allFinishedCalled = false
            upload.addAllFinishedListener { allFinishedCalled = true }
            upload.setUploadHandler(UploadHandler.inMemory { metadata, data -> })
            upload._upload("hello.txt", file = "Hello!".toByteArray())
            expect(true) { allFinishedCalled }
        }

        @Test fun `transfer progress monitoring`() {
            val upload = Upload()
            UI.getCurrent().add(upload)
            val tp = TestTransferProgressListener()
            upload.setUploadHandler(UploadHandler.inMemory({ metadata, data -> }, tp))
            upload._upload("hello.txt", file = "Hello!".toByteArray())
            expect(true) { tp.started }
            expect(6L) { tp.completedBytes }
            expect(true) { tp.progressReported }
            expect(null) { tp.error }
        }

        @Test fun `transfer progress monitoring on failure`() {
            val upload = Upload()
            UI.getCurrent().add(upload)
            val tp = TestTransferProgressListener()
            upload.setUploadHandler(OutputStreamUploadHandler.alwaysFail().withTransferProgressListener(tp))
            assertThrows<IOException> {
                upload._upload("hello.txt", file = "Hello!".toByteArray())
            }

            MockVaadin.clientRoundtrip()
            expect(true) { tp.started }
            expect(null) { tp.completedBytes }
            // OutputStreamUploadHandler.alwaysFail() fails to read even a single byte, so no progress is reported.
            expect(false) { tp.progressReported }
            expect(IOException::class.java) { tp.error!!.javaClass }
        }
    }
}

/**
 * Stores uploaded file to [out]. Calls [successCallback] when the transfer completes successfully.
 */
class OutputStreamUploadHandler(
    val out: OutputStream,
    val successCallback: (UploadMetadata) -> Unit
) : TransferProgressAwareHandler<UploadEvent, OutputStreamUploadHandler>(), UploadHandler {
    override fun getTransferContext(transferEvent: UploadEvent): TransferContext =
        TransferContext(
            transferEvent.request,
            transferEvent.response, transferEvent.session,
            transferEvent.fileName, transferEvent.owningElement,
            transferEvent.fileSize
        )

    override fun handleUploadRequest(event: UploadEvent) {
        try {
            event.inputStream.use { inputStream ->
                out.use { outputStream ->
                    TransferUtil.transfer(inputStream, outputStream, getTransferContext(event), listeners)
                }
            }
        } catch (e: IOException) {
            notifyError(event, e)
            throw e
        }
        event.getUI().access({
            successCallback(UploadMetadata(event.fileName, event.contentType, event.fileSize))
        })
    }

    fun withTransferProgressListener(listener: TransferProgressListener): OutputStreamUploadHandler = apply {
        addTransferProgressListener(listener)
    }

    companion object {
        /**
         * Produces OutputStreamUploadHandler which fails to read anything and always throws
         * [IOException]. Since this fails to read even a single byte, no progress is reported
         * to [TransferProgressListener].
         */
        fun alwaysFail() = OutputStreamUploadHandler(BrokenOutputStream()) { fail("shouldn't be called") }
    }
}
