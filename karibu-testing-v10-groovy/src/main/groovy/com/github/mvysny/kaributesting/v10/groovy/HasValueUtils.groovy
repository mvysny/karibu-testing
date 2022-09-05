package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.HasValueUtilsKt
import com.vaadin.flow.component.AbstractField
import com.vaadin.flow.component.HasValue
import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

/**
 * A set of basic extension methods for {@link HasValue}-based components such as
 * TextField.
 * @author Martin Vysny <mavi@vaadin.com>
 */
@CompileStatic
class HasValueUtils {
    /**
     * Gets the value of given component. Alias for {@link com.vaadin.flow.component.HasValue#getValue()}.
     */
    @Nullable
    static <V, E extends HasValue.ValueChangeEvent<V>> V get_value(@NotNull HasValue<E, V> self) {
        return HasValueUtilsKt.get_value(self)
    }
    /**
     * Sets the value of given component, but only if it is actually possible to do so by the user.
     * If the component is read-only or disabled, an exception is thrown.
     * <p></p>
     * The function fires the value change event; the {@link HasValue.ValueChangeEvent#isFromClient()} will
     * return false indicating that the event came from the server. If this is not desired,
     * depending on your code, it may be
     * possible to call {@link #_fireValueChange(AbstractField, boolean)} with <code>fromClient=true</code> instead.
     * @throws IllegalStateException if the field was not visible, not enabled or was read-only.
     */
    static <V> void set_value(@NotNull HasValue<?, V> self, @Nullable V value) {
        HasValueUtilsKt.set_value(self, value)
    }

    /**
     * Fires a value change event which "comes from the client".
     * <p></p>
     * The event is only fired if it is actually possible to do so by the user.
     * If the component is read-only or disabled, an exception is thrown.
     * @param fromClient defaults to true
     * @throws IllegalStateException if the field was not visible, not enabled or was read-only.
     */
    static void _fireValueChange(@NotNull AbstractField<?, ?> self, boolean fromClient = true) {
        HasValueUtilsKt._fireValueChange(self as AbstractField, fromClient)
    }
}
