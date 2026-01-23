package com.github.mvysny.kaributesting.v10

import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.onClick
import com.github.mvysny.karibudsl.v10.span
import com.github.mvysny.kaributools.get
import com.github.mvysny.kaributools.navigateTo
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.BeforeLeaveEvent
import com.vaadin.flow.router.BeforeLeaveObserver
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.VaadinRequest
import com.vaadin.flow.server.auth.NavigationAccessControl
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import test.app.MyRouteNotFoundError
import java.io.Serializable
import java.security.Principal
import java.util.function.Predicate
import kotlin.test.expect

abstract class AbstractNavigatorTests {
    companion object {
        lateinit var routes: Routes
        @BeforeAll @JvmStatic fun scanForRoutes() { routes = Routes().autoDiscoverViews("com.github") }
    }
    @BeforeEach fun fakeVaadin() { MockVaadin.setup(routes) }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Nested inner class currentView {
        @Test fun `simple view`() {
            navigateTo<TestingView>()
            expect(TestingView::class.java) { currentView }
        }

        @Test fun `navigation to parametrized test`() {
            navigateTo(ParametrizedView::class, 5)
            expect(ParametrizedView::class.java) { currentView }
        }

        @Test fun `navigation to view nested in a router layout`() {
            navigateTo<ChildView>()
            expect(ChildView::class.java) { currentView }
        }
        @Test fun `query parameters using navigateTo()`() {
            navigateTo("params/5?foo=bar")
            expect(ParametrizedView::class.java) { currentView }
        }
    }
    @Nested inner class expectView {
        @Test fun `simple view`() {
            navigateTo<TestingView>()
            expectView<TestingView>()
            expectThrows(AssertionError::class) {
                expectView<ParametrizedView>()
            }
        }

        @Test fun `navigation to parametrized test`() {
            navigateTo(ParametrizedView::class, 5)
            expectThrows(AssertionError::class) {
                expectView<TestingView>()
            }
        }

        @Test fun `navigation to view nested in a router layout`() {
            navigateTo<ChildView>()
            expectView<ChildView>()
        }

        @Test fun `query parameters using navigateTo()`() {
            navigateTo("params/5?foo=bar")
            expectView<ParametrizedView>()
        }
    }

    @Nested inner class navigation {
        @Test fun `simple navigation`() {
            navigateTo<TestingView>()
            expect("testing") { currentPath }
            expectView<TestingView>()
        }

        @Test fun `navigation using navigateTo()`() {
            navigateTo("testing")
            expect("testing") { currentPath }
            expectView<TestingView>()
        }

        @Test fun `navigation to parametrized test`() {
            navigateTo(ParametrizedView::class, 5)
            expect(5) { _get<ParametrizedView>().parameter }
        }

        @Test fun `navigation to parametrized test using navigateTo()`() {
            navigateTo("params/5")
            expect("params/5") { currentPath }
            expect(ParametrizedView::class.java) { currentView }
            expectView<ParametrizedView>()
            expect(5) { _get<ParametrizedView>().parameter }
        }

        @Test fun `query parameters using navigateTo()`() {
            navigateTo("params/5?foo=bar")
            expect(5) { _get<ParametrizedView>().parameter }
            expect("bar") { _get<ParametrizedView>().qp["foo"] }
        }
    }

    @Nested inner class currentPath {
        @Test fun `simple view`() {
            navigateTo<TestingView>()
            expect("testing") { currentPath }
        }

        @Test fun `navigation to parametrized test`() {
            navigateTo(ParametrizedView::class, 5)
            expect("params/5") { currentPath }
        }

        @Test fun `navigation to view nested in a router layout`() {
            navigateTo<ChildView>()
            expect("parent/child") { currentPath }
        }

        @Test fun `query parameters using navigateTo()`() {
            navigateTo("params/5?foo=bar")
            expect("params/5?foo=bar") { currentPath }
        }
    }

