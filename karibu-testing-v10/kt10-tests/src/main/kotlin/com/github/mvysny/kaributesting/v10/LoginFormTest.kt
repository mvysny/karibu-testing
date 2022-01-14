package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeDsl
import com.github.mvysny.dynatest.DynaNodeGroup
import com.vaadin.flow.component.login.LoginOverlay
import kotlin.test.expect

@DynaNodeDsl
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
