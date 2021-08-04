@file:Suppress("FunctionName")

package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.*
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.html.Input
import com.vaadin.flow.component.textfield.PasswordField
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.dom.DomEvent
import com.vaadin.flow.server.VaadinSession
import elemental.json.Json
import elemental.json.JsonObject
import kotlin.test.fail

/**
 * Allows us to fire any Vaadin event on any Vaadin component.
 * @receiver the component, not null.
 * @param event the event, not null.
 */
public fun Component._fireEvent(event: ComponentEvent<*>) {
    ComponentUtil.fireEvent(this, event)
}

/**
 * Fires a DOM event on this component.
 * @param eventType the event type, e.g. "click"
 * @param eventData optional event data, defaults to an empty object.
 */
@JvmOverloads
public fun Component._fireDomEvent(eventType: String, eventData: JsonObject = Json.createObject()) {
    element._fireDomEvent(DomEvent(element, eventType, eventData))
}

/**
 * Determines the component's `label` (usually it's the HTML element's `label` property, but it's [Checkbox.getLabel] for checkbox).
 * Intended to be used for fields such as [TextField].
 */
public var Component.label: String
    get() = when (this) {
        is Checkbox -> label
        else -> element.getProperty("label") ?: ""
    }
    set(value) {
        when (this) {
            is Checkbox -> label = value
            else -> element.setProperty("label", if (value.isBlank()) null else value)
        }
    }

/**
 * The Component's caption: [Button.getText] for [Button], [label] for fields such as [TextField].
 *
 * For FormItem: Concatenates texts from all elements placed in the `label` slot. This effectively
 * returns whatever was provided in the String label via [FormLayout.addFormItem].
 */
public var Component.caption: String
    get() = when (this) {
        is Button -> text
        is FormLayout.FormItem -> this.caption
        else -> label
    }
    set(value) {
        when (this) {
            is Button -> text = value
            is FormLayout.FormItem -> throw IllegalArgumentException("Setting the caption of FormItem is currently unsupported")
            else -> label = value
        }
    }
/**
 * The same as [Component.getId] but without Optional.
 *
 * Workaround for https://github.com/vaadin/flow/issues/664
 */
public var Component.id_: String?
    get() = id.orElse(null)
    set(value) {
        setId(value)
    }

/**
 * Checks whether this component is currently attached to an [UI].
 *
 * Returns true for attached components even if the UI itself is closed.
 */
public val Component.isAttached: Boolean
    // see https://github.com/vaadin/flow/issues/7911
    get() = element.node.isAttached

/**
 * Checks whether the component is visible (usually [Component.isVisible] but for [Text]
 * the text must be non-empty).
 */
public val Component._isVisible: Boolean
    get() = when (this) {
        is Text -> !text.isNullOrBlank()   // workaround for https://github.com/vaadin/flow/issues/3201
        else -> isVisible
    }

/**
 * Returns direct text contents (it doesn't peek into the child elements).
 */
public val Component._text: String?
    get() = when (this) {
        is HasText -> text
        is Text -> text   // workaround for https://github.com/vaadin/flow/issues/3606
        else -> null
    }

/**
 * Checks that a component is actually editable by the user:
 * * The component must be effectively visible: it itself must be visible, its parent must be visible and all of its ascendants must be visible.
 *   For the purpose of testing individual components not attached to the [UI], a component may be considered visible even though it's not
 *   currently nested in a [UI].
 * * The component must be effectively enabled: it itself must be enabled, its parent must be enabled and all of its ascendants must be enabled.
 * * If the component is [HasValue], it must not be [HasValue.isReadOnly].
 * @throws IllegalStateException if any of the above doesn't hold.
 */
