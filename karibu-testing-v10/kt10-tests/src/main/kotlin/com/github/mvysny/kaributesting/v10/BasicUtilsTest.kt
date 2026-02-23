package com.github.mvysny.kaributesting.v10

import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.textField
import com.vaadin.flow.component.ClickEvent
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.tabs.TabSheet
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.dom.DomEvent
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.ErrorParameter
import com.vaadin.flow.router.HasErrorParameter
import com.vaadin.flow.router.InternalServerError
import com.vaadin.flow.router.Location
import com.vaadin.flow.router.NavigationTrigger
import com.vaadin.flow.router.Route
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper
import kotlin.test.expect
import kotlin.test.fail

abstract class AbstractBasicUtilsTests {
    @Nested inner class _expectEditableByUser {
        @Test fun `disabled textfield fails`() {
            expectThrows(java.lang.IllegalStateException::class, "The TextField[DISABLED, value=''] is not enabled") {
                TextField().apply { isEnabled = false }._expectEditableByUser()
            }
        }
        @Test fun `invisible textfield fails`() {
            expectThrows(java.lang.IllegalStateException::class, "The TextField[INVIS, value=''] is not effectively visible") {
                TextField().apply { isVisible = false }._expectEditableByUser()
            }
        }
        @Test fun `textfield in invisible layout fails`() {
            expectThrows(java.lang.IllegalStateException::class, "The TextField[value=''] is not effectively visible") {
                VerticalLayout().apply {
                    isVisible = false
                    textField().also { it._expectEditableByUser() }
                }
            }
        }
        @Test fun `textfield succeeds`() {
            TextField()._expectEditableByUser()
        }
    }

    @Nested inner class expectNotEditableByUser {
        @Test fun `disabled textfield fails`() {
            TextField().apply { isEnabled = false }._expectNotEditableByUser()
        }
        @Test fun `invisible textfield fails`() {
            TextField().apply { isVisible = false }._expectNotEditableByUser()
        }
        @Test fun `textfield in invisible layout fails`() {
            VerticalLayout().apply {
                isVisible = false
                textField().also { it._expectNotEditableByUser() }
            }
        }
        @Test fun `textfield succeeds`() {
            expectThrows(AssertionError::class, "The TextField[value=''] is editable") {
                TextField()._expectNotEditableByUser()
            }
        }
    }

    @Nested inner class fireDomEvent {
        @Test fun smoke() {
            Div()._fireDomEvent("click")
        }
        @Test fun `listeners are called`() {
            val div = Div()
            lateinit var event: DomEvent
            div.element.addEventListener("click") { e -> event = e }
            div._fireDomEvent("click")
            expect("click") { event.type }
        }
        @Test fun `higher-level listeners are called`() {
            val div = Div()
            lateinit var event: ClickEvent<Div>
            div.addClickListener { e -> event = e }
            div._fireDomEvent("click", ObjectMapper().createObjectNode().apply { put("event.screenX", 20.0) })
            expect(20) { event.screenX }
        }
        @Test fun `fails for non-editable component`() {
            val div = Div().apply { isVisible = false }
            div.element.addEventListener("click") { fail("should not be called") }
            expectThrows<IllegalStateException>("The Div[INVIS] is not effectively visible") {
                div._fireDomEvent("click")
            }
        }
    }
    @Test fun fireDomClickEvent() {
        val div = Div()
        lateinit var event: ClickEvent<Div>
        div.addClickListener { e -> event = e }
        div._fireDomClickEvent(2, 3, true, true, true, true)
        expect(2) { event.button }
        expect(3) { event.clickCount }
        expect(true) { event.isShiftKey }
        expect(true) { event.isCtrlKey }
        expect(true) { event.isAltKey }
        expect(true) { event.isMetaKey }
    }

    @Test fun testFocus() {
        val f = TextField()
        var called = false
        f.addFocusListener {
            expect(true) { it.isFromClient }
            called = true
        }
        f._focus()
        expect(true) { called }
    }

    @Test fun _blur() {
        val f = TextField()
        var called = false
        f.addBlurListener {
            expect(true) { it.isFromClient }
            called = true
        }
        f._blur()
        expect(true) { called }
    }

    @Test fun _expectEnabled() {
        Button()._expectEnabled()
        expectThrows<AssertionError>("Button[DISABLED] is not enabled") {
            Button().apply { isEnabled = false } ._expectEnabled()
        }
        expectThrows<AssertionError>("Button[DISABLED] is not enabled") {
            VerticalLayout().apply {
                isEnabled = false
                button { _expectEnabled() }
            }
        }
        expectThrows<AssertionError>("Button[DISABLED] is not enabled") {
            VerticalLayout().apply {
                isEnabled = false
                button { isEnabled = false; _expectEnabled() }
            }
        }
    }

