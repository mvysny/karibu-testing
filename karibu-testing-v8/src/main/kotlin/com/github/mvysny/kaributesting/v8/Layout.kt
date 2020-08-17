@file:Suppress("FunctionName")

package com.github.mvysny.kaributesting.v8

import com.vaadin.event.LayoutEvents
import com.vaadin.server.AbstractClientConnector
import com.vaadin.shared.MouseEventDetails
import com.vaadin.ui.Component
import kotlin.test.fail

/**
 * Notifies a layout that a nested component has been clicked. Fires the [com.vaadin.event.LayoutEvents.LayoutClickEvent]
 * but only if it is actually possible to do so by the user.
 * If either the layout or the component is read-only or disabled, an exception is thrown.
 *
 * See [_checkClickable] for more details.
 * @param clickedComponent the component being clicked, needs to be [checkEditableByUser]. The component must descend from this layout.
 * @throws IllegalStateException if either the layout or the [clickedComponent] is not visible or not enabled.
 */
@JvmOverloads
public fun LayoutEvents.LayoutClickNotifier._click(clickedComponent: Component,
                                            mouseEventDetails: MouseEventDetails = MouseEventDetails()) {
    _checkClickable()
    clickedComponent.checkEditableByUser()

    val childComponent: Component? = clickedComponent.findAncestorOrSelf { it.parent == this }
    requireNotNull(childComponent) { "The clicked component ${clickedComponent.toPrettyString()} is not nested within this layout ${(this as Component).toPrettyString()}" }

    (this as AbstractClientConnector)._fireEvent(LayoutEvents.LayoutClickEvent(this as Component,
            mouseEventDetails, clickedComponent, childComponent))
}

/**
 * Alias for [checkEditableByUser].
 * @throws IllegalStateException if the button was not visible or not enabled.
 */
public fun LayoutEvents.LayoutClickNotifier._checkClickable() {
    (this as Component).checkEditableByUser()
}

/**
 * Fails if the button is clickable.
 * @throws AssertionError if the button is clickable.
 */
public fun LayoutEvents.LayoutClickNotifier._expectNotClickable() {
    try {
        _checkClickable()
        fail("The ${(this as Component).toPrettyString()} is clickable")
    } catch (ex: IllegalStateException) {
        // okay
    }
}
