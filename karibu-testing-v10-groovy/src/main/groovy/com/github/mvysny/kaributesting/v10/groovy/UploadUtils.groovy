package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.UploadKt
import com.vaadin.flow.component.upload.Upload
import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull

/**
 * {@link Upload}-related utilities.
 * @author mavi
 */
@CompileStatic
class UploadUtils {
    /**
     * Invokes {@link com.vaadin.flow.component.upload.StartedEvent}, then feeds given
     * file to the {@link Upload#getReceiver()}, then
     * invokes the {@link com.vaadin.flow.component.upload.SucceededEvent} and
     * {@link com.vaadin.flow.component.upload.FinishedEvent} listeners.
     * Doesn't call {@link com.vaadin.flow.component.upload.ProgressListener}.
     * <p></p>
     * If writing to the receiver fails, {@link com.vaadin.flow.component.upload.FailedEvent}
     * is invoked instead of {@link com.vaadin.flow.component.upload.SucceededEvent}, then
     * the exception is re-thrown from this function, so that the test fails properly.
     * <p></p>
     * Limitations:
     * <ul>
     * <li>The upload button is not automatically disabled in mocked environment after the max upload file
     *   is reached.</li>
     * <li>The upload "runs" in the UI thread, so there is no way to test thread safety.</li>
     * </ul>
     */
    static void _upload(@NotNull Upload self, @NotNull String fileName,
                        @NotNull String mimeType = URLConnection.guessContentTypeFromName(fileName),
                        @NotNull byte[] file) {
        UploadKt._upload(self, fileName, mimeType, file)
    }

    /**
     * Tests the "upload interrupted" scenario. First invokes
     * {@link com.vaadin.flow.component.upload.StartedEvent}, then polls {@link Upload#getReceiver()}, then
     * fires {@link com.vaadin.flow.component.upload.FailedEvent} and {@link com.vaadin.flow.component.upload.FinishedEvent}.
     * <p></p>
     * Currently the implementation simply calls {@link #_uploadFail}.
     */
    static void _uploadInterrupt(@NotNull Upload self, @NotNull String fileName,
                                 @NotNull String mimeType = URLConnection.guessContentTypeFromName(fileName)) {
        UploadKt._uploadInterrupt(self, fileName, mimeType)
    }

    /**
     * Tests the "upload failed" scenario. First invokes
     * {@link com.vaadin.flow.component.upload.StartedEvent}, then polls
     * {@link Upload#getReceiver()} and closes it immediately without writing anything, then
     * fires {@link com.vaadin.flow.component.upload.FailedEvent} and {@link com.vaadin.flow.component.upload.FinishedEvent}.
     */
    static void _uploadFail(@NotNull Upload self, @NotNull String fileName,
                            @NotNull String mimeType = URLConnection.guessContentTypeFromName(fileName)) {
        UploadKt._uploadFail(self, fileName, mimeType)
    }
}
