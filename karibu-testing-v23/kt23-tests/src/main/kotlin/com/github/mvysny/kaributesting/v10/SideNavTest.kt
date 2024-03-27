package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.karibudsl.v23.item
import com.github.mvysny.karibudsl.v23.route
import com.github.mvysny.karibudsl.v23.sideNav
import com.github.mvysny.kaributesting.v23._click
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.sidenav.SideNav
import com.vaadin.flow.component.sidenav.SideNavItem
import com.vaadin.flow.router.NavigationTrigger
import org.myapp.AdminView
import org.myapp.LoginView
import org.myapp.routes
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
        lateinit var trigger: NavigationTrigger
        UI.getCurrent().addBeforeEnterListener { e -> trigger = e.trigger }
        SideNavItem("foo", LoginView::class.java)._click()
        _expectOne<LoginView>()
        expect(NavigationTrigger.CLIENT_SIDE) { trigger }
        SideNavItem("foo", "admin")._click()
        _expectOne<AdminView>()
    }

    test("toPrettyString()") {
        expect("SideNav[label='label']") { SideNav("label").toPrettyString() }
        expect("SideNavItem[label='foo', @path='login']") { SideNavItem("foo", LoginView::class.java).toPrettyString() }
        expect("SideNavItem[label='bar', @path='admin']") { SideNavItem("bar", "admin").toPrettyString() }
    }
}
