package com.github.mvysny.kaributesting.v8

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v8.button
import com.vaadin.ui.Button
import com.vaadin.ui.VerticalLayout
import kotlin.test.expect
import kotlin.test.fail

class ButtonTest : DynaTest({
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
})
