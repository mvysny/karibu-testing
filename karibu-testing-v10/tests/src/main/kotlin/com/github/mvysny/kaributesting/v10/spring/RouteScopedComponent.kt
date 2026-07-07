package com.github.mvysny.kaributesting.v10.spring

import com.github.mvysny.kaributesting.v10.TestingView
import com.vaadin.flow.spring.annotation.RouteScope
import com.vaadin.flow.spring.annotation.RouteScopeOwner
import org.springframework.stereotype.Component

/**
 * Tests https://github.com/mvysny/karibu-testing/issues/184
 */
@RouteScope
@RouteScopeOwner(TestingView::class)
@Component
class RouteScopedComponent
