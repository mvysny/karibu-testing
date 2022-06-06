@file:Suppress("FunctionName")

package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributools.DepthFirstTreeIterator
import com.github.mvysny.kaributools.caption
import com.github.mvysny.kaributools.placeholder
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasStyle
import com.vaadin.flow.component.HasValue
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.polymertemplate.PolymerTemplate
import com.vaadin.flow.router.InternalServerError
import java.util.*
import java.util.function.Predicate

/**
 * A criterion for matching components. The component must match all of non-null fields.
 *
 * You can add more properties, simply by creating a write-only property which will register a new [predicate] on write. See
 * [Adding support for custom search criteria](https://github.com/mvysny/karibu-testing/tree/master/karibu-testing-v10#adding-support-for-custom-search-criteria)
 * for more details.
 * @property clazz the class of the component we are searching for.
 * @property id the required [Component.getId]; if null, no particular id is matched.
 * @property caption the required [Component.caption]; if null, no particular caption is matched.
 * @property placeholder the required [Component.placeholder]; if null, no particular placeholder is matched.
 * @property text the [com.vaadin.flow.dom.Element.getText]
 * @property count expected count of matching components, defaults to `0..Int.MAX_VALUE`
 * @property value expected [com.vaadin.flow.component.HasValue.getValue]; if `null`, no particular value is matched.
 * @property classes if not null, the component must match all of these class names. Space-separated.
 * @property withoutClasses if not null, the component must NOT match any of these class names. Space-separated.
 * @property themes if not null, the component must have all theme names defined. Space-separated
 * @property withoutThemes if not null, the component must NOT have any of the theme names defined. Space-separated
 * @property predicates the predicates the component needs to match, not null. May be empty - in such case it is ignored. By default empty.
 */
public class SearchSpec<T : Component>(
        public val clazz: Class<T>,
        public var id: String? = null,
        public var caption: String? = null,
        public var placeholder: String? = null,
        public var text: String? = null,
        public var count: IntRange = 0..Int.MAX_VALUE,
        public var value: Any? = null,
        public var classes: String? = null,
        public var withoutClasses: String? = null,
        public var themes: String? = null,
        public var withoutThemes: String? = null,
        public var predicates: MutableList<Predicate<T>> = mutableListOf()
) {

    override fun toString(): String {
        val list = mutableListOf<String>(if (clazz.simpleName.isBlank()) clazz.name else clazz.simpleName)
        if (id != null) list.add("id='$id'")
        if (caption != null) list.add("caption='$caption'")
        if (placeholder != null) list.add("placeholder='$placeholder'")
        if (text != null) list.add("text='$text'")
        if (!classes.isNullOrBlank()) list.add("classes='$classes'")
        if (!withoutClasses.isNullOrBlank()) list.add("withoutClasses='$withoutClasses'")
        if (!themes.isNullOrBlank()) list.add("themes='$themes'")
        if (!withoutThemes.isNullOrBlank()) list.add("withoutThemes='$withoutThemes'")
        if (value != null) list.add("value=$value")
        if (count != (0..Int.MAX_VALUE) && count != 1..1) list.add("count=$count")
        list.addAll(predicates.map { it.toString() })
        return list.joinToString(" and ")
    }

    /**
     * Returns a predicate which matches components based on this spec. All rules are matched except the [count] rule. The
     * rules are matched against given component only (not against its children).
     */
    @Suppress("UNCHECKED_CAST")
    public fun toPredicate(): (Component) -> Boolean {
        val p = mutableListOf<(Component)->Boolean>()
        p.add { component -> clazz.isInstance(component)}
        if (id != null) p.add { component -> component.id_ == id }
        if (caption != null) p.add { component -> component.caption == caption }
        if (placeholder != null) p.add { component -> component.placeholder == placeholder }
        if (!classes.isNullOrBlank()) p.add { component -> component.hasAllClasses(classes!!) }
        if (!withoutClasses.isNullOrBlank()) p.add { component -> component.doesntHaveAnyClasses(withoutClasses!!) }
        if (!themes.isNullOrBlank()) p.add { component -> component.hasAllThemes(themes!!) }
        if (!withoutThemes.isNullOrBlank()) p.add { component -> component.notContainsThemes(withoutThemes!!) }
        if (text != null) p.add { component -> component.element.text == text }
        if (value != null) p.add { component -> (component as? HasValue<*, *>)?.getValue() == value }
        p.addAll(predicates.map { predicate -> { component: Component -> clazz.isInstance(component) && predicate.test(component as T) } })
        return p.and()
    }
}

public fun Iterable<String?>.filterNotBlank(): List<String> = filterNotNull().filter { it.isNotBlank() }

