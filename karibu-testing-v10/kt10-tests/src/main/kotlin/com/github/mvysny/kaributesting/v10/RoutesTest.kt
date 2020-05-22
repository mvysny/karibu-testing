package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.kaributesting.mockhttp.MockContext
import com.vaadin.flow.server.startup.ApplicationRouteRegistry
import kotlin.test.expect

fun DynaNodeGroup.routesTestBatch() {
    test("All views discovered") {
        val routes: Routes = Routes().autoDiscoverViews("com.github")
        expect(allViews) { routes.routes.toSet() }
        expect(setOf(ErrorView::class.java)) { routes.errorRoutes.toSet() }
    }

    test("PWA is ignored by default") {
        val routes: Routes = Routes().autoDiscoverViews("com.github")
        val ctx = MockContext()
        routes.register(ctx)
        expect(null) { ApplicationRouteRegistry.getInstance(ctx).pwaConfigurationClass }
    }

    test("PWA is discovered properly if need be") {
        val routes: Routes = Routes().autoDiscoverViews("com.github")
        routes.skipPwaInit = false
        val ctx = MockContext()
        routes.register(ctx)
        expect(WelcomeView::class.java) { ApplicationRouteRegistry.getInstance(ctx).pwaConfigurationClass }
    }
}
