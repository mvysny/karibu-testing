@file:Suppress("FunctionName")

package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributools.serverClick
import com.vaadin.flow.component.ClickNotifier
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.button.Button
import kotlin.test.fail

/**
 * Clicks the component implementing the [ClickNotifier] interface,
 * but only if it is actually possible to do so by the user. If the button is read-only or disabled, it throws an exception.
 * @throws IllegalArgumentException if the button was not visible, not enabled, read-only. See [_checkClickable] for
 * more details.
 */
public fun <T : ClickNotifier<*>> T._click() {
    _checkClickable()
    if (this is Button) {
        this.click()
    } else {
        serverClick()
    }
}

/**
 * Alias for [checkEditableByUser].
 */
public fun Button._checkClickable() {
    _expectEditableByUser()
}

/**
 * Alias for [checkEditableByUser].
 */
public fun <T : ClickNotifier<*>> T._checkClickable() {
    (this as Component)._expectEditableByUser()
}

/**
 * Fails if the button is clickable.
 * @throws AssertionError if the button is clickable.
 */
public fun Button._expectNotClickable() {
    try {
        _checkClickable()
        fail("The ${toPrettyString()} is clickable")
    } catch (ex: IllegalStateException) {
        // okay
    }
}

/**
 * Fails if the button is clickable.
 * @throws AssertionError if the button is clickable.
 */
public fun ClickNotifier<*>._expectNotClickable() {
    try {
        _checkClickable()
        fail("The ${(this as Component).toPrettyString()} is clickable")
    } catch (ex: IllegalStateException) {
        // okay
    }
}
