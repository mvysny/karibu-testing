@file:Suppress("FunctionName")

package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.*
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.html.Input
import com.vaadin.flow.component.textfield.PasswordField
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.dom.DomEvent
import com.vaadin.flow.dom.Element
import com.vaadin.flow.dom.ElementUtil
import com.vaadin.flow.internal.nodefeature.ElementListenerMap
import com.vaadin.flow.router.HasErrorParameter
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.startup.ApplicationRouteRegistry
import com.vaadin.flow.server.startup.RouteRegistryInitializer
import elemental.json.Json
import elemental.json.JsonObject
import io.github.classgraph.ClassGraph
import io.github.classgraph.ScanResult
import org.jsoup.nodes.Document
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import java.io.Serializable
import javax.servlet.ServletContext
import kotlin.test.fail

/**
 * A configuration object of all routes and error routes in the application. Simply use [autoDiscoverViews] to discover everything.
 *
 * To speed up the tests, you can create one instance of this class only, then reuse that instance in every
 * call to [MockVaadin.setup].
 * @property routes a list of all route views in your application. Vaadin will ignore any routes not present here.
 * @property errorRoutes a list of all route views in your application. Vaadin will ignore any routes not present here.
 */
data class Routes(val routes: MutableSet<Class<out Component>> = mutableSetOf(),
                  val errorRoutes: MutableSet<Class<out HasErrorParameter<*>>> = mutableSetOf()): Serializable {

    /**
     * Manually register error routes. No longer needed since [autoDiscoverViews] can now detect error routes.
     */
    @Deprecated("No longer needed, error routes are now auto-detected with autoDiscoverViews()", ReplaceWith(""))
    fun addErrorRoutes(vararg routes: Class<out HasErrorParameter<*>>): Routes = apply {
        errorRoutes.addAll(routes.toSet())
    }

    /**
     * Registers all routes to Vaadin 13 registry. Automatically called from [MockVaadin.setup].
     */
    @Suppress("UNCHECKED_CAST")
    fun register(sc: ServletContext) {
        RouteRegistryInitializer().onStartup(routes.toSet(), sc)
        ApplicationRouteRegistry.getInstance(sc).setErrorNavigationTargets(errorRoutes.map { it as Class<out Component> } .toSet())
    }

    /**
     * Auto-discovers everything, registers it into `this` and returns `this`.
     * * [Route]-annotated views
     * * [HasErrorParameter] error views
     * @param packageName set the package name for the detector to be faster; or provide null to scan the whole classpath, but this is quite slow.
     * @param autoDetectErrorRoutes if false then [HasErrorParameter] error views are not auto-detected. This emulates
     * the old behavior of this method.
     * @return this
     */
    @JvmOverloads
    fun autoDiscoverViews(packageName: String? = null, autoDetectErrorRoutes: Boolean = true): Routes = apply {
        val scan: ScanResult = ClassGraph().enableClassInfo()
                .enableAnnotationInfo()
                .whitelistPackages(*(if (packageName == null) arrayOf() else arrayOf(packageName))).scan()
        scan.use { scanResult ->
            scanResult.getClassesWithAnnotation(Route::class.java.name).mapTo(routes) { info ->
                Class.forName(info.name).asSubclass(Component::class.java)
            }
            if (autoDetectErrorRoutes) {
                scanResult.getClassesImplementing(HasErrorParameter::class.java.name).mapTo(errorRoutes) { info ->
                    Class.forName(info.name).asSubclass(HasErrorParameter::class.java)
                }
            }
        }

        println("Auto-discovered views: $this")
    }

    override fun toString(): String =
            "Routes(routes=${routes.joinToString { it.simpleName }}, errorRoutes=${errorRoutes.joinToString { it.simpleName }})"
}

/**
 * Allows us to fire any Vaadin event on any Vaadin component.
 * @receiver the component, not null.
 * @param event the event, not null.
 */
fun Component._fireEvent(event: ComponentEvent<*>) {
    ComponentUtil.fireEvent(this, event)
}

/**
 * Fires a DOM [event] on this component.
 */
fun Element._fireDomEvent(event: DomEvent) {
    node.getFeature(ElementListenerMap::class.java).fireEvent(event)
}

/**
 * Fires a DOM event on this component.
 * @param eventType the event type, e.g. "click"
 * @param eventData optional event data, defaults to an empty object.
 */
fun Component._fireDomEvent(eventType: String, eventData: JsonObject = Json.createObject()) {
    element._fireDomEvent(DomEvent(element, eventType, eventData))
}

/**
 * Determines the component's `label` (usually it's the HTML element's `label` property, but it's [Checkbox.getLabel] for checkbox).
 * Intended to be used for fields such as [TextField].
 */
var Component.label: String
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
 * The same as [Component.getId] but without Optional.
 *
 * Workaround for https://github.com/vaadin/flow/issues/664
 */
var Component.id_: String?
    get() = id.orElse(null)
    set(value) { setId(value) }

/**
 * Checks whether the component is attached to the UI and to the Session.
 */
val Component.isAttached: Boolean
    get() = ui.orElse(null)?.session != null

/**
 * Checks whether the component is visible (usually [Component.isVisible] but for [Text]
 * the text must be non-empty).
 */
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
fun Component.expectNotEditableByUser() {
    try {
        checkEditableByUser()
        fail("The ${toPrettyString()} is editable")
    } catch (ex: IllegalStateException) {
        // okay
    }
}

internal fun Component.isEffectivelyVisible(): Boolean = _isVisible && (!parent.isPresent || parent.get().isEffectivelyVisible())

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

/**
 * Computes that this component and all of its parents are enabled.
 * @return false if this component or any of its parent is disabled.
 */
fun Component.isEffectivelyEnabled(): Boolean = isEnabled && (!parent.isPresent || parent.get().isEffectivelyEnabled())

/**
 * Checks whether this component is [HasEnabled.isEnabled]. All components not implementing [HasEnabled] are considered enabled.
 */
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

// modify when this is fixed: https://github.com/vaadin/flow/issues/4068
var Component.placeholder: String?
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
fun Component.removeFromParent() {
    (parent.orElse(null) as? HasComponents)?.remove(this)
}

/**
 * Checks whether this component matches given spec. All rules are matched except the [count] rule. The
 * rules are matched against given component only (not against its children).
 */
fun Component.matches(spec: SearchSpec<Component>.()->Unit): Boolean =
        SearchSpec(Component::class.java).apply { spec() }.toPredicate().invoke(this)

/**
 * Fires [FocusNotifier.FocusEvent] on the component, but only if it's editable.
 */
fun <T> T._focus() where T: Focusable<*>, T: Component{
    checkEditableByUser()
    _fireEvent(FocusNotifier.FocusEvent<T>(this, true))
}

/**
 * Fires [BlurNotifier.BlurEvent] on the component, but only if it's editable.
 */
fun <T> T._blur() where T: Focusable<*>, T: Component {
    checkEditableByUser()
    _fireEvent(BlurNotifier.BlurEvent<T>(this, true))
}
