package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.checkBox
import com.github.mvysny.karibudsl.v10.icon
import com.vaadin.flow.component.ClickNotifier
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasElement
import com.vaadin.flow.component.HasEnabled
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.html.Image
import com.vaadin.flow.component.html.ListItem
import com.vaadin.flow.component.html.NativeButton
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.html.UnorderedList
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import kotlin.test.expect
import kotlin.test.fail

@DynaTestDsl
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
            expect(false) { layout.isEnabled } // sanity check, to test that isEnabled works as intended
            expect(false) { layout.button().isEnabled } // sanity check, to test that isEnabled works as intended
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

    group("_click()") {
        clickTest("Button", true) { Button() }
        clickTest("Checkbox", true) { Checkbox() }
        clickTest("FormLayout", true) { FormLayout() }
        clickTest("Icon", false) { Icon() }
        clickTest("VerticalLayout", true) { VerticalLayout() }
        clickTest("HorizontalLayout", true) { HorizontalLayout() }
        clickTest("Image", true) { Image() }
        clickTest("ListItem", true) { ListItem() }
        clickTest("UnorderedList", true) { UnorderedList() }
        clickTest("Div", true) { Div() }
        clickTest("NativeButton", true) { NativeButton() }
        clickTest("FlexLayout", true) { FlexLayout() }
        clickTest("Paragraph", true) { Paragraph() }
        clickTest("H1", true) { H1() }
        clickTest("Span", true) { Span() }
    }
}

@DynaTestDsl
fun DynaNodeGroup.clickTest(componentName: String, hasEnabled: Boolean, componentProvider: () -> ClickNotifier<*>) {
    fun <T : ClickNotifier<*>> expectClickCount(button: T, clickCount: Int) {
        var clicked = 0
        button.addClickListener { e ->
            if (++clicked > clickCount) fail("Clicked more than $clickCount times")
            expect(true) { e.isFromClient }
        }
        button._click()
        expect(clickCount) { clicked }
    }

    group(componentName) {
        test("click succeeds on enabled component") {
            expectClickCount(componentProvider(), 1)
        }

        test("_click() also fires DOM Event") {
            val c = componentProvider()
            var domClicked = 0
            (c as HasElement).element.addEventListener("click") {domClicked++ }
            c._click()
            expect(1) { domClicked }
            expectClickCount(c, 1)
            expect(2) { domClicked } // both DOM listener and click listener are called when both are registered.
        }

        if (hasEnabled) {
            test("click fails on disabled component") {
                expectThrows(IllegalStateException::class, "is not enabled") {
                    val c = componentProvider()
                    (c as HasEnabled).isEnabled = false
                    expectClickCount(c, 0)
                }
            }
        }

        test("click fails on component with parent disabled") {
            val layout = VerticalLayout()
            layout.isEnabled = false
            val c = componentProvider()
            layout.add(c as Component)
            expect(false) { c.isEnabled } // sanity check, to test that isEnabled works as intended
            // click() does not check for parent disabled state
            // however _click() will properly fail
            expectThrows(IllegalStateException::class, "is nested in a disabled component") {
                expectClickCount(c, 0)
            }
        }

        test("click fails on invisible component") {
            expectThrows(IllegalStateException::class, "is not effectively visible") {
                val c = componentProvider()
                (c as Component).isVisible = false
                expectClickCount(c, 0)
            }
        }
    }
}
