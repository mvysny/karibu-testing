package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributools.navigateTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.myapp.MainLayout
import org.myapp.MyRoute

abstract class AbstractLayoutTests {
    companion object {
        lateinit var routes: Routes
        @BeforeAll @JvmStatic fun discoverRoutes() {
            routes = Routes().autoDiscoverViews("org.myapp")
        }
    }
    @BeforeEach fun fakeVaadin() { MockVaadin.setup(routes) }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Test fun automaticLayouting() {
        navigateTo<MyRoute>()
        _expectOne<MyRoute>()
        _expectOne<MainLayout>()
    }
}
