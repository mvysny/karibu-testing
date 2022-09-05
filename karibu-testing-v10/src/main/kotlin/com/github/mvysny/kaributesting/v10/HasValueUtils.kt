package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.AbstractField
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasValue

/**
 * Sets the value of given component, but only if it is actually possible to do so by the user.
 * If the component is read-only or disabled, an exception is thrown.
 *
 * Modification of this property fires the value change event; the [HasValue.ValueChangeEvent.isFromClient] will
 * return `false` indicating that the event came from the server. If this is not desired,
 * depending on your code, it may be
 * possible to call [_fireValueChange] with `fromClient=true` instead.
 * @throws IllegalStateException if the field was not visible, not enabled or was read-only.
 */
public var <V> HasValue<*, V>._value: V?
    get() = value
    set(v) {
        (this as Component).checkEditableByUser()
        value = v
    }

/**
 * Fires a value change event which "comes from the client".
 *
 * The event is only fired if it is actually possible to do so by the user.
 * If the component is read-only or disabled, an exception is thrown.
 * @param fromClient defaults to true
 * @throws IllegalStateException if the field was not visible, not enabled or was read-only.
 */
public fun <C: AbstractField<C, V>, V> C._fireValueChange(fromClient: Boolean = true) {
    checkEditableByUser()
    _fireEvent(AbstractField.ComponentValueChangeEvent<C, V>(this, this, value, fromClient))
}
