package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.login.LoginOverlay
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.expect

abstract class AbstractLoginFormTests() {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Test fun `login event`() {
        var loginCalled = false
        LoginOverlay().apply {
            addLoginListener {
                expect("u") { it.username }
                expect("p") { it.password }
                loginCalled = true
            }
            isOpened = true
        }
        _get<LoginOverlay>()._login("u", "p")
        expect(true) { loginCalled }
    }

    @Test fun `forgot password event`() {
        var forgotPasswordCalled = false
        LoginOverlay().apply {
            addForgotPasswordListener {
                forgotPasswordCalled = true
            }
            isOpened = true
        }
        _get<LoginOverlay>()._forgotPassword()
        expect(true) { forgotPasswordCalled }
    }
}
