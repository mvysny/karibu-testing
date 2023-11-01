package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.kaributesting.v10.mock.MockVaadinHelper
import com.github.mvysny.kaributools.VaadinVersion
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import com.vaadin.flow.router.HasErrorParameter
import com.vaadin.flow.router.InternalServerError
import com.vaadin.flow.router.NotFoundException
import com.vaadin.flow.router.PreserveOnRefresh
import com.vaadin.flow.router.RouteNotFoundError
import com.vaadin.flow.server.VaadinContext
import com.vaadin.flow.server.VaadinSession
import com.vaadin.flow.server.startup.ApplicationRouteRegistry
import test.app.MyRouteNotFoundError
import java.lang.Boolean
import kotlin.test.expect

val allViews: Set<Class<out Component>> = setOf<Class<out Component>>(
    TestingView::class.java,
    HelloWorldView::class.java,
    WelcomeView::class.java,
    ParametrizedView::class.java,
    ChildView::class.java,
    NavigationPostponeView::class.java,
    PreserveOnRefreshView::class.java
)
val allErrorRoutes: Set<Class<out HasErrorParameter<*>>> = setOf(ErrorView::class.java, MockRouteNotFoundError::class.java)

@DynaTestDsl
fun DynaNodeGroup.routesTestBatch() {
    afterEach { MockVaadin.tearDown() }

    test("All views discovered") {
        val routes: Routes = Routes().autoDiscoverViews("com.github")
        expect(allViews) { routes.routes.toSet() }
        expect(allErrorRoutes) { routes.errorRoutes.toSet() }
    }

    test("calling autoDiscoverViews() multiple times won't fail") {
        repeat(5) {
            expect(allViews) { Routes().autoDiscoverViews("com.github").routes }
        }
    }

    // https://github.com/mvysny/karibu-testing/issues/50
    test("app-specific NotFoundException handler removes MockRouteNotFoundError") {
        val routes: Routes = Routes().autoDiscoverViews()
        // Vaadin 24.3+ introduces additional route
        val routeAccessDeniedError = if (VaadinVersion.get.isAtLeast(24, 3)) Class.forName("com.vaadin.flow.router.RouteAccessDeniedError") else null
        val expectedRouteClasses =
            setOf(ErrorView::class.java, InternalServerError::class.java, MyRouteNotFoundError::class.java, RouteNotFoundError::class.java, routeAccessDeniedError)
                .filterNotNull().toSet()
        expect(expectedRouteClasses) { routes.errorRoutes.toSet() }
        // make sure that Vaadin initializes properly with this set of views
        MockVaadin.setup(routes)
    }

    test("PWA is ignored by default") {
        val routes: Routes = Routes().autoDiscoverViews("com.github")
        val ctx: VaadinContext = MockVaadinHelper.createMockVaadinContext()
        routes.register(ctx)
        expect(null) {
            @Suppress("DEPRECATION")
            ApplicationRouteRegistry.getInstance(ctx).pwaConfigurationClass
        }
    }

    test("PWA is discovered properly if need be") {
        val routes: Routes = Routes().autoDiscoverViews("com.github")
        routes.skipPwaInit = false
        val ctx: VaadinContext = MockVaadinHelper.createMockVaadinContext()
        routes.register(ctx)
        expect(WelcomeView::class.java) {
            @Suppress("DEPRECATION")
            ApplicationRouteRegistry.getInstance(ctx).pwaConfigurationClass
        }
    }

    test("MockRouteNotFoundError is called when the route doesn't exist, and it fails immediately with an informative error message") {
        val routes: Routes = Routes().autoDiscoverViews("com.github")
        MockVaadin.setup(routes)
        expectThrows(NotFoundException::class, "No route found for 'A_VIEW_THAT_DOESNT_EXIST'\nAvailable routes:") {
            UI.getCurrent().navigate("A_VIEW_THAT_DOESNT_EXIST")
        }
    }
}
