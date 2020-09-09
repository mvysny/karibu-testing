package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.kaributesting.mockhttp.MockContext
import com.vaadin.flow.server.VaadinServletContext
import com.vaadin.flow.server.startup.ApplicationRouteRegistry
import kotlin.test.expect

fun DynaNodeGroup.routesTestBatch() {
    test("All views discovered") {
        val routes: Routes = Routes().autoDiscoverViews("com.github")
        expect(allViews) { routes.routes.toSet() }
        expect(allErrorRoutes) { routes.errorRoutes.toSet() }
    }

    test("PWA is ignored by default") {
        val routes: Routes = Routes().autoDiscoverViews("com.github")
        val ctx = VaadinServletContext(MockContext())
        routes.register(ctx)
        expect(null) {
            @Suppress("DEPRECATION")
            ApplicationRouteRegistry.getInstance(ctx).pwaConfigurationClass
        }
    }

    test("PWA is discovered properly if need be") {
        val routes: Routes = Routes().autoDiscoverViews("com.github")
        routes.skipPwaInit = false
        val ctx = VaadinServletContext(MockContext())
        routes.register(ctx)
        expect(WelcomeView::class.java) {
            @Suppress("DEPRECATION")
            ApplicationRouteRegistry.getInstance(ctx).pwaConfigurationClass
        }
    }
}
