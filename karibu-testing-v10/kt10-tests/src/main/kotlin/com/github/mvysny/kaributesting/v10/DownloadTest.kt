package com.github.mvysny.kaributesting.v10

import com.github.mvysny.karibudsl.v10.anchor
import com.github.mvysny.karibudsl.v10.image
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.html.AttachmentType
import com.vaadin.flow.server.InputStreamFactory
import com.vaadin.flow.server.StreamResource
import com.vaadin.flow.server.streams.DownloadHandler
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
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
        }
        @Nested inner class image {
            @Test fun `file-handler-simple`() {
                val link = UI.getCurrent().image()
                link.setSrc(DownloadHandler.forFile(tempFile))
                expect("Hello!") { link.download().toString(Charsets.UTF_8) }
            }
        }

        // @todo mavi also test transfer progress and that handler listener methods have been called (e.g. error)
    }
}
