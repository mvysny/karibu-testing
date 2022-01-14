package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.anchor
import com.github.mvysny.karibudsl.v10.image
import com.vaadin.flow.component.UI
import com.vaadin.flow.server.InputStreamFactory
import com.vaadin.flow.server.StreamResource
import kotlin.test.expect

@DynaTestDsl
fun DynaNodeGroup.downloadTestBattery() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    group("link") {
        test("simple") {
            val link = UI.getCurrent().anchor("")
            val streamRes = StreamResource("foo.txt", InputStreamFactory { "yadda".byteInputStream() })
            link.setHref(streamRes)
            expect("yadda") { link._download().toString(Charsets.UTF_8) }
        }

        test("no href") {
            val link = UI.getCurrent().anchor("")
            expectThrows(AssertionError::class, "href hasn't been set for Anchor[href='']") {
                link._download()
            }
        }
    }

    group("image") {
        test("simple") {
            val link = UI.getCurrent().image("")
            val streamRes = StreamResource("foo.txt", InputStreamFactory { "yadda".byteInputStream() })
            link.setSrc(streamRes)
            expect("yadda") { link.download().toString(Charsets.UTF_8) }
        }
        test("no src") {
            val link = UI.getCurrent().image("")
            expectThrows(AssertionError::class, "src hasn't been set for Image[]") {
                link.download()
            }
        }
    }

    group("downloadResource") {
        test("fails if there is no such resource") {
            expectThrows(AssertionError::class, "No such StreamResource registered: 'foo'. Available resources: []") {
                downloadResource("foo")
            }
        }
        test("fails and lists resources if there is no such resource") {
            val link = UI.getCurrent().anchor("")
            val streamRes = StreamResource("foo.txt", InputStreamFactory { "yadda".byteInputStream() })
            link.setHref(streamRes)
            expectThrows(AssertionError::class, "No such StreamResource registered: 'foo'. Available resources: [VAADIN/dynamic/resource/1/") {
                downloadResource("foo")
            }
        }
    }
}
