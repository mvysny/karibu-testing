package com.github.karibu.testing

import com.github.mvysny.dynatest.DynaTest
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
        val session = VaadinSession.getCurrent()
        Page.getCurrent().reload()
        // a new UI must be created; but the Session must stay the same.
        expect(false) { UI.getCurrent() === ui }
        expect(true) { VaadinSession.getCurrent() === session }
    }
})

@PushStateNavigation
class MyUIWithNavigator : UI() {
    override fun init(request: VaadinRequest) {
        navigator = Navigator(this, this)
        navigator.addView("", DummyView())
    }
}

class DummyView : VerticalLayout(), View
