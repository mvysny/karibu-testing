@file:Suppress("FunctionName")

package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.upload.*
import java.net.URLConnection

/**
 * Invokes [StartedEvent], then feeds given [file] to the [Upload.receiver], then
 * invokes the [SucceededEvent] and [FinishedEvent] listeners.
 * Doesn't call [com.vaadin.flow.component.upload.ProgressListener].
 *
 * If writing to the receiver fails, [FailedEvent] is invoked instead of [SucceededEvent], then
 * the exception is re-thrown from this function, so that the test fails properly.
 *
 * Limitations:
 * * The upload button is not automatically disabled in mocked environment after the max upload file
 *   is reached.
 * * The upload "runs" in the UI thread, so there is no way to test thread safety.
 */
@JvmOverloads
public fun Upload._upload(fileName: String, mimeType: String = URLConnection.guessContentTypeFromName(fileName), file: ByteArray) {
    checkEditableByUser()
    _fireEvent(StartedEvent(this, fileName, mimeType, file.size.toLong()))
    val failure: Exception? = try {
        val r: Receiver = checkNotNull(receiver) { "${toPrettyString()}: receiver has not been set" }
        r.receiveUpload(fileName, mimeType).use { sout -> sout.write(file) }
        null
    } catch (e: Exception) {
        e
    }
    if (failure == null) {
        _fireEvent(SucceededEvent(this, fileName, mimeType, file.size.toLong()))
    } else {
        _fireEvent(FailedEvent(this, fileName, mimeType, file.size.toLong(), failure))
    }
    _fireEvent(FinishedEvent(this, fileName, mimeType, file.size.toLong()))
    if (failure != null) {
        throw failure
    }
}

/**
 * Tests the "upload interrupted" scenario. First invokes [StartedEvent], then polls [Upload.receiver], then
 * fires [FailedEvent] and [FinishedEvent].
 *
 * Currently the implementation simply calls [_uploadFail].
 */
@JvmOverloads
public fun Upload._uploadInterrupt(fileName: String, mimeType: String = URLConnection.guessContentTypeFromName(fileName)) {
    _uploadFail(fileName, mimeType)
}

/**
 * Tests the "upload failed" scenario. First invokes [StartedEvent], then polls
 * [Upload.receiver] and closes it immediately without writing anything, then
 * fires [FailedEvent] with an [exception] as a reason and then [FinishedEvent].
 */
@JvmOverloads
public fun Upload._uploadFail(fileName: String, mimeType: String = URLConnection.guessContentTypeFromName(fileName), exception: Exception? = null) {
    checkEditableByUser()
    _fireEvent(StartedEvent(this, fileName, mimeType, 100L))
    receiver.receiveUpload(fileName, mimeType).use {  }
    _fireEvent(FailedEvent(this, fileName, mimeType, 0L, exception))
    _fireEvent(FinishedEvent(this, fileName, mimeType, 0L))
}
