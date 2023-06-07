package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.karibudsl.v10.VaadinDsl
import com.github.mvysny.karibudsl.v10.init
import com.github.mvysny.kaributesting.v23._click
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.sidenav.SideNav
import com.vaadin.flow.component.sidenav.SideNavItem
import org.myapp.AdminView
import org.myapp.LoginView
import org.myapp.routes
import kotlin.reflect.KClass
import kotlin.test.expect

@DynaTestDsl
fun DynaNodeGroup.sideNavTests() {
    beforeEach { MockVaadin.setup(routes) }
    afterEach { MockVaadin.tearDown() }

    test("lookup") {
        UI.getCurrent().sideNav {
            route(LoginView::class, VaadinIcon.QUESTION) {
                item("Hello!")
                item("Hello2!", "path") {
                    item("subitem")
                }
            }
            item("Hierarchy") {
                route(AdminView::class, VaadinIcon.QUESTION)
            }
        }
        _expectOne<SideNav>()
        _expectOne<SideNavItem> { label = "Hello!" }
        _expectOne<SideNavItem> { label = "Hello2!" }
        _expectOne<SideNavItem> { label = "subitem" }
        _expectOne<SideNavItem> { label = "Hierarchy" }
        _expectOne<SideNavItem> { label = "LoginView" }
        _expectOne<SideNavItem> { label = "AdminView" }
    }

    test("lookup2") {
        UI.getCurrent().sideNav("label23")
        _expectOne<SideNav> { label = "label23" }
        _expectNone<SideNav> { label = "label22" }
    }

    test("click()") {
        SideNavItem("foo", LoginView::class.java)._click()
        _expectOne<LoginView>()
        SideNavItem("foo", "admin")._click()
        _expectOne<AdminView>()
    }

    test("toPrettyString()") {
        expect("SideNav[label='label']") { SideNav("label").toPrettyString() }
        expect("SideNavItem[@path='login', label='foo']") { SideNavItem("foo", LoginView::class.java).toPrettyString() }
        expect("SideNavItem[@path='admin', label='bar']") { SideNavItem("bar", "admin").toPrettyString() }
    }
}

@VaadinDsl
public fun (@VaadinDsl HasComponents).sideNav(
    label: String? = null,
    block: (@VaadinDsl SideNav).() -> Unit = {}
): SideNav =
    init(SideNav(label), block)

@VaadinDsl
public fun (@VaadinDsl SideNav).route(
    routeClass: KClass<out Component>,
    icon: VaadinIcon? = null,
    block: (@VaadinDsl SideNavItem).() -> Unit = {}
): SideNavItem {
    val item = SideNavItem(routeClass.java.simpleName, routeClass.java, icon?.create())
    block(item)
    addItem(item)
    return item
}

@VaadinDsl
public fun (@VaadinDsl SideNav).item(
    title: String,
    path: String? = null,
    block: (@VaadinDsl SideNavItem).() -> Unit = {}
): SideNavItem {
    val item = SideNavItem(title, path)
    block(item)
    addItem(item)
    return item
}

@VaadinDsl
public fun (@VaadinDsl SideNavItem).route(
    routeClass: KClass<out Component>,
    icon: VaadinIcon? = null,
    block: (@VaadinDsl SideNavItem).() -> Unit = {}
): SideNavItem {
    val item = SideNavItem(routeClass.java.simpleName, routeClass.java, icon?.create())
    block(item)
    addItem(item)
    return item
}

@VaadinDsl
public fun (@VaadinDsl SideNavItem).item(
    title: String,
    path: String? = null,
    block: (@VaadinDsl SideNavItem).() -> Unit = {}
): SideNavItem {
    val item = SideNavItem(title, path)
    block(item)
    addItem(item)
    return item
}
