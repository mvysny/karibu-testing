package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.MockVaadin
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.html.Image
import com.vaadin.flow.server.InputStreamFactory
import com.vaadin.flow.server.StreamResource
import groovy.transform.CompileStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Merely a test of the API. No need to test the implementation, it is thoroughly tested elsewhere.
 * @author mavi
 */
@CompileStatic
class DownloadUtilsTest {
    @BeforeEach void setup() { MockVaadin.setup() }
    @AfterEach void teardown() { MockVaadin.tearDown() }

    @Test
    void apiTest() {
        def anchor = new Anchor()
        UI.current.add(anchor)
        anchor.setHref(new StreamResource("foo", new InputStreamFactory() {
            @Override
            InputStream createInputStream() {
                return new ByteArrayInputStream(new byte[0])
            }
        }))
        anchor._download()
        anchor.download()

        def image = new Image()
        image.setSrc(new StreamResource("foo", new InputStreamFactory() {
            @Override
            InputStream createInputStream() {
                return new ByteArrayInputStream(new byte[0])
            }
        }))
        UI.current.add(image)
        image.download()
    }
}
