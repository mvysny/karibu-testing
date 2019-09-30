package com.github.mvysny.kaributesting.v8

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.vaadin.ui.UI
import com.vaadin.ui.Upload
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.test.expect
import kotlin.test.fail

/**
 * @author mavi
 */
class UploadTest : DynaTest({

    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("smoke") {
        UI.getCurrent().content = Upload()
        _expectOne<Upload>()
    }

    test("successful upload") {
        val upload = Upload()
        val fileContents = ByteArrayOutputStream()
        upload.receiver = Upload.Receiver { fileName, mimeType ->
            expect("hello.txt") { fileName }
            expect("text/plain") { mimeType }
            fileContents
        }
        var startedCalled = false
        var succeededCalled = false
        var finishedCalled = false
        upload.addFailedListener { throw RuntimeException("Should not be called", it.reason) }
        upload.addStartedListener {
            expect("hello.txt") { it.filename }
            expect("text/plain") { it.mimeType }
            expect(false) { succeededCalled }
            expect(false) { finishedCalled }
            startedCalled = true
        }
        upload.addSucceededListener {
            expect("hello.txt") { it.filename }
            expect("text/plain") { it.mimeType }
            expect(false) { finishedCalled }
            succeededCalled = true
        }
        upload.addFinishedListener {
            expect("hello.txt") { it.filename }
            expect("text/plain") { it.mimeType }
            finishedCalled = true
        }
        upload._upload("hello.txt", "text/plain", "Hello world!".toByteArray())
        expect(true) { startedCalled }
        expect(true) { succeededCalled }
        expect(true) { finishedCalled }
        expect("Hello world!") { fileContents.toString("UTF-8") }
    }

    test("unsuccessful upload") {
        val upload = Upload()
        upload.receiver = Upload.Receiver { fileName, mimeType ->
            expect("hello.txt") { fileName }
            expect("text/plain") { mimeType }
            throw IOException("simulated")
        }
        var startedCalled = false
        var failedCalled = false
        var finishedCalled = false
        upload.addFailedListener {
            expect("hello.txt") { it.filename }
            expect("text/plain") { it.mimeType }
            expect("java.io.IOException: simulated") { it.reason.toString() }
            failedCalled = true
        }
        upload.addStartedListener {
            expect("hello.txt") { it.filename }
            expect("text/plain") { it.mimeType }
            expect(false) { finishedCalled }
            expect(false) { failedCalled }
            startedCalled = true
        }
        upload.addSucceededListener { fail("shouldn't be called") }
        upload.addFinishedListener {
            expect("hello.txt") { it.filename }
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

    test("failed upload") {
        val upload = Upload()
        upload.receiver = Upload.Receiver { fileName, mimeType ->
            expect("hello.txt") { fileName }
            expect("text/plain") { mimeType }
            ByteArrayOutputStream()
        }
        var startedCalled = false
        var failedCalled = false
        var finishedCalled = false
        upload.addFailedListener {
            expect("hello.txt") { it.filename }
            expect("text/plain") { it.mimeType }
            expect(null) { it.reason }
            failedCalled = true
        }
        upload.addStartedListener {
            expect("hello.txt") { it.filename }
            expect("text/plain") { it.mimeType }
            expect(false) { finishedCalled }
            expect(false) { failedCalled }
            startedCalled = true
        }
        upload.addSucceededListener { fail("shouldn't be called") }
        upload.addFinishedListener {
            expect("hello.txt") { it.filename }
            expect("text/plain") { it.mimeType }
            finishedCalled = true
        }
        upload._uploadFail("hello.txt")
        expect(true) { startedCalled }
        expect(true) { failedCalled }
        expect(true) { finishedCalled }
    }
})