public fun Component.checkEditableByUser() {
    check(isEffectivelyVisible()) { "The ${toPrettyString()} is not effectively visible - either it is hidden, or its ascendant is hidden" }
    val parentNullOrEnabled = !parent.isPresent || parent.get().isEffectivelyEnabled()
    if (parentNullOrEnabled) {
        check(isEnabled) { "The ${toPrettyString()} is not enabled" }
    }
    check(isEffectivelyEnabled()) { "The ${toPrettyString()} is nested in a disabled component" }
    if (this is HasValue<*, *>) {
        @Suppress("UNCHECKED_CAST")
        val hasValue = this as HasValue<HasValue.ValueChangeEvent<Any?>, Any?>
        check(!hasValue.isReadOnly) { "The ${toPrettyString()} is read-only" }
    }
}

/**
 * Fails if the component is editable. See [checkEditableByUser] for more details.
 * @throws AssertionError if the component is editable.
 */
public fun Component.expectNotEditableByUser() {
    try {
        checkEditableByUser()
        fail("The ${toPrettyString()} is editable")
    } catch (ex: IllegalStateException) {
        // okay
    }
}

internal fun Component.isEffectivelyVisible(): Boolean = _isVisible && (!parent.isPresent || parent.get().isEffectivelyVisible())

/**
 * Computes whether this component and all of its parents are enabled.
 *
 * Effectively a shortcut for [isEnabled] since it recursively checks that all ancestors
 * are also enabled (the "implicitly disabled" effect, see [HasEnabled.isEnabled] javadoc for more details).
 * @return false if this component or any of its parent is disabled.
 */
public fun Component.isEffectivelyEnabled(): Boolean = isEnabled

/**
 * Checks whether this component is [HasEnabled.isEnabled]. All components not implementing [HasEnabled] are considered enabled.
 */
public val Component.isEnabled: Boolean
    get() = when (this) {
        is HasEnabled -> isEnabled
        else -> true
    }

// modify when this is fixed: https://github.com/vaadin/flow/issues/4068
public var Component.placeholder: String?
    get() = when (this) {
        is TextField -> placeholder
        is TextArea -> placeholder
        is PasswordField -> placeholder
        is ComboBox<*> -> this.placeholder  // https://youtrack.jetbrains.com/issue/KT-24275
        is DatePicker -> placeholder
        is Input -> placeholder.orElse(null)
        else -> null
    }
    set(value) {
        when (this) {
            is TextField -> placeholder = value
            is TextArea -> placeholder = value
            is PasswordField -> placeholder = value
            is ComboBox<*> -> this.placeholder = value
            is DatePicker -> placeholder = value
            is Input -> setPlaceholder(value)
            else -> throw IllegalStateException("${toPrettyString()} doesn't support setting placeholder")
        }
    }

/**
 * Removes the component from its parent. Does nothing if the component does not have a parent.
 */
public fun Component.removeFromParent() {
    (parent.orElse(null) as? HasComponents)?.remove(this)
}

/**
 * Checks whether this component matches given spec. All rules are matched except the [count] rule. The
 * rules are matched against given component only (not against its children).
 */
public fun Component.matches(spec: SearchSpec<Component>.() -> Unit): Boolean =
        SearchSpec(Component::class.java).apply { spec() }.toPredicate().invoke(this)

/**
 * Fires [FocusNotifier.FocusEvent] on the component, but only if it's editable.
 */
public fun <T> T._focus() where T : Focusable<*>, T : Component {
    checkEditableByUser()
    _fireEvent(FocusNotifier.FocusEvent<T>(this, true))
}

/**
 * Fires [BlurNotifier.BlurEvent] on the component, but only if it's editable.
 */
public fun <T> T._blur() where T : Focusable<*>, T : Component {
    checkEditableByUser()
    _fireEvent(BlurNotifier.BlurEvent<T>(this, true))
}

/**
 * Closes the UI and simulates the end of the request. The [UI.close] is called,
 * but also the session is set to null which fires the detach listeners and makes
 * the UI and all of its components detached.
 */
public fun UI._close() {
    close()
    // Mock closing of UI after request handled.
    VaadinSession.getCurrent().removeUI(this)
}

/**
 * Returns child components which were added to this component via
 * [com.vaadin.flow.dom.Element.appendVirtualChild].
 */
public fun Component._getVirtualChildren(): List<Component> =
    element.getVirtualChildren().map { it._findComponents() }.flatten()
