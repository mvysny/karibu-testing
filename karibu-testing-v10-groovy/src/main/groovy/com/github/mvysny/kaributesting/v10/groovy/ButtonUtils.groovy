package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.ButtonKt
import com.vaadin.flow.component.button.Button
import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull

/**
 * A set of basic extension methods for {@link Button}.
 * @author mavi
 */
@CompileStatic
class ButtonUtils {
    /**
     * Clicks the button, but only if it is actually possible to do so by the user. If the button is read-only or disabled, it throws an exception.
     * @throws IllegalArgumentException if the button was not visible, not enabled, read-only.
     * See {@link #_checkClickable(com.vaadin.flow.component.button.Button)} for more details.
     */
    static void _click(@NotNull Button self) {
        ButtonKt._click(self)
    }

    /**
     * Alias for {@link BasicUtils#checkEditableByUser(com.vaadin.flow.component.Component)}.
     */
    static void _checkClickable(@NotNull Button self) {
        ButtonKt._checkClickable(self)
    }

    /**
     * Fails if the button is clickable.
     * @throws AssertionError if the button is clickable.
     */
    static void _expectNotClickable(@NotNull Button self) {
        ButtonKt._expectNotClickable(self)
    }
}
