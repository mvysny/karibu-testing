package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.page.Page
import com.vaadin.flow.router.Location
import com.vaadin.flow.router.NavigationTrigger
import com.vaadin.flow.server.RouteRegistry
import kotlin.test.expect
import kotlin.test.fail

/**
 * Returns the browser's current path. Returns null if there is no current UI.
 */
public val currentPath: String? get() {
    return UI.getCurrent()?.internals?.activeViewLocation?.pathWithQueryParameters
}

/**
 * Returns the current view
 */
public val currentView: Class<out Component>? get() {
    var path: String = (currentPath ?: return null).trim('/')
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

/**
 * Navigates to any kind of link within the [currentUI], including optional query parameters:
 * * "" (empty string)
 * * `foo/bar` - any view
 * * `foo/25` - any view with parameters
 * * `foo/25?token=bar` - any view with parameters and query parameters
 * * `?token=foo` - the root view with query parameters
 */
public fun navigateTo(location: String) {
    currentUI.internals.router.navigate(currentUI, Location(location), NavigationTrigger.UI_NAVIGATE)
}
