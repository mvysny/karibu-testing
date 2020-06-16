@file:Suppress("FunctionName")

package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.ClickEvent
import com.vaadin.flow.component.ClickNotifier
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.button.Button
import kotlin.test.fail

/**
 * Clicks the button, but only if it is actually possible to do so by the user. If the button is read-only or disabled, it throws an exception.
 * @throws IllegalArgumentException if the button was not visible, not enabled, read-only. See [_checkClickable] for
 * more details.
 */
fun Button._click() {
    _checkClickable()
    click()
}

/**
 * Clicks the component implementing the [ClickNotifier] interface,
 * but only if it is actually possible to do so by the user. If the button is read-only or disabled, it throws an exception.
 * @throws IllegalArgumentException if the button was not visible, not enabled, read-only. See [_checkClickable] for
 * more details.
 */
fun <T: ClickNotifier<*>> T._click() {
    _checkClickable()
    click()
}

/**
 * Clicks the component implementing the [ClickNotifier] interface.
 *
 * WARNING: also clicks disabled/invisible button. Please use [_click] instead.
 */
internal fun <T: ClickNotifier<*>> T.click() {
    (this as Component)._fireEvent(ClickEvent<Component>(this,
            false, 0, 0, 0, 0, 0, 0, false, false, false, false))
}

/**
 * Alias for [checkEditableByUser].
 */
fun Button._checkClickable() {
    checkEditableByUser()
}

/**
 * Alias for [checkEditableByUser].
 */
fun <T: ClickNotifier<*>> T._checkClickable() {
    (this as Component).checkEditableByUser()
}

/**
 * Fails if the button is clickable.
 * @throws AssertionError if the button is clickable.
 */
fun Button._expectNotClickable() {
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
fun ClickNotifier<*>._expectNotClickable() {
    try {
        _checkClickable()
        fail("The ${(this as Component).toPrettyString()} is clickable")
    } catch (ex: IllegalStateException) {
        // okay
    }
}
