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
 * Resolves route for given [path].
 * @param path e.g. `my/view?foo=bar`
 */
public fun resolveRoute(path: String): Class<out Component>? {
    // trim & remove any query parameters
    val trimmedPath = path.trim('/').substringBefore('?')
    val registry: RouteRegistry = currentUI.internals.router.registry
    val segments: List<String> = trimmedPath.split('/')
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
 * Returns the current view class.
 * @return class of the current view; `null` if no navigation was performed yet.
 */
public val currentView: Class<out Component>?
    get() {
        val first = currentUI.internals.activeRouterTargetsChain.firstOrNull() ?: return null
        return first.javaClass.asSubclass(Component::class.java)
    }

/**
 * Expects that given [view] is the currently displayed view.
 */
public fun expectView(view: Class<out Component>) {
    @Suppress("UNCHECKED_CAST")
    expect(view, "current path: '$currentPath'") { currentView }
}

/**
 * Expects that given view is the currently displayed view.
 */
public inline fun <reified V: Component> expectView() {
    expectView(V::class.java)
}
