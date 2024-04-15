package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.AbstractField
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasValue
import com.vaadin.flow.component.internal.AbstractFieldSupport

/**
 * Sets the value of given component, but only if it is actually possible to do so by the user.
 * If the component is read-only or disabled, an exception is thrown.
 *
 * Modification of this property fires the value change event; the [HasValue.ValueChangeEvent.isFromClient] will
 * return `false` indicating that the event came from the server. If this is not desired, use [_setValue];
 * alternatively it may be
 * possible to call [_fireValueChange] with `fromClient=true` instead.
 * @throws IllegalStateException if the field was not visible, not enabled or was read-only.
 */
public var <V> HasValue<*, V>._value: V?
    get() = value
    set(v) {
        (this as Component)._expectEditableByUser()
        value = v
    }

public val <T> AbstractField<*, T>._fieldSupport: AbstractFieldSupport<*, T> get() {
    val f = AbstractField::class.java.getDeclaredField("fieldSupport")
    f.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    return f.get(this) as AbstractFieldSupport<*, T>
}

/**
 * Sets the value of given component, but only if it is actually possible to do so by the user.
 * If the component is read-only or disabled, an exception is thrown.
 *
 * Modification of this property fires the value change event; the [HasValue.ValueChangeEvent.isFromClient] will
 * mirror the [fromClient] parameter which defaults to `true` indicating that the event came from the client.
 * @throws IllegalStateException if the field was not visible, not enabled or was read-only.
 */
@JvmOverloads
public fun <V> HasValue<*, V>._setValue(value: V?, fromClient: Boolean = true) {
    (this as Component)._expectEditableByUser()
    val fs = (this as AbstractField<*, V>)._fieldSupport
    val m = AbstractFieldSupport::class.java.getDeclaredMethod("setValue", Any::class.java, Boolean::class.java, Boolean::class.java)
    m.isAccessible = true
    m.invoke(fs, value, false, fromClient)
}

/**
 * Fires a value change event which "comes from the client".
 *
 * The event is only fired if it is actually possible to do so by the user.
 * If the component is read-only or disabled, an exception is thrown.
 * @param fromClient defaults to true
 * @throws IllegalStateException if the field was not visible, not enabled or was read-only.
 */
public fun <C : AbstractField<C, *>> C._fireValueChange(fromClient: Boolean = true) {
    _expectEditableByUser()
    @Suppress("UNCHECKED_CAST")
    _fireEvent(
        AbstractField.ComponentValueChangeEvent(
            this,
            this as HasValue<*, Any?>,
            value,
            fromClient
        )
    )
}
