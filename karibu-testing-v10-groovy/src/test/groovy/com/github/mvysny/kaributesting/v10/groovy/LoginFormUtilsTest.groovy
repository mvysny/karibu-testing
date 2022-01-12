package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.MockVaadin
import com.vaadin.flow.component.login.LoginForm
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
class LoginFormUtilsTest {
    @BeforeEach void setup() {
        MockVaadin.setup()
    }
    @AfterEach void teardown() { MockVaadin.tearDown() }

    @Test
    void apiTest() {
        def loginForm = new LoginForm()
        loginForm._forgotPassword()
        loginForm._login("foo", "bar")
    }
}
