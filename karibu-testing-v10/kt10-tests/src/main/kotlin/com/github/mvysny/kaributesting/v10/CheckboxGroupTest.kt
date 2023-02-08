package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.dynatest.expectThrows
import com.vaadin.flow.component.checkbox.CheckboxGroup
import com.vaadin.flow.data.provider.ListDataProvider
import kotlin.test.expect

@DynaTestDsl
internal fun DynaNodeGroup.checkboxGroupTests() {

    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    group("getItemLabels()") {
        test("empty") {
            expectList() { CheckboxGroup<String>().getItemLabels() }
        }
        test("with itemLabelGenerator") {
            val cg = CheckboxGroup<String>()
            cg.setItems2("1", "2", "3")
            cg.setItemLabelGenerator { "item $it" }
            expectList("item 1", "item 2", "item 3") {
                cg.getItemLabels()
            }
        }
    }
    group("selectByLabel()") {
        test("empty") {
            expectThrows<AssertionError>("CheckboxGroup[value='[]', dataprovider='ListDataProvider<?>(0 items)']: No item found with label(s) '[foo]'. Available items: []") {
                CheckboxGroup<String>().selectByLabel("foo")
            }
        }
        test("fails on no match") {
            val c = CheckboxGroup<String>()
            c.setItems2("1", "2", "3")
            expectThrows<AssertionError>("CheckboxGroup[value='[]', dataprovider='ListDataProvider<String>(3 items)']: No item found with label(s) '[foo]'. Available items: [1, 2, 3]") {
                c.selectByLabel("foo")
            }
        }
        test("simple with itemLabelGenerator") {
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
