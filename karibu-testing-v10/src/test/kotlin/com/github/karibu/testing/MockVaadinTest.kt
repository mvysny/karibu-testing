package com.github.karibu.testing

import com.github.mvysny.dynatest.DynaTest
import com.github.vok.karibudsl.flow.button
import com.github.vok.karibudsl.flow.text
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.VaadinService
import com.vaadin.flow.server.VaadinSession
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.expect

class MockVaadinTest : DynaTest({

    beforeEach { MockVaadin.setup(Routes().autoDiscoverViews("com.github")) }

    test("smoke test that everything is mocked") {
        expect(true) { UI.getCurrent() != null }
        expect(true) { VaadinSession.getCurrent() != null }
        expect(true) { VaadinService.getCurrent() != null }
        expect(true) { VaadinSession.getCurrent().configuration != null }
        expect(true) { VaadinSession.getCurrent().service != null }
        expect(true) { VaadinSession.getCurrent().browser != null }
    }

    test("verifyAttachCalled") {
        val attachCalled = AtomicInteger()
        val vl = object : VerticalLayout() {
            override fun onAttach(attachEvent: AttachEvent?) {
                super.onAttach(attachEvent)
                attachCalled.incrementAndGet()
            }
        }
        vl.addAttachListener { attachCalled.incrementAndGet() }
        UI.getCurrent().add(vl)
        expect(2) { attachCalled.get() }
        expect(true) { vl.isAttached }
    }

    test("Navigation") {
        // no need: when UI is initialized in MockVaadin.setup(), automatic navigation to "" is performed.
//        UI.getCurrent().navigate("")
        _get<Text> { text = "Welcome!" }
        UI.getCurrent().navigate("helloworld")
        _get<Button> { caption = "Hello, World!" }
    }

    test("open dialog") {
        // there should be no dialogs in the UI
        _expectNone<Dialog>()
        _expectNone<Div> { text = "Dialog Text" }
        val dialog = Dialog(Div().apply { text("Dialog Text") })
        dialog.open()
        _get<Dialog>()  // should be in the UI, along with its contents
        _get<Div> { text = "Dialog Text" }
        dialog.close()
        // there should be no dialogs in the UI
        _expectNone<Div> { text = "Dialog Text" }
        _expectNone<Dialog>()
    }

    test("the dialogs must be cleared up from the component tree on close") {
        val dialog = Dialog(Div().apply { text("Dialog Text") })
        dialog.open()
        dialog.close()
        cleanupDialogs()
        expect("""
└── MockedUI[]
    └── WelcomeView[]
        └── Text[text='Welcome!']
""".trim()) { UI.getCurrent().toPrettyTree().trim() }
    }

    test("Page reload should re-create the UI") {
        val ui = UI.getCurrent()
        var detachCalled = false
        ui.addDetachListener { detachCalled = true }
        UI.getCurrent().page.reload()
        // a new UI must be created; but the Session must stay the same.
        expect(true) { UI.getCurrent() != null }
        expect(false) { UI.getCurrent() === ui }
        // the old UI must be detached properly
        expect(true) { detachCalled }
    }

    test("Page reload should preserve session") {
        val session = VaadinSession.getCurrent()
        session.setAttribute("foo", "bar")
        UI.getCurrent().page.reload()
        expect(true) { VaadinSession.getCurrent() === session }
        expect("bar") { VaadinSession.getCurrent().getAttribute("foo") }
    }

    test("Page reload should automatically navigate to the current URL") {
        _get<WelcomeView>()
        UI.getCurrent().page.reload()
        _get<WelcomeView>()
        UI.getCurrent().navigate("helloworld")
        _expectNone<WelcomeView>()
        _get<HelloWorldView>()
        UI.getCurrent().page.reload()
        _expectNone<WelcomeView>()
        _get<HelloWorldView>()
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
})

@Route("helloworld")
class HelloWorldView : VerticalLayout() {
    init {
        button("Hello, World!")
    }
}

@Route("")
class WelcomeView : VerticalLayout() {
    init {
        text("Welcome!")
    }
}
