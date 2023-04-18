package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.textField
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.ClickEvent
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.dom.DomEvent
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.ErrorParameter
import com.vaadin.flow.router.HasErrorParameter
import com.vaadin.flow.router.Route
import elemental.json.Json
import kotlin.test.expect
import kotlin.test.fail

@DynaTestDsl
internal fun DynaNodeGroup.basicUtilsTestbatch() {

    group("_expectEditableByUser()") {
        test("disabled textfield fails") {
            expectThrows(java.lang.IllegalStateException::class, "The TextField[DISABLED, value=''] is not enabled") {
                TextField().apply { isEnabled = false }._expectEditableByUser()
            }
        }
        test("invisible textfield fails") {
            expectThrows(java.lang.IllegalStateException::class, "The TextField[INVIS, value=''] is not effectively visible") {
                TextField().apply { isVisible = false }._expectEditableByUser()
            }
        }
        test("textfield in invisible layout fails") {
            expectThrows(java.lang.IllegalStateException::class, "The TextField[value=''] is not effectively visible") {
                VerticalLayout().apply {
                    isVisible = false
                    textField().also { it._expectEditableByUser() }
                }
            }
        }
        test("textfield succeeds") {
            TextField()._expectEditableByUser()
        }
    }

    group("expectNotEditableByUser") {
        test("disabled textfield fails") {
            TextField().apply { isEnabled = false }._expectNotEditableByUser()
        }
        test("invisible textfield fails") {
            TextField().apply { isVisible = false }._expectNotEditableByUser()
        }
        test("textfield in invisible layout fails") {
            VerticalLayout().apply {
                isVisible = false
                textField().also { it._expectNotEditableByUser() }
            }
        }
        test("textfield succeeds") {
            expectThrows(AssertionError::class, "The TextField[value=''] is editable") {
                TextField()._expectNotEditableByUser()
            }
        }
    }

    group("fireDomEvent()") {
        test("smoke") {
            Div()._fireDomEvent("click")
        }
        test("listeners are called") {
            val div = Div()
            lateinit var event: DomEvent
            div.element.addEventListener("click") { e -> event = e }
            div._fireDomEvent("click")
            expect("click") { event.type }
        }
        test("higher-level listeners are called") {
            val div = Div()
            lateinit var event: ClickEvent<Div>
            div.addClickListener { e -> event = e }
            div._fireDomEvent("click", Json.createObject().apply { put("event.screenX", 20.0) })
            expect(20) { event.screenX }
        }
        test("fails for non-editable component") {
            val div = Div().apply { isVisible = false }
            div.element.addEventListener("click") { fail("should not be called") }
            expectThrows<IllegalStateException>("The Div[INVIS] is not effectively visible") {
                div._fireDomEvent("click")
            }
        }
    }
    test("_fireDomClickEvent") {
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

    test("_focus") {
        val f = TextField()
        var called = false
        f.addFocusListener { called = true }
        f._focus()
        expect(true) { called }
    }

    test("_blur") {
        val f = TextField()
        var called = false
        f.addBlurListener { called = true }
        f._blur()
        expect(true) { called }
    }

    test("_expectEnabled") {
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

    test("_expectDisabled") {
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

    test("_expectNotReadOnly") {
        TextField()._expectNotReadOnly()
        expectThrows<AssertionError>("TextField[RO, value=''] is read-only") {
            TextField().apply { isReadOnly = true } ._expectNotReadOnly()
        }
    }

    test("_expectReadOnly") {
        TextField().apply { isReadOnly = true } ._expectReadOnly()
        expectThrows<AssertionError>("TextField[value=''] is not read-only") {
            TextField()._expectReadOnly()
        }
    }
}

@Route("testing")
class TestingView : VerticalLayout()

class ErrorView : VerticalLayout(), HasErrorParameter<Exception> {
    override fun setErrorParameter(event: BeforeEnterEvent, parameter: ErrorParameter<Exception>): Int =
            throw RuntimeException(parameter.caughtException)
}
