package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.checkbox.CheckboxGroup
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect

abstract class AbstractCheckboxGroupTests() {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Nested inner class getItemLabels {
        @Test fun empty() {
            expectList() { CheckboxGroup<String>().getItemLabels() }
        }
        @Test fun `with itemLabelGenerator`() {
            val cg = CheckboxGroup<String>()
            cg.setItems2("1", "2", "3")
            cg.setItemLabelGenerator { "item $it" }
            expectList("item 1", "item 2", "item 3") {
                cg.getItemLabels()
            }
        }
    }
    @Nested inner class selectByLabel {
        @Test fun empty() {
            expectThrows<AssertionError>("CheckboxGroup[value='[]', dataprovider='ListDataProvider<?>(0 items)']: No item found with label(s) '[foo]'. Available items: []") {
                CheckboxGroup<String>().selectByLabel("foo")
            }
        }
        @Test fun `fails on no match`() {
            val c = CheckboxGroup<String>()
            c.setItems2("1", "2", "3")
            expectThrows<AssertionError>("CheckboxGroup[value='[]', dataprovider='ListDataProvider<String>(3 items)']: No item found with label(s) '[foo]'. Available items: [1, 2, 3]") {
                c.selectByLabel("foo")
            }
        }
        @Test fun `simple with itemLabelGenerator`() {
            val cg = CheckboxGroup<String>()
            cg.setItems2("1", "2", "3")
            cg.setItemLabelGenerator { "item $it" }
            cg.selectByLabel("item 2")
            expect(setOf("2")) { cg._value }
        }
    }
}

fun <T> CheckboxGroup<T>.setItems2(vararg items: T) {
    setItems(items.toList())
}
