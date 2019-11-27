@file:Suppress("FunctionName")

package com.github.mvysny.kaributesting.v8

import com.vaadin.ui.Button
import kotlin.test.fail

/**
 * Clicks the button, but only if it is actually possible to do so by the user.
 * If the button is read-only or disabled, an exception is thrown.
 *
 * See [_checkClickable] for more details.
 * @throws IllegalStateException if the button was not visible or not enabled.
 */
fun Button._click() {
    _checkClickable()
    click()
}

/**
 * Alias for [checkEditableByUser].
 * @throws IllegalStateException if the button was not visible or not enabled.
 */
fun Button._checkClickable() {
    checkEditableByUser()
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