private fun Component.hasAllClasses(classes: String): Boolean {
    if (this !is HasStyle) return false
    return classes.split(' ').filterNotBlank().all { classNames.contains(it) }
}
private fun Component.doesntHaveAnyClasses(classes: String): Boolean {
    if (this !is HasStyle) return true
    return classes.split(' ').filterNotBlank().all { !classNames.contains(it) }
}

private fun Component.hasAllThemes(themes: String): Boolean {
    return themes.split(' ').filterNotBlank().all { element.themeList.contains(it) }
}

private fun Component.notContainsThemes(themes: String): Boolean {
    return themes.split(' ').filterNotBlank().all { !element.themeList.contains(it) }
}

/**
 * Finds a VISIBLE component of given type which matches given [block]. This component and all of its descendants are searched.
 * @param block the search specification
 * @return the only matching component, never null.
 * @throws IllegalArgumentException if no component matched, or if more than one component matches.
 */
public inline fun <reified T: Component> Component._get(noinline block: SearchSpec<T>.()->Unit = {}): T = this._get(T::class.java, block)

/**
 * Finds a VISIBLE component of given [clazz] which matches given [block]. This component and all of its descendants are searched.
 * @param clazz the component must be of this class.
 * @param block the search specification
 * @return the only matching component, never null.
 * @throws IllegalArgumentException if no component matched, or if more than one component matches.
 */
public fun <T: Component> Component._get(clazz: Class<T>, block: SearchSpec<T>.()->Unit = {}): T {
    val result: List<T> = _find(clazz) {
        count = 1..1
        block()
        check(count == 1..1) { "You're calling _get which is supposed to return exactly 1 component, yet you tried to specify the count of $count" }
    }
    return clazz.cast(result.single())
}

/**
 * Finds a VISIBLE component in the current UI of given type which matches given [block]. The [currentUI] and all of its descendants are searched.
 * @return the only matching component, never null.
 * @throws IllegalArgumentException if no component matched, or if more than one component matches.
 */
public inline fun <reified T: Component> _get(noinline block: SearchSpec<T>.()->Unit = {}): T =
    _get(T::class.java, block)

/**
 * Finds a VISIBLE component in the current UI of given [clazz] which matches given [block]. The [currentUI] and all of its descendants are searched.
 * @param clazz the component must be of this class.
 * @param block the search specification
 * @return the only matching component, never null.
 * @throws IllegalArgumentException if no component matched, or if more than one component matches.
 */
public fun <T: Component> _get(clazz: Class<T>, block: SearchSpec<T>.()->Unit = {}): T = currentUI._get(clazz, block)

/**
 * Finds a list of VISIBLE components of given [clazz] which matches [block]. This component and all of its descendants are searched.
 * @return the list of matching components, may be empty.
 */
public fun <T: Component> Component._find(clazz: Class<T>, block: SearchSpec<T>.()->Unit = {}): List<T> {
    val spec: SearchSpec<T> = SearchSpec(clazz)
    spec.block()
    val result: List<Component> = find(spec.toPredicate())
    if (result.size !in spec.count) {
        val loc: String = currentPath ?: "?"
        var message: String = when {
            result.isEmpty() -> "/$loc: No visible ${clazz.simpleName}"
            result.size < spec.count.first -> "/$loc: Too few (${result.size}) visible ${clazz.simpleName}s"
            else -> "/$loc: Too many visible ${clazz.simpleName}s (${result.size})"
        }
        message = "$message in ${toPrettyString()} matching $spec: [${result.joinToString { it.toPrettyString() }}]. Component tree:\n${toPrettyTree()}"

        // if there's a PolymerTemplate, warn that Karibu-Testing can't really locate components in there:
        // https://github.com/mvysny/karibu-testing/tree/master/karibu-testing-v10#polymer-templates
        // fixes https://github.com/mvysny/karibu-testing/issues/35
        val hasPolymerTemplates: Boolean = _walkAll().any { it is PolymerTemplate<*> }
        if (hasPolymerTemplates) {
            message = "$message\nWarning: Karibu-Testing is not able to look up components from inside of PolymerTemplate. Please see https://github.com/mvysny/karibu-testing/tree/master/karibu-testing-v10#polymer-templates for more details."
        }

        // find() used to fail with IllegalArgumentException which makes sense for a general-purpose utility method. However,
        // since find() is used in tests overwhelmingly, not finding the correct set of components is generally treated as an assertion error.
        throw AssertionError(message)
    }
    return result.filterIsInstance(clazz)
}

/**
 * Finds a list of VISIBLE components of given type which matches [block]. This component and all of its descendants are searched.
 * @return the list of matching components, may be empty.
 */
public inline fun <reified T: Component> Component._find(noinline block: SearchSpec<T>.()->Unit = {}): List<T> = this._find(T::class.java, block)

