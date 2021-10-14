package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.vaadin.flow.component.radiobutton.RadioButtonGroup
import com.vaadin.flow.data.renderer.TextRenderer

internal fun DynaNodeGroup.radioButtonTests() {
    group("getItemLabels()") {
        test("empty") {
            expectList() { RadioButtonGroup<String>().getItemLabels() }
        }
        test("no renderer set uses toString()") {
            expectList("1", "2", "3") {
                val rb = RadioButtonGroup<Int>()
                rb.setItems(1, 2, 3)
                rb.getItemLabels()
            }
            expectList("1", "2", "3") {
                val rb = RadioButtonGroup<String>()
                rb.setItems("1", "2", "3")
                rb.getItemLabels()
            }
        }
        test("text renderer") {
            expectList("Item #1", "Item #2", "Item #3") {
                val rb = RadioButtonGroup<Int>()
                rb.setItems(1, 2, 3)
                rb.setRenderer(TextRenderer { "Item #$it" })
                rb.getItemLabels()
            }
        }
    }
}