    // tests for https://github.com/mvysny/karibu-testing/issues/34
    @Nested inner class `delayed navigation` {
        @Test fun view() {
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

        @Test fun notification() {
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

        @Test fun `dialog - stay on page`() {
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

        @Test fun `dialog - leave`() {
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

    @Nested inner class History {
        @Test fun `replaceState() doesn't affect currentPath`() {
            // also see https://github.com/mvysny/karibu-testing/issues/138
            navigateTo<TestingView>()
            expect("testing") { currentPath }
            currentUI.page.history.replaceState(null, "params/5")
            expect("testing") { currentPath }
            expect(TestingView::class.java) { currentView }
        }
        @Test fun `pushState() doesn't affect currentPath`() {
            // also see https://github.com/mvysny/karibu-testing/issues/138
            navigateTo<TestingView>()
            expect("testing") { currentPath }
            currentUI.page.history.pushState(null, "params/5")
            expect("testing") { currentPath }
            expect(TestingView::class.java) { currentView }
        }
    }

    @Nested inner class security {
        // includes also Karibu-Testing MockRouteNotFoundError and MockRouteAccessDeniedError on classpath
        @Nested inner class ComGithub : AbstractNavigatorSecurityTests("com.github")
        // includes everything on classpath, including Flow's default RouteNotFoundError and RouteAccessDeniedError and also MyRouteNotFoundError
        @Nested inner class All : AbstractNavigatorSecurityTests(null)
        // includes only MyRouteNotFoundError, but not Mock*Errors nor Flow default routes.
        @Nested inner class TestApp : AbstractNavigatorSecurityTests("test.app")
        // includes nothing.
        @Nested inner class NonexistingPkg : AbstractNavigatorSecurityTests("nonexisting.pkg")
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

abstract class AbstractNavigatorSecurityTests(val classpathScanPackage: String?) {
    lateinit var routes: Routes
    @BeforeEach fun fakeVaadin() {
        MockVaadin.tearDown()
        routes = Routes().autoDiscoverViews(classpathScanPackage)
        if (routes.errorRoutes.contains(MyRouteNotFoundError::class.java)) {
            routes.errorRoutes.remove(MyRouteNotFoundError::class.java)
            routes.errorRoutes.add(MockRouteNotFoundError::class.java)
        }
        routes.routes.addAll(setOf(TestingView::class.java, WelcomeView::class.java))
        MockVaadin.setup(routes)
    }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Nested inner class `no user logged in`() {
        @Test fun `both mock routes are present`() {
            expect(true, routes.toString()) { routes.errorRoutes.contains(MockRouteNotFoundError::class.java) }
            expect(true, routes.toString()) { routes.errorRoutes.contains(MockRouteAccessDeniedError::class.java) }
        }
        @Test fun `when access is rejected, redirect goes to WelcomeView`() {
            UI.getCurrent().addBeforeEnterListener(SimpleNavigationAccessControl().apply {
                setLoginView(WelcomeView::class.java)
            })
            navigateTo<TestingView>()
            expectView<WelcomeView>()
        }
        @Test fun `when access is rejected and no login view is set, redirects to MockRouteNotFoundError`() {
            UI.getCurrent().addBeforeEnterListener(SimpleNavigationAccessControl())
            expectThrows<NotFoundError>("No route found for 'testing': Consider adding one of the following annotations to make the view accessible: @AnonymousAllowed, @PermitAll, @RolesAllowed.") {
                navigateTo<TestingView>()
            }
        }
    }
    @Nested inner class `user logged in` {
        @Test fun `when access is rejected, default handler redirects to MockRouteNotFoundError`() {
            MockVaadin.tearDown()
            routes.errorRoutes.remove(MockRouteAccessDeniedError::class.java)
            MockVaadin.setup(routes)

            UI.getCurrent().addBeforeEnterListener(SimpleNavigationAccessControl("admin"))
            expectThrows<NotFoundError>("No route found for 'testing': Consider adding one of the following annotations to make the view accessible: @AnonymousAllowed, @PermitAll, @RolesAllowed.") {
                navigateTo<TestingView>()
            }
        }
        @Test fun `when access is rejected, Karibu's MockRouteAccessDeniedError throws MockAccessDeniedException`() {
            UI.getCurrent().addBeforeEnterListener(SimpleNavigationAccessControl("admin"))
            expectThrows<MockAccessDeniedException>("Consider adding one of the following annotations to make the view accessible: @AnonymousAllowed, @PermitAll, @RolesAllowed") {
                navigateTo<TestingView>()
            }
        }
    }
}
