package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.listbox.ListBox
import com.vaadin.flow.component.listbox.ListBoxBase
import com.vaadin.flow.component.listbox.MultiSelectListBox
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.data.renderer.TextRenderer
import com.vaadin.flow.function.SerializableFunction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

abstract class AbstractListBoxTests() {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Nested inner class ListBoxTests : AbstractListBoxBaseTests({ ListBox() })
    @Nested inner class MultiSelectListBoxTests : AbstractListBoxBaseTests({ MultiSelectListBox() })
}

abstract class AbstractListBoxBaseTests(val component: () -> ListBoxBase<*, String, *>) {
    @Test fun empty() {
        expectList() { component().getRenderedItems() }
    }
    @Test fun textRenderer() {
        val lb = component()
        lb.setItems2("1", "2", "3")
        lb.setRenderer(TextRenderer { "item $it" })
        expectList("item 1", "item 2", "item 3") {
            lb.getRenderedItems()
        }
    }
    @Test fun componentRenderer() {
        val lb = component()
        lb.setItems2("1", "2", "3")
        lb.setRenderer(ComponentRenderer(SerializableFunction { Span("item $it") }))
        expectList("Span[text='item 1']", "Span[text='item 2']", "Span[text='item 3']") {
            lb.getRenderedItems()
        }
    }
}

fun <T> ListBoxBase<*, T, *>.setItems2(vararg items: T) {
    setItems(items.toList())
}
