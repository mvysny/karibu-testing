package com.github.mvysny.kaributesting.v10

import com.github.mvysny.karibudsl.v10.anchor
import com.github.mvysny.karibudsl.v10.image
import com.vaadin.flow.component.UI
import com.vaadin.flow.server.InputStreamFactory
import com.vaadin.flow.server.StreamResource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
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
}