    @Test fun _expectDisabled() {
        Button().apply { isEnabled = false } ._expectDisabled()
        expectThrows<AssertionError>("Button[] is not disabled") {
            Button()._expectDisabled()
        }
        VerticalLayout().apply {
            isEnabled = false
            button { _expectDisabled() }
        }
        VerticalLayout().apply {
            isEnabled = false
            button { isEnabled = false; _expectDisabled() }
        }
    }

    @Test fun _expectNotReadOnly() {
        TextField()._expectNotReadOnly()
        expectThrows<AssertionError>("TextField[RO, value=''] is read-only") {
            TextField().apply { isReadOnly = true } ._expectNotReadOnly()
        }
    }

    @Test fun _expectReadOnly() {
        TextField().apply { isReadOnly = true } ._expectReadOnly()
        expectThrows<AssertionError>("TextField[value=''] is not read-only") {
            TextField()._expectReadOnly()
        }
    }

    @Nested inner class InternalServerErrorTests {
        @BeforeEach fun fakeVaadin() {
            MockVaadin.setup()
        }
        @AfterEach fun tearDownVaadin() {
            MockVaadin.tearDown()
        }
        @Test
        fun uninitialized() {
            val e = InternalServerError()
            expect("") { e._errorMessage }
        }
        @Test
        fun simpleException() {
            val e = InternalServerError()
            e.setErrorParameter(fakeBeforeEnterEvent(), ErrorParameter(
                Exception::class.java, RuntimeException("Simulated")))
            expect(true, e._errorMessage) { e._errorMessage.startsWith("There was an exception while trying to navigate to ''\njava.lang.RuntimeException: Simulated") }
        }
        @Test
        fun exceptionWithCause() {
            val e = InternalServerError()
            e.setErrorParameter(fakeBeforeEnterEvent(), ErrorParameter(
                Exception::class.java, RuntimeException("Simulated", RuntimeException("Cause"))))
            expect(true, e._errorMessage) { e._errorMessage.startsWith("There was an exception while trying to navigate to '' with the root cause 'java.lang.RuntimeException: Cause'\njava.lang.RuntimeException: Simulated") }
            expect(true, e._errorMessage) { e._errorMessage.contains("RuntimeException: Cause") }
        }
    }

    @Nested inner class visibilityTests {
        @BeforeEach fun fakeVaadin() {
            MockVaadin.setup()
        }
        @AfterEach fun tearDownVaadin() {
            MockVaadin.tearDown()
        }
        @Test fun unattachedComponentVisible() {
            expect(true) { Button("foo")._isVisible }
            expect(true) { Text("foo")._isVisible }
        }
        @Test fun textVisibility() {
            expect(true) { Text("foo")._isVisible }
            // workaround for https://github.com/vaadin/flow/issues/3201
            expect(false) { Text("")._isVisible }
        }
        @Test fun tabSheetContentsVisibility() {
            val ts = TabSheet()
            currentUI.add(ts)
            val c1 = Button("Tab1")
            val c2 = Button("Tab2")
            ts.add("tab1", c1)
            ts.add("tab2", c2)
            MockVaadin.clientRoundtrip()
            expect(true) { c1._isVisible }
            expect(ts) { c1._parent }
            expect(null) { c2._parent } // c2 not yet attached to TabSheet - lazy initialization
            expect(true) { c2._isVisible } // c2 not yet attached to TabSheet - lazy initialization
            expect(true) { ts._tabs._isVisible }
            ts.selectedIndex = 1
            MockVaadin.clientRoundtrip()
            expect(ts) { c1._parent }
            expect(ts) { c2._parent }
            expect(false) { c1._isVisible }
            expect(true) { c2._isVisible }
            expect(true) { ts._tabs._isVisible }
        }
        // tests https://github.com/mvysny/karibu-testing/issues/198
        @Test fun tabSheetPrefixSuffixVisibility() {
            val ts = TabSheet()
            ts.prefixComponent = Button("Prefix")
            ts.suffixComponent = Button("Suffix")
            expect(true) { ts.prefixComponent._isVisible }
            expect(true) { ts.suffixComponent._isVisible }
            expect(ts) { ts.prefixComponent._parent }
            expect(ts) { ts.suffixComponent._parent }
            ts.add("tab1", Span("Hi"))
            expect(true) { ts.prefixComponent._isVisible }
            expect(true) { ts.suffixComponent._isVisible }
            expect(ts) { ts.prefixComponent._parent }
            expect(ts) { ts.suffixComponent._parent }
        }
    }
}

@Route("testing", autoLayout = false)
class TestingView : VerticalLayout()

class ErrorView : VerticalLayout(), HasErrorParameter<Exception> {
    override fun setErrorParameter(event: BeforeEnterEvent, parameter: ErrorParameter<Exception>): Int =
            throw RuntimeException(parameter.caughtException)
}

fun fakeBeforeEnterEvent(): BeforeEnterEvent {
    val router = currentUI.internals.router
    return BeforeEnterEvent(
        router,
        NavigationTrigger.PROGRAMMATIC, Location(""), TestingView::class.java,
        currentUI, listOf())
}
