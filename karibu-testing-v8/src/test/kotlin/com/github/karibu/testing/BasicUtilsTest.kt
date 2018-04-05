package com.github.karibu.testing

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.vaadin.ui.Button
import com.vaadin.ui.VerticalLayout
import kotlin.test.expect

class BasicUtilsTest : DynaTest({
    group("button click") {
        fun expectClickCount(button: Button, clickCount: Int, block: Button.()->Unit) {
            var clicked = 0
            button.addClickListener { if (++clicked > clickCount) kotlin.test.fail("Clicked more than $clickCount times") }
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
            expectThrows(IllegalArgumentException::class, "The Button[DISABLED] is not enabled") {
                expectClickCount(Button().apply { isEnabled = false }, 0) { _click() }
            }
        }

        test("button with parent disabled") {
            val layout = VerticalLayout().apply { isEnabled = false }
            // the Vaadin's Button.click() works regardless of whether the button's parent is enabled or not.
            expectClickCount(layout.button(), 1) { click() }
            // however _click() will properly fail
            expectThrows(IllegalArgumentException::class, "The Button[] is nested in a disabled component") {
                expectClickCount(layout.button(), 0) { _click() }
            }
        }
    }
})
