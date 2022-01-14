package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.vaadin.flow.component.login.LoginOverlay
import kotlin.test.expect

@DynaTestDsl
fun DynaNodeGroup.loginFormTestbatch() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("login event") {
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

    test("forgot password event") {
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
