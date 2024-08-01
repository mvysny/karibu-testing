package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.onClick
import com.github.mvysny.karibudsl.v10.onLeftClick
import com.github.mvysny.karibudsl.v10.span
import com.github.mvysny.kaributools.get
import com.github.mvysny.kaributools.navigateTo
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.AccessDeniedException
import com.vaadin.flow.router.BeforeLeaveEvent
import com.vaadin.flow.router.BeforeLeaveObserver
import com.vaadin.flow.router.NotFoundException
import com.vaadin.flow.router.Route
import com.vaadin.flow.router.RouteNotFoundError
import com.vaadin.flow.server.VaadinRequest
import com.vaadin.flow.server.auth.NavigationAccessControl
import test.app.MyRouteNotFoundError
import java.io.Serializable
import java.security.Principal
import java.util.function.Predicate
import kotlin.test.expect

@DynaTestDsl
internal fun DynaNodeGroup.navigatorTest() {
    lateinit var routes: Routes
    beforeGroup { routes = Routes().autoDiscoverViews("com.github") }
    beforeEach { MockVaadin.setup(routes) }
    afterEach { MockVaadin.tearDown() }

    group("currentView") {
        test("simple view") {
            navigateTo<TestingView>()
            expect(TestingView::class.java) { currentView }
        }

        test("navigation to parametrized test") {
            navigateTo(ParametrizedView::class, 5)
            expect(ParametrizedView::class.java) { currentView }
        }

        test("navigation to view nested in a router layout") {
            navigateTo<ChildView>()
            expect(ChildView::class.java) { currentView }
        }
        test("query parameters using navigateTo()") {
            navigateTo("params/5?foo=bar")
            expect(ParametrizedView::class.java) { currentView }
        }
    }
    group("expectView") {
        test("simple view") {
            navigateTo<TestingView>()
            expectView<TestingView>()
            expectThrows(AssertionError::class) {
                expectView<ParametrizedView>()
            }
        }

        test("navigation to parametrized test") {
            navigateTo(ParametrizedView::class, 5)
            expectThrows(AssertionError::class) {
                expectView<TestingView>()
            }
        }

        test("navigation to view nested in a router layout") {
            navigateTo<ChildView>()
            expectView<ChildView>()
        }

        test("query parameters using navigateTo()") {
            navigateTo("params/5?foo=bar")
            expectView<ParametrizedView>()
        }
    }

    group("navigation") {
        test("navigation") {
            navigateTo<TestingView>()
            expect("testing") { currentPath }
            expectView<TestingView>()
        }

        test("navigation using navigateTo()") {
            navigateTo("testing")
            expect("testing") { currentPath }
            expectView<TestingView>()
        }

        test("navigation to parametrized test") {
            navigateTo(ParametrizedView::class, 5)
            expect(5) { _get<ParametrizedView>().parameter }
        }

        test("navigation to parametrized test using navigateTo()") {
            navigateTo("params/5")
            expect("params/5") { currentPath }
            expect(ParametrizedView::class.java) { currentView }
            expectView<ParametrizedView>()
            expect(5) { _get<ParametrizedView>().parameter }
        }

        test("query parameters using navigateTo()") {
            navigateTo("params/5?foo=bar")
            expect(5) { _get<ParametrizedView>().parameter }
            expect("bar") { _get<ParametrizedView>().qp["foo"] }
        }
    }

    group("currentPath") {
        test("simple view") {
            navigateTo<TestingView>()
            expect("testing") { currentPath }
        }

        test("navigation to parametrized test") {
            navigateTo(ParametrizedView::class, 5)
            expect("params/5") { currentPath }
        }

        test("navigation to view nested in a router layout") {
            navigateTo<ChildView>()
            expect("parent/child") { currentPath }
        }

        test("query parameters using navigateTo()") {
            navigateTo("params/5?foo=bar")
            expect("params/5?foo=bar") { currentPath }
        }
    }

    // tests for https://github.com/mvysny/karibu-testing/issues/34
    group("delayed navigation") {
        test("view") {
            var a: BeforeLeaveEvent.ContinueNavigationAction? = null
            UI.getCurrent().addBeforeLeaveListener { e ->
                expect(null) { a }
                a = e.postpone()
            }
            navigateTo<TestingView>()
            _expectNone<TestingView>()
            a!!.proceed()
            _expectOne<TestingView>()
        }

        test("notification") {
            var a: BeforeLeaveEvent.ContinueNavigationAction? = null
            UI.getCurrent().addBeforeLeaveListener { e ->
                expect(null) { a }
                a = e.postpone()
                Notification.show("postponing")
            }
            navigateTo<TestingView>()
            expectNotifications("postponing")
            a!!.proceed()
            expectNoNotifications()
        }

        test("dialog - stay on page") {
            navigateTo<NavigationPostponeView>()
            _expectNone<Dialog>()

            navigateTo<TestingView>()
            //  the navigation should be postponed
            _expectOne<NavigationPostponeView>()
            _expectOne<Dialog>()
            _expectNone<TestingView>()

            // cancel the navigation
            _get<Button> { text = "No" } ._click()
            // the navigation should have been canceled
            _expectOne<NavigationPostponeView>()
            _expectNone<Dialog>()
            _expectNone<TestingView>()
        }

        test("dialog - leave") {
            navigateTo<NavigationPostponeView>()
            _expectNone<Dialog>()

            navigateTo<TestingView>()
            //  the navigation should be postponed
            _expectOne<NavigationPostponeView>()
            _expectOne<Dialog>()
            _expectNone<TestingView>()

            // cancel the navigation
            _get<Button> { text = "Yes" } ._click()
            // the navigation should have proceeded
            _expectNone<NavigationPostponeView>()
            _expectNone<Dialog>()
            _expectOne<TestingView>()
        }
    }

    group("History") {
        test("replaceState() doesn't affect currentPath") {
            // also see https://github.com/mvysny/karibu-testing/issues/138
            navigateTo<TestingView>()
            expect("testing") { currentPath }
            currentUI.page.history.replaceState(null, "params/5")
            expect("testing") { currentPath }
            expect(TestingView::class.java) { currentView }
        }
        test("pushState() doesn't affect currentPath") {
            // also see https://github.com/mvysny/karibu-testing/issues/138
            navigateTo<TestingView>()
            expect("testing") { currentPath }
            currentUI.page.history.pushState(null, "params/5")
            expect("testing") { currentPath }
            expect(TestingView::class.java) { currentView }
        }
    }

    group("security") {
        navigatorSecurityTests("com.github") // includes also Karibu-Testing MockRouteNotFoundError and MockRouteAccessDeniedError on classpath
        navigatorSecurityTests(null) // includes everything on classpath, including Flow's default RouteNotFoundError and RouteAccessDeniedError and also MyRouteNotFoundError
        navigatorSecurityTests("test.app") // includes only MyRouteNotFoundError, but not Mock*Errors nor Flow default routes.
        navigatorSecurityTests("nonexisting.pkg") // includes nothing.
    }
}

