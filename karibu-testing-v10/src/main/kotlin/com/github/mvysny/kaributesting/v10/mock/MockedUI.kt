package com.github.mvysny.kaributesting.v10.mock

import com.github.mvysny.kaributesting.v10.toPrettyString
import com.vaadin.flow.component.UI
import com.vaadin.flow.router.NotFoundException
import com.vaadin.flow.router.RouteData

/**
 * A simple no-op UI used by default by [com.github.mvysny.kaributesting.v10.MockVaadin.setup]. The class is open, in order to be extensible in user's library
 */
public open class MockedUI : UI() {
    override fun navigateToClient(clientRoute: String) {
        // Vaadin 24.1.0.alpha3+: if navigating to a non-existing route, the MockRouteNotFoundError is no
        // longer consulted directly. Instead, the browser is asked to perform the navigation (via this method),
        // which then probably bubbles back to the server and finally runs MockRouteNotFoundError.
        // However, with KaribuTesting, there's no browser...
        val message: String = buildString {
            val path: String = clientRoute
            append("No route found for '").append(path).append("'")
            append("\nAvailable routes: ")
            val routes: List<RouteData> = internals.router.registry.registeredRoutes
            append(routes.map { it.toPrettyString() })
        }
        throw NotFoundException(message)
    }
}
