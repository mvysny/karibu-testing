package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.checkBox
import com.vaadin.flow.component.ClickNotifier
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import kotlin.test.expect
import kotlin.test.fail

internal fun DynaNodeGroup.buttonTestbatch() {
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
            // click() does not check for disabled state
            expectClickCount(Button().apply { isEnabled = false }, 1) { click() }
            // however _click() will properly fail
            expectThrows(IllegalStateException::class, "The Button[DISABLED] is not enabled") {
                expectClickCount(Button().apply { isEnabled = false }, 0) { _click() }
            }
        }

        test("button with parent disabled") {
            val layout = VerticalLayout().apply { isEnabled = false }
            expect(false) { layout.isEffectivelyEnabled() }
            expect(false) { layout.button().isEffectivelyEnabled() }
            // click() does not check for parent disabled state
            expectClickCount(layout.button(), 1) { click() }
            // however _click() will properly fail
            expectThrows(IllegalStateException::class, "The Button[DISABLED] is nested in a disabled component") {
                expectClickCount(layout.button(), 0) { _click() }
            }
        }

        test("invisible button") {
            // click() does not check for invisible state
            expectClickCount(Button().apply { isVisible = false }, 1) { click() }
            // however _click() will properly fail
            expectThrows(IllegalStateException::class, "The Button[INVIS] is not effectively visible") {
                expectClickCount(Button().apply { isVisible = false }, 0) { _click() }
            }
        }
    }
    group("ClickNotifier click") {
        fun <T : ClickNotifier<*>> expectClickCount(button: T, clickCount: Int, block: T.()->Unit) {
            var clicked = 0
            button.addClickListener { if (++clicked > clickCount) fail("Clicked more than $clickCount times") }
            button.block()
            expect(clickCount) { clicked }
        }

        test("enabled cbeckbox") {
            expectClickCount(Checkbox(), 1) { _click() }
        }

        test("disabled cbeckbox") {
            expectThrows(IllegalStateException::class, "The Checkbox[DISABLED, value='false'] is not enabled") {
                expectClickCount(Checkbox().apply { isEnabled = false }, 0) { _click() }
            }
        }

        test("Checkbox with parent disabled") {
            val layout = VerticalLayout().apply { isEnabled = false }
            expect(false) { layout.isEffectivelyEnabled() }
            // click() does not check for parent disabled state
            // however _click() will properly fail
            expectThrows(IllegalStateException::class, "The Checkbox[DISABLED, value='false'] is nested in a disabled component") {
                expectClickCount(layout.checkBox(), 0) { _click() }
            }
        }

        test("invisible button") {
            // click() does not check for invisible state
            // however _click() will properly fail
            expectThrows(IllegalStateException::class, "The Checkbox[INVIS, value='false'] is not effectively visible") {
                expectClickCount(Checkbox().apply { isVisible = false }, 0) { _click() }
            }
        }
    }
}
