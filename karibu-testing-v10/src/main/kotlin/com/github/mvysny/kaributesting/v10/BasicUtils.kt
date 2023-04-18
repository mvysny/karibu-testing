@file:Suppress("FunctionName")

package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributools.getVirtualChildren
import com.github.mvysny.kaributools.textRecursively2
import com.vaadin.flow.component.*
import com.vaadin.flow.dom.DomEvent
import com.vaadin.flow.router.InternalServerError
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
 * Fires a DOM event on this component. Checks whether the component is [_expectEditableByUser].
 * @param eventType the event type, e.g. "click"
 * @param eventData optional event data, defaults to an empty object.
 */
@JvmOverloads
public fun Component._fireDomEvent(
    eventType: String,
    eventData: JsonObject = Json.createObject()
) {
    _expectEditableByUser()
    element._fireDomEvent(DomEvent(element, eventType, eventData))
}

/**
 * Adds [com.vaadin.flow.component.button.Button.click] functionality to all [ClickNotifier]s. This function directly calls
 * all click listeners, thus it avoids the roundtrip to client and back. It even works with browserless testing.
 * @param fromClient see [ComponentEvent.isFromClient], defaults to true.
 * @param button see [ClickEvent.getButton], defaults to 0.
 * @param clickCount see [ClickEvent.getClickCount], defaults to 1.
 */
public fun Component._fireDomClickEvent(
    button: Int = 0,
    clickCount: Int = 1,
    shiftKey: Boolean = false,
    ctrlKey: Boolean = false,
    altKey: Boolean = false,
    metaKey: Boolean = false
) {
    val json = Json.createObject().apply {
        put("event.button", Json.create(button.toDouble()))
        put("event.detail", Json.create(clickCount.toDouble()))
        put("event.shiftKey", Json.create(shiftKey))
        put("event.ctrlKey", Json.create(ctrlKey))
        put("event.altKey", Json.create(altKey))
        put("event.metaKey", Json.create(metaKey))
    }
    _fireDomEvent("click", json)
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
 * Checks whether the component is visible (usually [Component.isVisible] but for [Text]
 * the text must be non-empty).
 */
public val Component._isVisible: Boolean
    get() = when (this) {
        is Text -> !text.isNullOrBlank()   // workaround for https://github.com/vaadin/flow/issues/3201
        else -> isVisible
    }

/**
 * Returns direct text contents (it doesn't peek into the child elements). If this component
 * doesn't implement [HasText] then this returns null.
 */
public val Component._text: String?
    get() = when (this) {
        is HasText -> text
        else -> null
    }

/**
 * Returns text contents of this node and all of its descendant nodes.
 */
public val Component._textRecursively: String get() = element.textRecursively2

@Deprecated("Replaced by _expectEditableByUser()")
public fun Component.checkEditableByUser() {
    _expectEditableByUser()
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
public fun Component._expectEditableByUser() {
    check(isEffectivelyVisible()) { "The ${toPrettyString()} is not effectively visible - either it is hidden, or its ascendant is hidden" }
    val parentNullOrEnabled = !parent.isPresent || parent.get().isEnabled
    if (parentNullOrEnabled) {
        check(isEnabled) { "The ${toPrettyString()} is not enabled" }
    }
    check(isEnabled) { "The ${toPrettyString()} is nested in a disabled component" }
    if (this is HasValue<*, *>) {
        @Suppress("UNCHECKED_CAST")
        val hasValue = this as HasValue<HasValue.ValueChangeEvent<Any?>, Any?>
        check(!hasValue.isReadOnly) { "The ${toPrettyString()} is read-only" }
    }
}

@Deprecated("Replaced by _expectNotEditableByUser()")
public fun Component.expectNotEditableByUser() {
    _expectNotEditableByUser()
}

/**
 * Fails if the component is editable. See [checkEditableByUser] for more details.
 * @throws AssertionError if the component is editable.
 */
public fun Component._expectNotEditableByUser() {
    try {
        _expectEditableByUser()
        fail("The ${toPrettyString()} is editable")
    } catch (ex: IllegalStateException) {
        // okay
    }
}

internal fun Component.isEffectivelyVisible(): Boolean =
    _isVisible && (!parent.isPresent || parent.get().isEffectivelyVisible())

/**
 * Computes whether this component and all of its parents are enabled.
 *
 * Effectively a shortcut for [isEnabled] since it recursively checks that all ancestors
 * are also enabled (the "implicitly disabled" effect, see [HasEnabled.isEnabled] javadoc for more details).
 *
 * Deprecated; replace with [isEnabled].
 * @return false if this component or any of its parent is disabled.
 */
@Deprecated("Deprecated since HasEnabled.isEnabled is essentially the same thing as isEffectivelyEnabled")
public fun Component.isEffectivelyEnabled(): Boolean = isEnabled

/**
 * Checks whether this component is [HasEnabled.isEnabled]. All components not implementing [HasEnabled] are considered enabled
 * unless their ascendant is disabled.
 */
public val Component.isEnabled: Boolean
    get() = when (this) {
        is HasEnabled -> isEnabled
        else -> parent.orElse(null)?.isEnabled ?: true
    }

/**
 * Checks whether this component matches given spec. All rules are matched except the [count] rule. The
 * rules are matched against given component only (not against its children).
 */
public fun Component.matches(spec: SearchSpec<Component>.() -> Unit): Boolean =
    SearchSpec(Component::class.java).apply { spec() }.toPredicate()
        .invoke(this)

/**
 * Fires [FocusNotifier.FocusEvent] on the component, but only if it's editable.
 */
public fun <T> T._focus() where T : Focusable<*>, T : Component {
    _expectEditableByUser()
    _fireEvent(FocusNotifier.FocusEvent<T>(this, true))
}

/**
 * Fires [BlurNotifier.BlurEvent] on the component, but only if it's editable.
 */
public fun <T> T._blur() where T : Focusable<*>, T : Component {
    _expectEditableByUser()
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

internal val InternalServerError.errorMessage: String get() = element.text

/**
 * Fails if this component is not [com.vaadin.flow.component.HasEnabled.isEnabled].
 * Also fails when the parent is disabled.
 */
public fun Component._expectEnabled() {
    if (!isEnabled) {
        fail("${toPrettyString()} is not enabled")
    }
}

/**
 * Fails if this component is [com.vaadin.flow.component.HasEnabled.isEnabled].
 * Always succeeds when the parent is disabled.
 */
public fun Component._expectDisabled() {
    if (isEnabled) {
        fail("${toPrettyString()} is not disabled")
    }
}

/**
 * Fails if this component is not [read-only][HasValue.isReadOnly].
 */
public fun HasValue<*, *>._expectReadOnly() {
    if (!isReadOnly) {
        fail("${(this as Component).toPrettyString()} is not read-only")
    }
}

/**
 * Fails if this component is [read-only][HasValue.isReadOnly].
 */
public fun HasValue<*, *>._expectNotReadOnly() {
    if (isReadOnly) {
        fail("${(this as Component).toPrettyString()} is read-only")
    }
}
