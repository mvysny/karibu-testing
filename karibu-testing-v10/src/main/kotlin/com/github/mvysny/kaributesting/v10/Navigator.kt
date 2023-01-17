package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import com.vaadin.flow.server.RouteRegistry
import kotlin.test.expect

/**
 * Returns the path of the current view as shown in browser's URL bar. Includes query parameters and all.
 * For example, for `http://localhost:8080/my/view?foo=bar` returns `my/view?foo=bar`.
 * Returns null if there is no current UI.
 *
 * Doesn't take [com.vaadin.flow.component.page.History.replaceState] or others into account.
 * The reason is that changing browser's history only manipulates the URL but doesn't
 * cause the browser to navigate and change a view.
 */
public val currentPath: String? get() =
    UI.getCurrent()?.internals?.activeViewLocation?.pathWithQueryParameters

/**
 * Returns the current view class. Returns null if the [UI.getCurrent] is null, or no such view
 * can not be found in the registry.
 */
public val currentView: Class<out Component>? get() {
    var path: String = (currentPath ?: return null).trim('/')
    // no other way but to scan the route registry: https://github.com/vaadin/flow/issues/4565

    // remove any query parameters
    path = path.substringBefore('?')
    val registry: RouteRegistry = UI.getCurrent().internals.router.registry
    val segments: List<String> = path.split('/')
    for (prefix: Int in segments.size downTo 1) {
        val p: String = segments.subList(0, prefix).joinToString("/")
        val s: List<String> = segments.subList(prefix, segments.size)
        val clazz: Class<out Component>? = registry.getNavigationTarget(p, s).orElse(null)
        if (clazz != null) {
            return clazz
        }
    }
    return null
}

/**
 * Expects that given [view] is the currently displayed view.
 */
public fun <V: Component> expectView(view: Class<V>) {
    @Suppress("UNCHECKED_CAST")
    expect(view) { currentView }
}

/**
 * Expects that given view is the currently displayed view.
 */
public inline fun <reified V: Component> expectView() {
    expectView(V::class.java)
}
