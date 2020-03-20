package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.UploadKt
import com.vaadin.flow.component.upload.Upload
import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull

/**
 * @author mavi
 */
@CompileStatic
class UploadUtils {
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
    static void _upload(@NotNull Upload self, @NotNull String fileName,
                        @NotNull String mimeType = URLConnection.guessContentTypeFromName(fileName),
                        @NotNull byte[] file) {
        UploadKt._upload(self, fileName, mimeType, file)
    }

    /**
     * Tests the "upload interrupted" scenario. First invokes [StartedEvent], then polls [Upload.receiver], then
     * fires [FailedEvent] and [FinishedEvent].
     *
     * Currently the implementation simply calls [_uploadFail].
     */
    static void _uploadInterrupt(@NotNull Upload self, @NotNull String fileName,
                                 @NotNull String mimeType = URLConnection.guessContentTypeFromName(fileName)) {
        UploadKt._uploadInterrupt(self, fileName, mimeType)
    }

    /**
     * Tests the "upload failed" scenario. First invokes [StartedEvent], then polls
     * [Upload.receiver] and closes it immediately without writing anything, then
     * fires [FailedEvent] and [FinishedEvent].
     */
    static void _uploadFail(@NotNull Upload self, @NotNull String fileName,
                            @NotNull String mimeType = URLConnection.guessContentTypeFromName(fileName)) {
        UploadKt._uploadFail(self, fileName, mimeType)
    }
}
