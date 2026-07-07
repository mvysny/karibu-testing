package org.myapp

import com.github.mvysny.kaributesting.v10.Routes
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Layout
import com.vaadin.flow.router.Route
import com.vaadin.flow.router.RouterLayout
import jakarta.annotation.security.RolesAllowed

val routes = Routes().autoDiscoverViews("org.myapp")

@Route(autoLayout = false)
internal class LoginView : VerticalLayout()
@Route("admin", autoLayout = false)
@RolesAllowed("admin")
internal class AdminView : VerticalLayout()

@Layout
class MainLayout : Div(), RouterLayout

/**
 * Should automatically include [MainLayout] as its layout:
 * https://vaadin.com/docs/latest/flow/routing/layout#automatic-layout-using-layout
 */
@Route("myroute")
internal class MyRoute : Div()
