package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.DownloadKt
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.html.Image
import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull

/**
 * Download utilities for {@link Anchor}.
 * @author mavi
 */
@CompileStatic
class DownloadUtils {
    /**
     * Downloads contents of this link. Only works with [StreamResource]; all other resources
     * are rejected with [AssertionError].
     * @throws IllegalStateException if the link was not visible, not enabled. See [checkEditableByUser] for
     * more details.
     */
    @NotNull
    static byte[] _download(@NotNull Anchor self) {
        DownloadKt._download(self)
    }

    /**
     * Downloads contents of this link. Only works with [StreamResource]; all other resources
     * are rejected with [AssertionError].
     */
    @NotNull
    static byte[] download(@NotNull Anchor self) {
        DownloadKt.download(self)
    }

    /**
     * Downloads contents of this image. Only works with [StreamResource]; all other resources
     * are rejected with [AssertionError].
     */
    @NotNull
    static byte[] download(@NotNull Image self) {
        DownloadKt.download(self)
    }
}