/**
 * Finds a list of VISIBLE components in the current UI of given type which matches given [block]. The [UI.getCurrent] and all of its descendants are searched.
 * @param block the search specification
 * @return the list of matching components, may be empty.
 */
public inline fun <reified T: Component> _find(noinline block: SearchSpec<T>.()->Unit = {}): List<T> =
    _find(T::class.java, block)

/**
 * Finds a list of VISIBLE components of given [clazz] which matches [block]. The [UI.getCurrent] and all of its descendants are searched.
 * @return the list of matching components, may be empty.
 */
public fun <T: Component> _find(clazz: Class<T>, block: SearchSpec<T>.()->Unit = {}): List<T> =
        currentUI._find(clazz, block)

private fun Component.find(predicate: (Component)->Boolean): List<Component> {
    testingLifecycleHook.awaitBeforeLookup()
    val descendants: List<Component> = _walkAll().toList()
    testingLifecycleHook.awaitAfterLookup()
    val error: InternalServerError? = descendants.filterIsInstance<InternalServerError>().firstOrNull()
    if (error != null) {
        throw AssertionError("An internal server error occurred; please check log for the actual stack-trace. Error text: ${error.errorMessage}\n${currentUI.toPrettyTree()}")
    }
    return descendants.filter { it.isEffectivelyVisible() && predicate(it) }
}

/**
 * `AND`s all predicates.
 */
private fun <T> Iterable<(T) -> Boolean>.and(): (T) -> Boolean =
    { component -> all { it(component) } }

/**
 * Walks the component child/descendant tree, depth-first: first the component, then its descendants,
 * then its next sibling.
 */
public fun Component._walkAll(): Iterable<Component> = Iterable {
    DepthFirstTreeIterator(this) { component: Component -> testingLifecycleHook.getAllChildren(component) }
}

/**
 * Expects that there are no VISIBLE components of given type which matches [block]. This component and all of its descendants are searched.
 * @throws IllegalArgumentException if one or more components matched.
 */
public inline fun <reified T: Component> Component._expectNone(noinline block: SearchSpec<T>.()->Unit = {}) {
    this._expectNone(T::class.java, block)
}

/**
 * Expects that there are no VISIBLE components of given [clazz] which matches [block]. This component and all of its descendants are searched.
 * @throws IllegalArgumentException if one or more components matched.
 */
public fun <T: Component> Component._expectNone(clazz: Class<T>, block: SearchSpec<T>.()->Unit = {}) {
    val result: List<T> = _find(clazz) {
        count = 0..0
        block()
        check(count == 0..0) { "You're calling _expectNone which expects 0 component, yet you tried to specify the count of $count" }
    }
    check(result.isEmpty()) // safety check that _find works as expected
}

/**
 * Expects that there are no dialogs shown.
 */
public fun _expectNoDialogs() {
    _expectNone<Dialog>()
}

/**
 * Expects that there are no VISIBLE components in the current UI of given type which matches [block]. The [currentUI] and all of its descendants are searched.
 * @throws IllegalArgumentException if one or more components matched.
 */
public inline fun <reified T: Component> _expectNone(noinline block: SearchSpec<T>.()->Unit = {}) {
    _expectNone(T::class.java, block)
}

/**
 * Expects that there are no VISIBLE components in the current UI of given [clazz] which matches [block]. The [currentUI] and all of its descendants are searched.
 * @throws IllegalArgumentException if one or more components matched.
 */
public fun <T: Component> _expectNone(clazz: Class<T>, block: SearchSpec<T>.()->Unit = {}) {
    currentUI._expectNone(clazz, block)
}

/**
 * Expects that there is exactly one VISIBLE components of given type which matches [block]. This component and all of its descendants are searched.
 * @throws AssertionError if none, or more than one components matched.
 */
public inline fun <reified T : Component> Component._expectOne(noinline block: SearchSpec<T>.() -> Unit = {}) {
    this._expectOne(T::class.java, block)
}

/**
 * Expects that there is exactly one VISIBLE components of given [clazz] which matches [block]. This component and all of its descendants are searched.
 * @throws AssertionError if none, or more than one components matched.
 */
public fun <T : Component> Component._expectOne(clazz: Class<T>, block: SearchSpec<T>.() -> Unit = {}) {
    // technically _expectOne is the same as _get, but the semantics differ - with _get() we're "just" doing a lookup (and asserting on
    // the component later). _expectOne() explicitly declares in the test sources that we want to check that there is exactly one such component.
    _get(clazz, block)
}

/**
 * Expects that there is exactly one VISIBLE components in the current UI of given type which matches [block]. The [currentUI] and all of its descendants are searched.
 * @throws AssertionError if none, or more than one components matched.
 */
