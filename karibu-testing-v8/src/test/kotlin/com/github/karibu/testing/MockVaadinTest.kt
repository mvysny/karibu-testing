package com.github.karibu.testing

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.github.vok.karibudsl.autoDiscoverViews
import com.github.vok.karibudsl.autoViewProvider
import com.vaadin.navigator.Navigator
import com.vaadin.navigator.PushStateNavigation
import com.vaadin.navigator.View
import com.vaadin.server.Page
import com.vaadin.server.VaadinRequest
import com.vaadin.server.VaadinService
import com.vaadin.server.VaadinSession
import com.vaadin.ui.UI
import com.vaadin.ui.VerticalLayout
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.expect

class MockVaadinTest : DynaTest({
    beforeEach { MockVaadin.setup() }

    test("Vaadin.getCurrent() returns non-null values") {
        expect(true) { VaadinSession.getCurrent() != null }
        expect(true) { VaadinService.getCurrent() != null }
        expect(true) { UI.getCurrent() != null }
    }

    test("verifyAttachCalled") {
        val attachCalled = AtomicInteger()
        val vl = object : VerticalLayout() {
            override fun attach() {
                super.attach()
                attachCalled.incrementAndGet()
            }
        }
        vl.addAttachListener { attachCalled.incrementAndGet() }
        UI.getCurrent().content = vl
        expect(2) { attachCalled.get() }
        expect(true) { vl.isAttached }
    }

    test("wrapped session works") {
        VaadinSession.getCurrent().session.setAttribute("foo", "bar")
        expect("bar") { VaadinSession.getCurrent().session.getAttribute("foo") }
    }

    test("Browser should be mocked as well") {
        expect("127.0.0.1") { Page.getCurrent().webBrowser.address }
    }

    test("UI with push state and navigator won't fail") {
        MockVaadin.setup(uiFactory = { MyUIWithNavigator() })
    }

    test("Page reload should re-create the UI") {
        val ui = UI.getCurrent()
        var detachCalled = false
        ui.addDetachListener { detachCalled = true }
        Page.getCurrent().reload()
        // a new UI must be created; but the Session must stay the same.
        expect(true) { UI.getCurrent() != null }
        expect(false) { UI.getCurrent() === ui }
        // the old UI must be detached properly
        expect(true) { detachCalled }
    }

    test("Page reload should preserve session") {
        val session = VaadinSession.getCurrent()
        session.setAttribute("foo", "bar")
        Page.getCurrent().reload()
        expect(true) { VaadinSession.getCurrent() === session }
        expect("bar") { VaadinSession.getCurrent().getAttribute("foo") }
    }

    test("Page reload should automatically navigate to the current URL") {
        _get<DummyView>()
        UI.getCurrent().page.reload()
        _get<DummyView>()
        UI.getCurrent().navigator.navigateTo("2")
        _expectNone<DummyView>()
        _get<DummyView2>()
        UI.getCurrent().page.reload()
        _expectNone<DummyView>()
        _get<DummyView2>()
    }

    test("VaadinSession.close() must re-create the entire session and the UI") {
        val ui = UI.getCurrent()
        var detachCalled = false
        ui.addDetachListener { detachCalled = true }
        val session = VaadinSession.getCurrent()
        session.setAttribute("foo", "bar")
        session.close()

        // a new UI+Session must be created
        expect(true) { UI.getCurrent() != null }
        expect(true) { VaadinSession.getCurrent() != null }
        expect(false) { UI.getCurrent() === ui }
        expect(false) { VaadinSession.getCurrent() === session }
        // the old UI must be detached properly
        expect(true) { detachCalled }
        // the new session must not inherit attributes from the old one
        expect(null) { VaadinSession.getCurrent().getAttribute("foo") }
    }

    test("if autoViewProvider is used with a Navigator, display a helpful message instead of standard Vaadin one") {
        autoDiscoverViews("non.existing.package")   // should remove all views

        expectThrows(RuntimeException::class, "UI failed to initialize. If you're using autoViewProvider, make sure that views are auto-discovered via autoDiscoverViews()") {
            MockVaadin.setup({ MyUIWithAutoViewProvider() })
        }
    }
})

@PushStateNavigation
class MyUIWithNavigator : UI() {
    override fun init(request: VaadinRequest) {
        navigator = Navigator(this, this)
        navigator.addView("", DummyView())
        navigator.addView("2", DummyView2())
    }
}

class DummyView : VerticalLayout(), View
class DummyView2 : VerticalLayout(), View

@PushStateNavigation
class MyUIWithAutoViewProvider : UI() {
    override fun init(request: VaadinRequest) {
        navigator = Navigator(this, this)
        navigator.addProvider(autoViewProvider)
    }
}