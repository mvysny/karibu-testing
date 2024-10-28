package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.expectThrows
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.upload.Receiver
import com.vaadin.flow.component.upload.Upload
import com.vaadin.flow.component.upload.receivers.MemoryBuffer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.test.expect
import kotlin.test.fail

abstract class AbstractUploadTests() {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Test fun smoke() {
        UI.getCurrent().add(Upload())
        _expectOne<Upload>()
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
}
