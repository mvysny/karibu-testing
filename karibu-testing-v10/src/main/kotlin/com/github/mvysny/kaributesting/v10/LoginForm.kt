package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.login.AbstractLogin
import kotlin.test.expect

/**
 * Fires the [AbstractLogin.LoginEvent].
 */
public fun AbstractLogin._login(username: String, password: String) {
    _expectEditableByUser()
    _fireEvent(AbstractLogin.LoginEvent(this, true, username, password))
}

/**
 * Fires the [AbstractLogin.ForgotPasswordEvent].
 */
public fun AbstractLogin._forgotPassword() {
    _expectEditableByUser()
    expect(
        true,
        "isForgotPasswordButtonVisible is false"
    ) { isForgotPasswordButtonVisible }
    _fireEvent(AbstractLogin.ForgotPasswordEvent(this, true))
}
