package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.navigateToView
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.router.BeforeLeaveEvent
import kotlin.test.expect

internal fun DynaNodeGroup.navigatorTest() {
    lateinit var routes: Routes
    beforeGroup { routes = Routes().autoDiscoverViews("com.github") }
    beforeEach { MockVaadin.setup(routes) }
    afterEach { MockVaadin.tearDown() }

    test("navigation") {
        navigateToView<TestingView>()
        expect(TestingView::class.java) { currentView }
        expectView<TestingView>()
    }

    test("navigation to parametrized test") {
        navigateToView(ParametrizedView::class, 5)
        expect(ParametrizedView::class.java) { currentView }
        expectView<ParametrizedView>()
        expectThrows(AssertionError::class) {
            expectView<TestingView>()
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
            navigateToView<TestingView>()
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
            navigateToView<TestingView>()
            expectNotifications("postponing")
            a!!.proceed()
            expectNoNotifications()
        }
    }
}
