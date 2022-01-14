package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeDsl
import com.github.mvysny.dynatest.DynaNodeGroup
import com.vaadin.flow.component.checkbox.CheckboxGroup

@DynaNodeDsl
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
}

fun <T> CheckboxGroup<T>.setItems2(vararg items: T) {
    // workaround for java.lang.NoSuchMethodError: 'void com.vaadin.flow.component.listbox.MultiSelectListBox.setItems(java.util.Collection)'
    // the setItems() method has been moved in Vaadin 22+, from HasItems to HasListDataView
    setDataProvider(ListDataProvider2(items.toList()))
}
