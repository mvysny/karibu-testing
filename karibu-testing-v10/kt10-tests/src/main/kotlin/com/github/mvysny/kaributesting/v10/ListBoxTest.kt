package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.listbox.ListBox
import com.vaadin.flow.component.listbox.ListBoxBase
import com.vaadin.flow.component.listbox.MultiSelectListBox
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.data.renderer.TextRenderer
import com.vaadin.flow.function.SerializableFunction

internal fun DynaNodeGroup.listBoxTestbatch() {

    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    group("ListBox") {
        tests { ListBox() }
    }
    group("MultiSelectListBox") {
        tests { MultiSelectListBox() }
    }
}

private fun DynaNodeGroup.tests(component: () -> ListBoxBase<*, String, *>) {
    group("getRenderedItems()") {
        test("empty") {
            expectList() { component().getRenderedItems() }
        }
        test("TextRenderer") {
            val lb = component()
            lb.setItems("1", "2", "3")
            lb.setRenderer(TextRenderer { "item $it" })
            expectList("item 1", "item 2", "item 3") {
                lb.getRenderedItems()
            }
        }
        test("ComponentRenderer") {
            val lb = component()
            lb.setItems("1", "2", "3")
            lb.setRenderer(ComponentRenderer(SerializableFunction { Span("item $it") }))
            expectList("Span[text='item 1']", "Span[text='item 2']", "Span[text='item 3']") {
                lb.getRenderedItems()
            }
        }
    }
}
