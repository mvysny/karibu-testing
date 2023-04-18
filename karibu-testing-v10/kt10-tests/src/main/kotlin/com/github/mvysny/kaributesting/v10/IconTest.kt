package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.checkBox
import com.github.mvysny.karibudsl.v10.icon
import com.vaadin.flow.component.ClickNotifier
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import kotlin.test.expect
import kotlin.test.fail

@DynaTestDsl
internal fun DynaNodeGroup.iconTests() {
    group("_click") {
        fun expectClickCount(icon: Icon, clickCount: Int, block: Icon.()->Unit) {
            var clicked = 0
            icon.addClickListener { if (++clicked > clickCount) fail("Clicked more than $clickCount times") }
            icon.block()
            expect(clickCount) { clicked }
        }

        test("enabled icon") {
            expectClickCount(Icon(), 1) { _click() }
        }

        test("icon with parent disabled") {
            val layout = VerticalLayout().apply { isEnabled = false }
            expect(false) { layout.isEnabled } // sanity check, to test that isEnabled works as intended
            expect(false) { layout.icon(VaadinIcon.ABACUS).isEnabled } // sanity check, to test that isEnabled works as intended
            expectThrows(IllegalStateException::class, "The Icon[DISABLED, icon='vaadin:abacus'] is nested in a disabled component") {
                expectClickCount(layout.icon(VaadinIcon.ABACUS), 0) { _click() }
            }
        }

        test("invisible icon") {
            expectThrows(IllegalStateException::class, "The Icon[INVIS, icon='vaadin:vaadin-h'] is not effectively visible") {
                expectClickCount(Icon().apply { isVisible = false }, 0) { _click() }
            }
        }
    }
}
