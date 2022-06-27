package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.BinderUtilsKt
import com.github.mvysny.kaributesting.v10.HasValidationUtilsKt
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasValidation
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.binder.BinderValidationStatus
import com.vaadin.flow.data.binder.ValidationException
import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull

/**
 * @author mavi
 */
@CompileStatic
class BinderUtils {
    /**
     * Workaround for https://github.com/vaadin/flow/issues/7081
     */
    @NotNull
    static String getVerboseMessage(@NotNull ValidationException self) {
        BinderUtilsKt.getVerboseMessage(self)
    }

    @NotNull
    static String getVerboseMessage(@NotNull BinderValidationStatus<?> status) {
        BinderUtilsKt.getVerboseMessage(status)
    }

    static void assertValid(@NotNull BinderValidationStatus<?> status) {
        BinderUtilsKt._expectValid(status)
    }

    static void assertValid(@NotNull Binder<?> binder) {
        BinderUtilsKt._expectValid(binder)
    }

    static void assertValid(@NotNull HasValidation field) {
        HasValidationUtilsKt._expectValid(field)
    }

    static void assertAllFieldsValid(@NotNull Component form) {
        HasValidationUtilsKt._expectAllFieldsValid(form)
    }

    static void assertInvalid(@NotNull HasValidation field, @NotNull String expectedErrorMessage = "") {
        HasValidationUtilsKt._expectInvalid(field, expectedErrorMessage)
    }
}
