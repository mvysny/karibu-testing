package com.github.karibu.testing

import com.vaadin.data.HasValue
import com.vaadin.server.AbstractClientConnector
import com.vaadin.ui.Button
import com.vaadin.ui.Component
import java.util.*
import kotlin.test.expect

/**
 * Allows us to fire any Vaadin event on any Vaadin component.
 * @receiver the component, not null.
 * @param event the event, not null.
 */
fun AbstractClientConnector._fireEvent(event: EventObject) {
    // fireEvent() is protected, gotta make it public
    val fireEvent = AbstractClientConnector::class.java.getDeclaredMethod("fireEvent", EventObject::class.java)
    fireEvent.isAccessible = true
    fireEvent.invoke(this, event)
}

val IntRange.size: Int get() = (endInclusive + 1 - start).coerceAtLeast(0)

/**
 * Clicks the button, but only if it is actually possible to do so by the user. If the button is read-only or disabled, an exception is thrown.
 * @throws IllegalArgumentException if the button was not visible, not enabled, read-only or if no button (or too many buttons) matched.
 */
fun Button._click() {
    checkEditableByUser()
    click()
}

private fun Component.checkEditableByUser() {
    if (!isEffectivelyVisible()) {
        throw IllegalArgumentException("The ${toPrettyString()} is not effectively visible - either it is hidden, or its ascendant is hidden")
    }
    if (!isEnabled) {
        throw IllegalArgumentException("The ${toPrettyString()} is not enabled")
    }
    if (!isEffectivelyEnabled()) {
        throw IllegalArgumentException("The ${toPrettyString()} is nested in a disabled component")
    }
    if (this is HasValue<*> && this.isReadOnly) {
        throw IllegalArgumentException("The ${toPrettyString()} is read-only")
    }
}

private fun Component.isEffectivelyEnabled(): Boolean = when {
    !isEnabled -> false
    // this is the difference to Component.isConnectorEnabled which in this case returns false.
    // however, we should be perfectly able to test components not connected to a UI.
    parent == null -> true
    else -> parent.isEffectivelyEnabled()
}

/**
 * Expects that [actual] list of objects matches [expected] list of objects. Fails otherwise.
 */
fun <T> expectList(vararg expected: T, actual: ()->List<T>) = expect(expected.toList(), actual)

/**
 * Sets the value of given component, but only if it is actually possible to do so by the user.
 * If the component is read-only or disabled, an exception is thrown.
 */
var <V> HasValue<V>._value: V?
    get() = value
    set(v) {
        (this as Component).checkEditableByUser()
        value = v
    }
