package org.myapp

import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route
import jakarta.annotation.security.RolesAllowed

@Route
internal class LoginView : VerticalLayout()
@Route
@RolesAllowed("admin")
internal class AdminView : VerticalLayout()
