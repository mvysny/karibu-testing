package com.github.karibu.testing

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.github.vok.karibudsl.flow.button
import com.github.vok.karibudsl.flow.checkBox
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route
import kotlin.test.expect

class BasicUtilsTest : DynaTest({

    val allViews = setOf(TestingView::class.java, HelloWorldView::class.java, WelcomeView::class.java)

    test("AutoViewDiscovery") {
        expect(allViews) { autoDiscoverViews("com.github") }
    }

    test("calling autoDiscoverViews() multiple times won't fail") {
        expect(allViews) { autoDiscoverViews("com.github") }
        expect(allViews) { autoDiscoverViews("com.github") }
    }

    group("button click") {
        fun expectClickCount(button: Button, clickCount: Int, block: Button.()->Unit) {
            var clicked = 0
            button.addClickListener { if (++clicked > clickCount) kotlin.test.fail("Clicked more than $clickCount times") }
            button.block()
            expect(clickCount) { clicked }
        }

        test("enabled button") {
            // click() does nothing without an actual Browser, bummer
            expectClickCount(Button(), 0) { click() }
            expectClickCount(Button(), 1) { _click() }
        }

        test("disabled button") {
            // click() does nothing without an actual Browser, bummer
            expectClickCount(Button().apply { isEnabled = false }, 0) { click() }
            // however _click() will properly fail
            expectThrows(IllegalStateException::class, "The Button[DISABLED] is not enabled") {
                expectClickCount(Button().apply { isEnabled = false }, 0) { _click() }
            }
        }

        test("button with parent disabled") {
            val layout = VerticalLayout().apply { isEnabled = false }
            expect(false) { layout.isEffectivelyEnabled() }
            // click() does nothing without an actual Browser, bummer
            expectClickCount(layout.button(), 0) { click() }
            // however _click() will properly fail
            expectThrows(IllegalStateException::class, "The Button[] is nested in a disabled component") {
                expectClickCount(layout.button(), 0) { _click() }
            }
        }

        test("invisible button") {
            // click() does nothing without an actual Browser, bummer
            expectClickCount(Button().apply { isVisible = false }, 0) { click() }
            // however _click() will properly fail
            expectThrows(IllegalStateException::class, "The Button[INVIS] is not effectively visible") {
                expectClickCount(Button().apply { isVisible = false }, 0) { _click() }
            }
        }
    }

    group("HasValue.setValue()") {
        test("enabled check box") {
            expect(true) { Checkbox().apply { value = true } .value }
            expect(true) { Checkbox().apply { _value = true } .value }
        }

        test("disabled check box") {
            // Vaadin ignores the enabled flag and updates the value happily.
            expect(true) { Checkbox().apply { isEnabled = false; value = true } .value }
            // However, calling _value will fail
            val cb = Checkbox().apply { isEnabled = false }
            expectThrows(IllegalStateException::class, "The Checkbox[DISABLED, value='false'] is not enabled") {
                cb._value = true
            }
            expect(false) { cb.value }
        }

        test("invisible check box") {
            expect(true) { Checkbox().apply { isVisible = false; value = true } .value }
            // However, calling _value will fail
            val cb = Checkbox().apply { isVisible = false }
            expectThrows(IllegalStateException::class, "The Checkbox[INVIS, value='false'] is not effectively visible") {
                cb._value = true
            }
            expect(false) { cb.value }
        }

        test("check box with parent disabled") {
            val layout = VerticalLayout().apply { isEnabled = false }
            expect(false) { layout.isEffectivelyEnabled() }
            // Vaadin ignores the enabled flag and updates the value happily.
            expect(true) { layout.checkBox { value = true } .value }
            // However, calling _value will fail
            val cb = layout.checkBox()
            expectThrows(IllegalStateException::class, "The Checkbox[value='false'] is nested in a disabled component") {
                cb._value = true
            }
            expect(false) { cb.value }
        }

        test("read-only check box") {
            var cb = Checkbox().apply { isReadOnly = true }
            // surprisingly this works too
            cb.value = true
            expect(true) { cb.value }

            cb = Checkbox().apply { isReadOnly = true }
            expectThrows(IllegalStateException::class, "The Checkbox[RO, value='false'] is read-only") {
                cb._value = true
            }
            expect(false) { cb.value }
        }
    }
})

@Route("testing")
class TestingView : Div()
