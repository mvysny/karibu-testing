@file:Suppress("FunctionName")

package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.upload.*
import com.vaadin.flow.internal.streams.UploadCompleteEvent
import com.vaadin.flow.internal.streams.UploadStartEvent
import com.vaadin.flow.server.StreamResourceRegistry
import com.vaadin.flow.server.streams.UploadEvent
import com.vaadin.flow.server.streams.UploadHandler
import com.vaadin.flow.server.streams.UploadResult
import java.net.URI

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
@Suppress("DEPRECATION")
@JvmOverloads
public fun Upload._upload(
    fileName: String,
    mimeType: String = currentService.getMimeType(fileName) ?: "application/octet-stream",
    file: ByteArray
) {
    _expectEditableByUser()
    val r = receiver
    if (r != null) {
        _uploadLegacy(fileName, mimeType, file)
    } else {
        // Vaadin 24.8 UploadHandler
        _uploadNew(fileName, mimeType, file)
        // events are fired asynchronously
        MockVaadin.clientRoundtrip()
    }
}

/**
 * Returns the [UploadHandler] set to this [Upload] via [Upload.setUploadHandler]. Fails if no [UploadHandler]
 * has been set.
 *
 * Technical limitation: The [Upload] component must be attached to the UI otherwise
 * it's impossible to retrieve the [UploadHandler]. Explanation: the `target` attribute
 * is only set after Upload is attached.
 */
public val Upload._handler: UploadHandler get() {
    require(isAttached) { "Karibu-Testing can't retrieve UploadHandler unless the Upload component is attached to the UI" }
    var target = element.getAttribute("target")
    if (target == null) {
        MockVaadin.clientRoundtrip()
        target = element.getAttribute("target")
    }
    checkNotNull(target) { "${toPrettyString()}: upload handler has not been set" }
    val res = currentSession.resourceRegistry.getResource(URI(target))
    val resource = res.get() as StreamResourceRegistry.ElementStreamResource
    val handler = resource.elementRequestHandler
    return handler as UploadHandler
}

private fun Upload._uploadNew(fileName: String, mimeType: String, file: ByteArray) {
    val handler = _handler
    val req = MockVaadin.createVaadinRequest()
    req.fake.content = file
    val res = MockVaadin.createVaadinResponse()
    val event = UploadEvent(req, res, currentSession, fileName, file.size.toLong(), mimeType, element, null)
    try {
        _fireEvent(UploadStartEvent(this))
        try {
            handler.handleUploadRequest(event)
        } finally {
            _fireEvent(UploadCompleteEvent(this))
        }
        handler.responseHandled(UploadResult(true, res))
        // emulate client-side Upload which fires this DOM event.
        _fireEvent(AllFinishedEvent(this))
    } catch (e: Exception) {
        handler.responseHandled(UploadResult(false, res, e))
        throw e
    }
}

private fun Upload._uploadLegacy(fileName: String, mimeType: String, file: ByteArray) {
    _fireEvent(StartedEvent(this, fileName, mimeType, file.size.toLong()))
    val failure: Exception? = try {
        val r: Receiver =
            checkNotNull(receiver) { "${toPrettyString()}: receiver has not been set" }
        r.receiveUpload(fileName, mimeType).use { sout -> sout.write(file) }
        null
    } catch (e: Exception) {
        e
    }
    if (failure == null) {
        _fireEvent(SucceededEvent(this, fileName, mimeType, file.size.toLong()))
    } else {
        _fireEvent(
            FailedEvent(
                this,
                fileName,
                mimeType,
                file.size.toLong(),
                failure
            )
        )
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
public fun Upload._uploadInterrupt(
    fileName: String,
    mimeType: String = currentService.getMimeType(fileName)
) {
    _uploadFail(fileName, mimeType)
}

/**
 * Tests the "upload failed" scenario. First invokes [StartedEvent], then polls
 * [Upload.receiver] and closes it immediately without writing anything, then
 * fires [FailedEvent] with an [exception] as a reason and then [FinishedEvent].
 */
@Suppress("DEPRECATION")
@JvmOverloads
public fun Upload._uploadFail(
    fileName: String,
    mimeType: String = currentService.getMimeType(fileName),
    exception: Exception? = null
) {
    _expectEditableByUser()
    _fireEvent(StartedEvent(this, fileName, mimeType, 100L))
    receiver.receiveUpload(fileName, mimeType).close()
    _fireEvent(FailedEvent(this, fileName, mimeType, 0L, exception))
    _fireEvent(FinishedEvent(this, fileName, mimeType, 0L))
}