@Route("navigation-postpone")
class NavigationPostponeView : VerticalLayout(), BeforeLeaveObserver {
    override fun beforeLeave(event: BeforeLeaveEvent) {
        val action = event.postpone()
        Dialog().apply {
            span("Are you sure you want to leave such a beautiful view?")
            button("Yes") {
                onClick { action.proceed(); close() }
            }
            button("No") {
                onClick { close() }
            }
        }.open()
    }
}

class SimpleNavigationAccessControl(val user: String? = null) : NavigationAccessControl() {
    override fun getPrincipal(request: VaadinRequest?): Principal? = if (user == null) null else SimplePrincipal(user)
    override fun getRolesChecker(request: VaadinRequest?): Predicate<String> = Predicate { false }
}

data class SimplePrincipal(private val name: String): Principal, Serializable {
    override fun getName(): String = name
}

@DynaTestDsl
internal fun DynaNodeGroup.navigatorSecurityTests(classpathScanPackage: String?) {
    group("classpath scan of $classpathScanPackage") {
        lateinit var routes: Routes
        beforeEach {
            MockVaadin.tearDown()
            routes = Routes().autoDiscoverViews(classpathScanPackage)
            if (routes.errorRoutes.contains(MyRouteNotFoundError::class.java)) {
                routes.errorRoutes.remove(MyRouteNotFoundError::class.java)
                routes.errorRoutes.add(MockRouteNotFoundError::class.java)
            }
            routes.routes.addAll(setOf(TestingView::class.java, WelcomeView::class.java))
            MockVaadin.setup(routes)
        }
        afterEach { MockVaadin.tearDown() }

        group("no user logged in") {
            test("both mock routes are present") {
                expect(true, routes.toString()) { routes.errorRoutes.contains(MockRouteNotFoundError::class.java) }
                expect(true, routes.toString()) { routes.errorRoutes.contains(MockRouteAccessDeniedError::class.java) }
            }
            test("when access is rejected, redirect goes to WelcomeView") {
                UI.getCurrent().addBeforeEnterListener(SimpleNavigationAccessControl().apply {
                    setLoginView(WelcomeView::class.java)
                })
                navigateTo<TestingView>()
                expectView<WelcomeView>()
            }
            test("when access is rejected and no login view is set, redirects to MockRouteNotFoundError") {
                UI.getCurrent().addBeforeEnterListener(SimpleNavigationAccessControl())
                expectThrows<NotFoundException>("No route found for 'testing': Consider adding one of the following annotations to make the view accessible: @AnonymousAllowed, @PermitAll, @RolesAllowed.") {
                    navigateTo<TestingView>()
                }
            }
        }
        group("user logged in") {
            test("when access is rejected, default handler redirects to MockRouteNotFoundError") {
                MockVaadin.tearDown()
                routes.errorRoutes.remove(MockRouteAccessDeniedError::class.java)
                MockVaadin.setup(routes)

                UI.getCurrent().addBeforeEnterListener(SimpleNavigationAccessControl("admin"))
                expectThrows<NotFoundException>("No route found for 'testing': Consider adding one of the following annotations to make the view accessible: @AnonymousAllowed, @PermitAll, @RolesAllowed.") {
                    navigateTo<TestingView>()
                }
            }
            test("when access is rejected, Karibu's MockRouteAccessDeniedError throws AccessDeniedException") {
                UI.getCurrent().addBeforeEnterListener(SimpleNavigationAccessControl("admin"))
                expectThrows<AccessDeniedException>("Consider adding one of the following annotations to make the view accessible: @AnonymousAllowed, @PermitAll, @RolesAllowed") {
                    navigateTo<TestingView>()
                }
            }
        }
    }
}
