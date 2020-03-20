package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.LoginFormKt
import com.vaadin.flow.component.login.AbstractLogin
import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull

/**
 * @author mavi
 */
@CompileStatic
class LoginFormUtils {
    /**
     * Fires the [AbstractLogin.LoginEvent].
     */
    static void _login(@NotNull AbstractLogin self, @NotNull String username, @NotNull String password) {
        LoginFormKt._login(self, username, password)
    }

    /**
     * Fires the [AbstractLogin.ForgotPasswordEvent].
     */
    static void _forgotPassword(@NotNull AbstractLogin self) {
        LoginFormKt._forgotPassword(self)
    }
}
