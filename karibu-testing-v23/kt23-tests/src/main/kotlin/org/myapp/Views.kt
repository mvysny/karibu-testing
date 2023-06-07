package org.myapp

import com.github.mvysny.kaributesting.v10.Routes
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route
import jakarta.annotation.security.RolesAllowed

val routes = Routes().autoDiscoverViews("org.myapp")

@Route
internal class LoginView : VerticalLayout()
@Route("admin")
@RolesAllowed("admin")
internal class AdminView : VerticalLayout()
