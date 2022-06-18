package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.onLeftClick
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
import kotlin.test.expect

@DynaTestDsl
internal fun DynaNodeGroup.navigatorTest() {
    lateinit var routes: Routes
    beforeGroup { routes = Routes().autoDiscoverViews("com.github") }
    beforeEach { MockVaadin.setup(routes) }
    afterEach { MockVaadin.tearDown() }

    test("navigation") {
        navigateTo<TestingView>()
        expect(TestingView::class.java) { currentView }
        expectView<TestingView>()
    }

    test("navigation using navigateTo()") {
        navigateTo("testing")
        expect(TestingView::class.java) { currentView }
        expectView<TestingView>()
    }

    test("navigation to parametrized test") {
        navigateTo(ParametrizedView::class, 5)
        expect(ParametrizedView::class.java) { currentView }
        expectView<ParametrizedView>()
        expect(5) { _get<ParametrizedView>().parameter }
        expectThrows(AssertionError::class) {
            expectView<TestingView>()
        }
    }

    test("navigation to parametrized test using navigateTo()") {
        navigateTo("params/5")
        expect(ParametrizedView::class.java) { currentView }
        expectView<ParametrizedView>()
        expect(5) { _get<ParametrizedView>().parameter }
    }

    test("query parameters using navigateTo()") {
        navigateTo("params/5?foo=bar")
        expect(5) { _get<ParametrizedView>().parameter }
        expect("bar") { _get<ParametrizedView>().qp["foo"] }
        expect(ParametrizedView::class.java) { currentView }
        expectView<ParametrizedView>()
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
            _get<Button> { caption = "No" } ._click()
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
            _get<Button> { caption = "Yes" } ._click()
            // the navigation should have proceeded
            _expectNone<NavigationPostponeView>()
            _expectNone<Dialog>()
            _expectOne<TestingView>()
        }
    }
}

@Route("navigation-postpone")
class NavigationPostponeView : VerticalLayout(), BeforeLeaveObserver {
    override fun beforeLeave(event: BeforeLeaveEvent) {
        val action = event.postpone()
        Dialog().apply {
            span("Are you sure you want to leave such a beautiful view?")
            button("Yes") {
                onLeftClick { action.proceed(); close() }
            }
            button("No") {
                onLeftClick { close() }
            }
        }.open()
    }
}
