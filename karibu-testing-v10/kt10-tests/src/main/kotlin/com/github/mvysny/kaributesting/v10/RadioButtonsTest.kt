package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.vaadin.flow.component.radiobutton.RadioButtonGroup
import com.vaadin.flow.data.renderer.TextRenderer

@DynaTestDsl
internal fun DynaNodeGroup.radioButtonTests() {
    group("getItemLabels()") {
        test("empty") {
            expectList() { RadioButtonGroup<String>().getItemLabels() }
        }
        test("no renderer set uses toString()") {
            expectList("1", "2", "3") {
                val rb = RadioButtonGroup<Int>()
                rb.setItems2(listOf(1, 2, 3))
                rb.getItemLabels()
            }
            expectList("1", "2", "3") {
                val rb = RadioButtonGroup<String>()
                rb.setItems2(listOf("1", "2", "3"))
                rb.getItemLabels()
            }
        }
        test("text renderer") {
            expectList("Item #1", "Item #2", "Item #3") {
                val rb = RadioButtonGroup<Int>()
                rb.setItems2(listOf(1, 2, 3))
                rb.setRenderer(TextRenderer { "Item #$it" })
                rb.getItemLabels()
            }
        }
    }
}

fun <T> RadioButtonGroup<T>.setItems2(items: Collection<T>) {
    // this way it's also compatible with Vaadin 18.
    dataProvider = ListDataProvider2(items)
}
