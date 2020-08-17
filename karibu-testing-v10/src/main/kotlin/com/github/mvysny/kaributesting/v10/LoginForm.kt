package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.login.AbstractLogin
import kotlin.test.expect

/**
 * Fires the [AbstractLogin.LoginEvent].
 */
public fun AbstractLogin._login(username: String, password: String) {
    checkEditableByUser()
    _fireEvent(AbstractLogin.LoginEvent(this, true, username, password))
}

/**
 * Fires the [AbstractLogin.ForgotPasswordEvent].
 */
public fun AbstractLogin._forgotPassword() {
    checkEditableByUser()
    expect(true, "isForgotPasswordButtonVisible is false") { isForgotPasswordButtonVisible }
    _fireEvent(AbstractLogin.ForgotPasswordEvent(this, true))
}
