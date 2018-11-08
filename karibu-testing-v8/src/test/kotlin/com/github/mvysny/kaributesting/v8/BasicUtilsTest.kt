package com.github.mvysny.kaributesting.v8

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.github.vok.karibudsl.button
import com.github.vok.karibudsl.checkBox
import com.vaadin.ui.Button
import com.vaadin.ui.CheckBox
import com.vaadin.ui.VerticalLayout
import kotlin.test.expect
import kotlin.test.fail

class BasicUtilsTest : DynaTest({
    group("button click") {
        fun expectClickCount(button: Button, clickCount: Int, block: Button.()->Unit) {
            var clicked = 0
            button.addClickListener { if (++clicked > clickCount) fail("Clicked more than $clickCount times") }
            button.block()
            expect(clickCount) { clicked }
        }

        test("enabled button") {
            expectClickCount(Button(), 1) { click() }
            expectClickCount(Button(), 1) { _click() }
        }

        test("disabled button") {
            // the Vaadin's Button.click() will silently do nothing when the button is disabled
            expectClickCount(Button().apply { isEnabled = false }, 0) { click() }
            // however _click() will properly fail
            expectThrows(IllegalStateException::class, "The Button[DISABLED] is not enabled") {
                expectClickCount(Button().apply { isEnabled = false }, 0) { _click() }
            }
        }

        test("button with parent disabled") {
            val layout = VerticalLayout().apply { isEnabled = false }
            // the Vaadin's Button.click() works regardless of whether the button's parent is enabled or not.
            expectClickCount(layout.button(), 1) { click() }
            // however _click() will properly fail
            expectThrows(IllegalStateException::class, "The Button[] is nested in a disabled component") {
                expectClickCount(layout.button(), 0) { _click() }
            }
        }

        test("invisible button") {
            expectClickCount(Button().apply { isVisible = false }, 1) { click() }
            // however _click() will properly fail
            expectThrows(IllegalStateException::class, "The Button[INVIS] is not effectively visible") {
                expectClickCount(Button().apply { isVisible = false }, 0) { _click() }
            }
        }
    }

    group("HasValue.setValue()") {
        test("enabled check box") {
            expect(true) { CheckBox().apply { value = true } .value }
            expect(true) { CheckBox().apply { _value = true } .value }
        }

        test("disabled check box") {
            // Vaadin ignores the enabled flag and updates the value happily.
            expect(true) { CheckBox().apply { isEnabled = false; value = true } .value }
            // However, calling _value will fail
            val cb = CheckBox().apply { isEnabled = false }
            expectThrows(IllegalStateException::class, "The CheckBox[DISABLED, value='false'] is not enabled") {
                cb._value = true
            }
            expect(false) { cb.value }
        }

        test("invisible check box") {
            expect(true) { CheckBox().apply { isVisible = false; value = true } .value }
            // However, calling _value will fail
            val cb = CheckBox().apply { isVisible = false }
            expectThrows(IllegalStateException::class, "The CheckBox[INVIS, value='false'] is not effectively visible") {
                cb._value = true
            }
            expect(false) { cb.value }
        }

        test("check box with parent disabled") {
            val layout = VerticalLayout().apply { isEnabled = false }
            // Vaadin ignores the enabled flag and updates the value happily.
            expect(true) { layout.checkBox { value = true } .value }
            // However, calling _value will fail
            val cb = layout.checkBox()
            expectThrows(IllegalStateException::class, "The CheckBox[value='false'] is nested in a disabled component") {
                cb._value = true
            }
            expect(false) { cb.value }
        }

        test("read-only check box") {
            var cb = CheckBox().apply { isReadOnly = true }
            // surprisingly this works too. I'm pretty sure it failed with ReadOnlyException in Vaadin 7...
            cb.value = true
            expect(true) { cb.value }

            cb = CheckBox().apply { isReadOnly = true }
            expectThrows(IllegalStateException::class, "The CheckBox[RO, value='false'] is read-only") {
                cb._value = true
            }
            expect(false) { cb.value }
        }
    }
})
