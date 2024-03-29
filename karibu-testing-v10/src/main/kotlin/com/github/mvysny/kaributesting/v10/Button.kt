@file:Suppress("FunctionName")

package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.ClickNotifier
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.ComponentEvent
import kotlin.test.fail

/**
 * Performs a "browser" click on the component. Clicks the component,
 * but only if it is actually possible to do so by the user. If the button is read-only or disabled, it throws an exception.
 *
 * Notifies both [ClickNotifier] listeners and the DOM "click" event listeners;
 * [ComponentEvent.isFromClient] will return true.
 * @throws IllegalArgumentException if the button was not visible, not enabled, read-only. See [_checkClickable] for
 * more details.
 */
public fun <T : ClickNotifier<*>> T._click() {
    _checkClickable()
    // Fire the DOM click event. That will both call the 'click' DOM event listeners,
    // and also causes Vaadin Flow to fire the ClickNotifier
    // higher-level event from its internal handling code. Requested by: https://github.com/mvysny/karibu-testing/issues/151
    (this as Component)._fireDomClickEvent()
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