public inline fun <reified T : Component> _expectOne(noinline block: SearchSpec<T>.() -> Unit = {}) {
    _expectOne(T::class.java, block)
}

/**
 * Expects that there is exactly one VISIBLE components in the current UI of given [clazz] which matches [block]. The [currentUI] and all of its descendants are searched.
 * @throws AssertionError if none, or more than one components matched.
 */
public fun <T : Component> _expectOne(clazz: Class<T>, block: SearchSpec<T>.() -> Unit = {}) {
    currentUI._expectOne(clazz, block)
}

/**
 * Expects that there are exactly [count] VISIBLE components matching [block]. This component and all of its descendants are searched. Examples:
 * ```
 * // check that there are 5 buttons in a button bar
 * buttonBar._expect<Button>(5..5)
 * // check that there are either 3, 4 or 5 vertical layouts in the UI with given class
 * _expect<VerticalLayout>{ count = 3..5; styles = "menubar" }
 * ```
 * Special cases: for asserting one component use [_expectOne]. For asserting no components use [_expectNone].
 * @throws AssertionError if incorrect count of component matched.
 */
public inline fun <reified T : Component> Component._expect(count: Int = 1, noinline block: SearchSpec<T>.() -> Unit = {}) {
    this._expect(T::class.java, count, block)
}

/**
 * Expects that there are exactly [count] VISIBLE components of given [clazz] match [block]. This component and all of its descendants are searched. Examples:
 * ```
 * // check that there are 5 buttons in a button bar
 * buttonBar._expect<Button>(5)
 * // check that there are either 3, 4 or 5 vertical layouts in the UI with given class
 * _expect<VerticalLayout>{ count = 3..5; styles = "menubar" }
 * ```
 * Special cases: for asserting one component use [_expectOne]. For asserting no components use [_expectNone].
 * @throws AssertionError if incorrect count of component matched.
 */
public fun <T : Component> Component._expect(clazz: Class<T>, count: Int = 1, block: SearchSpec<T>.() -> Unit = {}) {
    // technically _expect is the same as _find, but the semantics differ - with _find() we're "just" doing a lookup (and asserting on
    // the components later). _expect() explicitly declares in the test sources that we want to check that there are exactly x components that match given spec.
    _find(clazz) {
        this.count = count..count
        block()
    }
}

/**
 * Expects that there are exactly [count] VISIBLE components in the current UI match [block]. The [currentUI] and all of its descendants are searched. Examples:
 * ```
 * // check that there are 5 buttons in a button bar
 * buttonBar._expect<Button>(5..5)
 * // check that there are either 3, 4 or 5 vertical layouts in the UI with given class
 * _expect<VerticalLayout>{ count = 3..5; styles = "menubar" }
 * ```
 * Special cases: for asserting one component use [_expectOne]. For asserting no components use [_expectNone].
 * @throws AssertionError if incorrect count of component matched.
 */
public inline fun <reified T : Component> _expect(count: Int = 1, noinline block: SearchSpec<T>.() -> Unit = {}) {
    _expect(T::class.java, count, block)
}

/**
 * Expects that there are exactly [count] VISIBLE components in the current UI with given [clazz] match [block]. The [currentUI] and all of its descendants are searched. Examples:
 * ```
 * // check that there are 5 buttons in a button bar
 * buttonBar._expect<Button>(5)
 * // check that there are either 3, 4 or 5 vertical layouts in the UI with given class
 * _expect<VerticalLayout>{ count = 3..5; styles = "menubar" }
 * ```
 * Special cases: for asserting one component use [_expectOne]. For asserting no components use [_expectNone].
 * They provide
 * a better error message in case of a failure, and also communicate your intent
 * as a test writer better in the test code.
 * @throws AssertionError if incorrect count of component matched.
 */
public fun <T : Component> _expect(clazz: Class<T>, count: Int = 1, block: SearchSpec<T>.() -> Unit = {}) {
    currentUI._expect(clazz, count, block)
}

/**
 * Asserts that the [InternalServerError] page is currently being shown, optionally
 * with given [expectedErrorMessage].
 */
public fun _expectInternalServerError(expectedErrorMessage: String = "") {
    testingLifecycleHook.awaitBeforeLookup()
    val descendants: List<Component> = currentUI._walkAll().toList()
    testingLifecycleHook.awaitAfterLookup()
    val error: InternalServerError? = descendants.filterIsInstance<InternalServerError>().firstOrNull()
    if (error == null) {
        throw AssertionError("Expected an internal server error but none happened. Component tree:\n${currentUI.toPrettyTree()}")
    }
    if (!error.errorMessage.contains(expectedErrorMessage)) {
        throw AssertionError("Expected InternalServerError with message '$expectedErrorMessage' but was '${error.errorMessage}'. Component tree:\n${currentUI.toPrettyTree()}")
    }
}

