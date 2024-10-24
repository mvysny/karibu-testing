package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.kaributesting.v10.mock.MockVaadinHelper
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import com.vaadin.flow.router.HasErrorParameter
import com.vaadin.flow.router.InternalServerError
import com.vaadin.flow.router.NotFoundException
import com.vaadin.flow.server.VaadinContext
import com.vaadin.flow.server.startup.ApplicationRouteRegistry
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import test.app.MyRouteNotFoundError
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
val allErrorRoutes: Set<Class<out HasErrorParameter<*>>> = setOf(ErrorView::class.java, MockRouteNotFoundError::class.java, MockRouteAccessDeniedError::class.java)

abstract class AbstractRoutesTests {
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Test fun `All views discovered`() {
        val routes: Routes = Routes().autoDiscoverViews("com.github")
        expect(allViews) { routes.routes.toSet() }
        expect(allErrorRoutes) { routes.errorRoutes.toSet() }
    }

    @Test fun `calling autoDiscoverViews() multiple times won't fail`() {
        repeat(5) {
            expect(allViews) { Routes().autoDiscoverViews("com.github").routes }
        }
    }

    // https://github.com/mvysny/karibu-testing/issues/50
    @Test fun `app-specific NotFoundException handler removes MockRouteNotFoundError`() {
        val routes: Routes = Routes().autoDiscoverViews()
        val expectedRouteClasses = setOf(ErrorView::class.java, InternalServerError::class.java, MyRouteNotFoundError::class.java, MockRouteAccessDeniedError::class.java)
        expect(expectedRouteClasses) { routes.errorRoutes.toSet() }
        // make sure that Vaadin initializes properly with this set of views
        MockVaadin.setup(routes)
    }

    @Test fun `PWA is ignored by default`() {
        val routes: Routes = Routes().autoDiscoverViews("com.github")
        val ctx: VaadinContext = MockVaadinHelper.createMockVaadinContext()
        routes.register(ctx)
        expect(null) {
            @Suppress("DEPRECATION")
            ApplicationRouteRegistry.getInstance(ctx).pwaConfigurationClass
        }
    }

    @Test fun `PWA is discovered properly if need be`() {
        val routes: Routes = Routes().autoDiscoverViews("com.github")
        routes.skipPwaInit = false
        val ctx: VaadinContext = MockVaadinHelper.createMockVaadinContext()
        routes.register(ctx)
        expect(WelcomeView::class.java) {
            @Suppress("DEPRECATION")
            ApplicationRouteRegistry.getInstance(ctx).pwaConfigurationClass
        }
    }

    @Test fun `MockRouteNotFoundError is called when the route doesn't exist, and it fails immediately with an informative error message`() {
        val routes: Routes = Routes().autoDiscoverViews("com.github")
        MockVaadin.setup(routes)
        expectThrows(NotFoundException::class, "No route found for 'A_VIEW_THAT_DOESNT_EXIST'\nAvailable routes:") {
            UI.getCurrent().navigate("A_VIEW_THAT_DOESNT_EXIST")
        }
    }
}
