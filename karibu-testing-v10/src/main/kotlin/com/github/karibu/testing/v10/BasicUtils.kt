package com.github.karibu.testing.v10

import com.vaadin.flow.component.*
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.dom.Element
import com.vaadin.flow.dom.ElementUtil
import com.vaadin.flow.router.HasErrorParameter
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.startup.RouteRegistry
import org.atmosphere.util.annotation.AnnotationDetector
import org.jsoup.nodes.Document
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

fun Serializable.serializeToBytes(): ByteArray = ByteArrayOutputStream().use { it -> ObjectOutputStream(it).writeObject(this); it }.toByteArray()
inline fun <reified T: Serializable> ByteArray.deserialize(): T = ObjectInputStream(inputStream()).readObject() as T
inline fun <reified T: Serializable> T.serializeDeserialize() = serializeToBytes().deserialize<T>()

/**
 * A configuration object of all routes and error routes in the application. Use [autoDiscoverViews] to discover views; use [addErrorRoutes]
 * to manually add [HasErrorParameter]s.
 */
class Routes: Serializable {
    val routes = mutableSetOf<Class<out Component>>()
    val errorRoutes = mutableSetOf<Class<out HasErrorParameter<*>>>()

    fun addErrorRoutes(vararg routes: Class<out HasErrorParameter<*>>): Routes = apply {
        errorRoutes.addAll(routes.toSet())
    }

    /**
     * Creates a Vaadin 10 registry from this configuration object.
     */
    fun createRegistry() : RouteRegistry = object : RouteRegistry() {
        init {
            setNavigationTargets(routes)
            setErrorNavigationTargets(errorRoutes.map { it.asSubclass(Component::class.java) } .toMutableSet())
        }
    }

    /**
     * Auto-discovers all `@Route`-annotated views and returns them.
     *
     * *WARNING*: it currently can not detect error views (components which implement the `HasErrorParameter` interface) - those need to be added manually.
     * @param packageName set the package name for the detector to be faster; or provide null to scan the whole classpath, but this is quite slow.
     */
    fun autoDiscoverViews(packageName: String? = null): Routes = apply {
        val detector = AnnotationDetector(object : AnnotationDetector.TypeReporter {
            override fun reportTypeAnnotation(annotation: Class<out Annotation>?, className: String?) {
                routes.add(Class.forName(className).asSubclass(Component::class.java))
            }

            override fun annotations(): Array<out Class<out Annotation>> = arrayOf(Route::class.java)
        })
        if (packageName == null) {
            detector.detect()
        } else {
            detector.detect(packageName)
        }

        println("Auto-discovered views: ${routes.joinToString { it.simpleName }}")
    }
}

/**
 * Allows us to fire any Vaadin event on any Vaadin component.
 * @receiver the component, not null.
 * @param event the event, not null.
 */
fun Component._fireEvent(event: ComponentEvent<*>) {
    // fireEvent() is protected, gotta make it public
    val fireEvent = Component::class.java.getDeclaredMethod("fireEvent", ComponentEvent::class.java)
    fireEvent.isAccessible = true
    fireEvent.invoke(this, event)
}

/**
 * Determines the component's `label` (it's the HTML element's `label` property actually). Intended to be used for fields such as [TextField].
 */
var Component.label: String
    get() = element.getProperty("label") ?: ""
    set(value) {
        element.setProperty("label", if (value.isBlank()) null else value)
    }

/**
 * The Component's caption: [Button.text] for [Button], [label] for fields such as [TextField].
 */
var Component.caption: String
    get() = when (this) {
        is Button -> text
        else -> label
    }
    set(value) {
        when (this) {
            is Button -> text = value
            else -> label = value
        }
    }
/**
 * Workaround for https://github.com/vaadin/flow/issues/664
 */
var Component.id_: String?
    get() = id.orElse(null)
    set(value) { setId(value) }

val Component.isAttached: Boolean
    get() = ui.orElse(null)?.session != null

val IntRange.size: Int get() = (endInclusive + 1 - start).coerceAtLeast(0)

val Component._isVisible: Boolean get() = when (this) {
    is Text -> !text.isNullOrBlank()   // workaround for https://github.com/vaadin/flow/issues/3201
    else -> isVisible
}

/**
 * Returns direct text contents (it doesn't peek into the child elements).
 */
val Component._text: String? get() = when (this) {
    is HasText -> text
    is Text -> text   // workaround for https://github.com/vaadin/flow/issues/3606
    else -> null
}

/**
 * Clicks the button, but only if it is actually possible to do so by the user. If the button is read-only or disabled, it throws an exception.
 * @throws IllegalArgumentException if the button was not visible, not enabled, read-only or if no button (or too many buttons) matched.
 */
fun Button._click() {
    checkEditableByUser()
    // click()  // can't call this since this calls JS method on the browser... but we're server-testing and there is no browser and this call would do nothing.
    _fireEvent(ClickEvent<Button>(this))
}

private fun Component.checkEditableByUser() {
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

private fun Component.isEffectivelyVisible(): Boolean = isVisible && (!parent.isPresent || parent.get().isEffectivelyVisible())

/**
 * This function actually works, as opposed to [Element.getTextRecursively].
 */
val Element.textRecursively2: String get() {
    // remove when this is fixed: https://github.com/vaadin/flow/issues/3668
    val node = ElementUtil.toJsoup(Document(""), this)
    return node.textRecursively
}

val Node.textRecursively: String get() = when (this) {
    is TextNode -> this.text()
    else -> childNodes().joinToString(separator = "", transform = { it.textRecursively })
}

internal fun Component.isEffectivelyEnabled(): Boolean = isEnabled && (!parent.isPresent || parent.get().isEffectivelyEnabled())

val Component.isEnabled: Boolean get() = when (this) {
    is HasEnabled -> isEnabled
    else -> true
}

/**
 * Sets the value of given component, but only if it is actually possible to do so by the user.
 * If the component is read-only or disabled, an exception is thrown.
 * @throws IllegalStateException if the field was not visible, not enabled or was read-only.
 */
var <V, E: HasValue.ValueChangeEvent<V>> HasValue<E, V>._value: V?
    get() = value
    set(v) {
        (this as Component).checkEditableByUser()
        value = v
    }
