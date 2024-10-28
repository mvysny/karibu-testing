package com.github.mvysny.kaributesting.v10

import com.github.mvysny.karibudsl.v10.button
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
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect
import kotlin.test.fail

abstract class AbstractButtonTests {
    @Nested inner class `button click` {
        fun expectClickCount(button: Button, clickCount: Int, block: Button.()->Unit) {
            var clicked = 0
            button.addClickListener { if (++clicked > clickCount) fail("Clicked more than $clickCount times") }
            button.block()
            expect(clickCount) { clicked }
        }

        @Test fun `enabled button`() {
            expectClickCount(Button(), 1) { click() }
            expectClickCount(Button(), 1) { _click() }
        }

        @Test fun `disabled button`() {
            // click() does not check for disabled state. Actually, in Vaadin 24.2.2+ it does, and the event is not fired.
            // doesn't matter - you should always use _click()
//            expectClickCount(Button().apply { isEnabled = false }, 0) { click() }
            // however _click() will properly fail
            expectThrows(IllegalStateException::class, "The Button[DISABLED] is not enabled") {
                expectClickCount(Button().apply { isEnabled = false }, 0) { _click() }
            }
        }

        @Test fun `button with parent disabled`() {
            val layout = VerticalLayout().apply { isEnabled = false }
            expect(false) { layout.isEnabled } // sanity check, to test that isEnabled works as intended
            expect(false) { layout.button().isEnabled } // sanity check, to test that isEnabled works as intended
            // click() does not check for parent disabled state. Actually, in Vaadin 24.2.2+ it does, and the event is not fired.
            // doesn't matter - you should always use _click()
//            expectClickCount(layout.button(), 1) { click() }
            // however _click() will properly fail
            expectThrows(IllegalStateException::class, "The Button[DISABLED] is nested in a disabled component") {
                expectClickCount(layout.button(), 0) { _click() }
            }
        }

        @Test fun `invisible button`() {
            // click() does not check for invisible state
            expectClickCount(Button().apply { isVisible = false }, 1) { click() }
            // however _click() will properly fail
            expectThrows(IllegalStateException::class, "The Button[INVIS] is not effectively visible") {
                expectClickCount(Button().apply { isVisible = false }, 0) { _click() }
            }
        }
    }

    @Nested inner class _click() {
        @Nested inner class ButtonTests : AbstractClickTests("Button", true, { Button() })
        @Nested inner class CheckboxTests : AbstractClickTests("Checkbox", true, { Checkbox() })
        @Nested inner class FormLayoutTests : AbstractClickTests("FormLayout", true, { FormLayout() })
        @Nested inner class IconTests : AbstractClickTests("Icon", false, { Icon() })
        @Nested inner class VerticalLayoutTests : AbstractClickTests("VerticalLayout", true, { VerticalLayout() })
        @Nested inner class HorizontalLayoutTests : AbstractClickTests("HorizontalLayout", true, { HorizontalLayout() })
        @Nested inner class ImageTests : AbstractClickTests("Image", true, { Image() })
        @Nested inner class ListItemTests : AbstractClickTests("ListItem", true, { ListItem() })
        @Nested inner class UnorderedListTests : AbstractClickTests("UnorderedList", true, { UnorderedList() })
        @Nested inner class DivTests : AbstractClickTests("Div", true, { Div() })
        @Nested inner class NativeButtonTests : AbstractClickTests("NativeButton", true, { NativeButton() })
        @Nested inner class FlexLayoutTests : AbstractClickTests("FlexLayout", true, { FlexLayout() })
        @Nested inner class ParagraphTests : AbstractClickTests("Paragraph", true, { Paragraph() })
        @Nested inner class H1Tests : AbstractClickTests("H1", true, { H1() })
        @Nested inner class SpanTests : AbstractClickTests("Span", true, { Span() })
    }
}

abstract class AbstractClickTests(val componentName: String, val hasEnabled: Boolean, val componentProvider: () -> ClickNotifier<*>) {
    fun <T : ClickNotifier<*>> expectClickCount(button: T, clickCount: Int) {
        var clicked = 0
        button.addClickListener { e ->
            if (++clicked > clickCount) fail("Clicked more than $clickCount times")
            expect(true) { e.isFromClient }
        }
        button._click()
        expect(clickCount) { clicked }
    }

    @Test fun `click succeeds on enabled component`() {
        expectClickCount(componentProvider(), 1)
    }

    @Test fun `_click() also fires DOM Event`() {
        val c = componentProvider()
        var domClicked = 0
        (c as HasElement).element.addEventListener("click") {domClicked++ }
        c._click()
        expect(1) { domClicked }
        expectClickCount(c, 1)
        expect(2) { domClicked } // both DOM listener and click listener are called when both are registered.
    }

    @Test fun `click fails on disabled component`() {
        assumeTrue(hasEnabled)
        expectThrows(IllegalStateException::class, "is not enabled") {
            val c = componentProvider()
            (c as HasEnabled).isEnabled = false
            expectClickCount(c, 0)
        }
    }

    @Test fun `click fails on component with parent disabled`() {
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

    @Test fun `click fails on invisible component`() {
        expectThrows(IllegalStateException::class, "is not effectively visible") {
            val c = componentProvider()
            (c as Component).isVisible = false
            expectClickCount(c, 0)
        }
    }
}
