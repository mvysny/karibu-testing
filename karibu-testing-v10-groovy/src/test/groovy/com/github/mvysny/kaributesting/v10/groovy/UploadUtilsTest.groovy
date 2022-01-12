package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.MockVaadin
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.upload.Receiver
import com.vaadin.flow.component.upload.Upload
import groovy.transform.CompileStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Merely a test of the API. No need to test the implementation, it is thoroughly tested elsewhere.
 * @author mavi
 */
@CompileStatic
class UploadUtilsTest {
    @BeforeEach void setup() {
        MockVaadin.setup()
    }
    @AfterEach void teardown() { MockVaadin.tearDown() }

    @Test
    void apiTest() {
        def upload = new Upload()
        upload.receiver = new Receiver() {
            @Override
            OutputStream receiveUpload(String fileName, String mimeType) {
                return new ByteArrayOutputStream()
            }
        }
        upload._upload("foo.txt", new byte[0])
        upload._uploadFail("foo.txt")
        upload._uploadInterrupt("foo.txt")
    }
}
