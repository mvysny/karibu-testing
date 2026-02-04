package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributools.SemanticVersion
import com.github.mvysny.kaributools.VaadinVersion
import com.vaadin.flow.component.AbstractField
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasValue
import com.vaadin.flow.component.internal.AbstractFieldSupport
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Sets the value of given component, but only if it is actually possible to do so by the user.
 * If the component is read-only or disabled, an exception is thrown.
 *
 * Modification of this property calls [_setValue] with `isFromClient` set to true (since Karibu-Testing
 * 2.4.0). To emulate older Karibu-Testing, set [defaultIsFromClient] to `false`.
 *
 * @throws IllegalStateException if the field was not visible, not enabled or was read-only.
 */
public var <V> HasValue<*, V>._value: V?
    get() = value
    set(v) {
        (this as Component)._expectEditableByUser()
        _setValue(v, defaultIsFromClient)
    }

private val __AbstractField_setModelValue: Method by lazy {
    val m = AbstractField::class.java.getDeclaredMethod("setModelValue", Object::class.java, Boolean::class.java)
    m.isAccessible = true
    m
}

private fun <V> HasValue<*, V>.setModelValue(v: V?, fromClient: Boolean) {
    __AbstractField_setModelValue.invoke(this, v, fromClient)
}

private val __AbstractField_fieldSupport: Field by lazy {
    val f = AbstractField::class.java.getDeclaredField("fieldSupport")
    f.isAccessible = true
    f
}

@Suppress("UNCHECKED_CAST")
private val <V> HasValue<*, V>._fieldSupport: AbstractFieldSupport<*, V>
    get() = __AbstractField_fieldSupport.get(this) as AbstractFieldSupport<*, V>

private val __AbstractFieldSupport_setValue: Method by lazy {
    val m = AbstractFieldSupport::class.java.getDeclaredMethod("setValue", Object::class.java, Boolean::class.java, Boolean::class.java)
    m.isAccessible = true
    m
}

private fun <V> HasValue<*, V>.setValue(value: V?, fromInternal: Boolean, fromClient: Boolean) {
    __AbstractFieldSupport_setValue.invoke(_fieldSupport, value, fromInternal, fromClient)
}

/**
 * Sets the [value] of given component, but only if it is actually possible to do so by the user.
 * If the component is read-only or disabled, an exception is thrown.
 *
 * Modification of this property fires the value change event; the [HasValue.ValueChangeEvent.isFromClient] will
 * mirror the [fromClient] parameter which defaults to `true` indicating that the event came from the client.
 * @param V the type of the value we're setting to the receiver field.
 * @param fromClient `true` if the new value originates from the client; otherwise `false`. This will be set to [HasValue.ValueChangeEvent.isFromClient].
 * @throws IllegalStateException if the field was not visible, not enabled or was read-only.
 */
@JvmOverloads
public fun <V> HasValue<*, V>._setValue(value: V?, fromClient: Boolean = true) {
    (this as Component)._expectEditableByUser()
    // this doesn't call the AbstractField.setPresentationValue(): https://github.com/mvysny/karibu-testing/issues/194#issuecomment-3077492570
//    setModelValue(value, fromClient)
    setValue(value, false, fromClient)
}

/**
 * Fires a value change event which "comes from the client".
 *
 * The event is only fired if it is actually possible to do so by the user.
 * If the component is read-only or disabled, an exception is thrown.
 * @param fromClient defaults to true
 * @throws IllegalStateException if the field was not visible, not enabled or was read-only.
 */
public fun HasValue<*, *>._fireValueChange(fromClient: Boolean = true) {
    (this as Component)._expectEditableByUser()
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
