@file:Suppress("ObjectPropertyName", "FunctionName")

package com.github.mvysny.kaributesting.v8

import com.vaadin.annotations.Theme
import com.vaadin.data.HasValue
import com.vaadin.server.AbstractClientConnector
import com.vaadin.server.VaadinRequest
import com.vaadin.shared.communication.ClientRpc
import com.vaadin.ui.*
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
 * Checks that a component is actually editable by the user:
 * * The component must be effectively visible: it itself must be visible, its parent must be visible and all of its ascendants must be visible.
 *   For the purpose of testing individual components not attached to the [UI], a component may be considered visible even though it's not
 *   currently nested in a [UI].
 * * The component must be effectively enabled: it itself must be enabled, its parent must be enabled and all of its ascendants must be enabled.
 * * If the component is [HasValue], it must not be [HasValue.isReadOnly].
 * @throws IllegalStateException if any of the above doesn't hold.
 */
fun Component.checkEditableByUser() {
    check(isEffectivelyVisible()) { "The ${toPrettyString()} is not effectively visible - either it is hidden, or its ascendant is hidden" }
    check(isEnabled) { "The ${toPrettyString()} is not enabled" }
    check(isEffectivelyEnabled()) { "The ${toPrettyString()} is nested in a disabled component" }
    if (this is HasValue<*>) {
        check(!this.isReadOnly) { "The ${toPrettyString()} is read-only" }
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
 * Returns [Label.value] or [HasValue.getValue]; returns `null` if the receiver is neither of those two things.
 */
val Component.value: Any? get()= when(this) {
    is Label -> this.value
    is HasValue<*> -> this.value
    else -> null
}

/**
 * Sets the value of given component, but only if it is actually possible to do so by the user, i.e. the component
 * is enabled and is not read-only. If the component is read-only or disabled, an exception is thrown.
 * @throws IllegalStateException if the field was not visible, not enabled or was read-only.
 */
var <V> HasValue<V>._value: V?
    get() = value
    set(v) {
        (this as Component).checkEditableByUser()
        value = v
    }

fun <T : ClientRpc> AbstractClientConnector.overrideRpcProxy(rpcInterface: Class<T>, instance: T) {
    val rpcProxyMap: MutableMap<Class<*>, ClientRpc> = AbstractClientConnector::class.java.getDeclaredField("rpcProxyMap").run {
        isAccessible = true
        @Suppress("UNCHECKED_CAST")
        get(this@overrideRpcProxy) as MutableMap<Class<*>, ClientRpc>
    }
    rpcProxyMap[rpcInterface] = instance
}

/**
 * Returns [AbstractTextField.getPlaceholder]/[ComboBox.getPlaceholder]/[DateField.getPlaceholder]/[DateTimeField.getPlaceholder] or null
 * for other components.
 */
val Component.placeholder: String?
    get() = when (this) {
        is AbstractTextField -> placeholder
        is ComboBox<*> -> this.placeholder  // https://youtrack.jetbrains.com/issue/KT-24275
        is DateField -> placeholder
        is DateTimeField -> placeholder
        else -> null
    }

/**
 * Checks whether this component matches given spec. All rules are matched except the [count] rule. The
 * rules are matched against given component only (not against its children).
 */
fun Component.matches(spec: SearchSpec<Component>.()->Unit): Boolean = SearchSpec(Component::class.java).apply { spec() }.toPredicate().invoke(this)

/**
 * Returns the current Vaadin theme (the theme that the current UI uses).
 */
val currentTheme: String get() {
    val ui = UI.getCurrent() ?: throw AssertionError("No current UI")
    return ui.theme
            ?: ui.javaClass.getAnnotation(Theme::class.java)?.value
            ?: ui.session.service.getConfiguredTheme(VaadinRequest.getCurrent())
}
