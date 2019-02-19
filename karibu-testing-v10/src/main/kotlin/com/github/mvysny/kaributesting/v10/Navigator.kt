package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.page.Page
import com.vaadin.flow.server.RouteRegistry
import kotlin.test.expect
import kotlin.test.fail

/**
 * Returns the browser's current path. Returns null if there is no current UI.
 */
val currentPath: String? get() {
    return UI.getCurrent()?.internals?.activeViewLocation?.pathWithQueryParameters
}

/**
 * Returns the current view
 */
val currentView: Class<out Component>? get() {
    val path = currentPath ?: return null
    val registry: RouteRegistry = UI.getCurrent().router.registry
    return registry.getNavigationTarget(currentPath).orElse(null)
}

/**
 * Expects that given [view] is the currently displayed view.
 */
fun <V: Component> expectView(view: Class<V>) {
    @Suppress("UNCHECKED_CAST")
    expect(view) { currentView }
}

/**
 * Expects that given view is the currently displayed view.
 */
inline fun <reified V: Component> expectView() = expectView(V::class.java)
