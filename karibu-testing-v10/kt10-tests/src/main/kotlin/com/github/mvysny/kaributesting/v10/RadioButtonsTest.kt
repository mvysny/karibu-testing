package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.checkBox
import com.vaadin.flow.component.ClickNotifier
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.radiobutton.RadioButtonGroup
import com.vaadin.flow.data.renderer.TextRenderer
import kotlin.test.expect
import kotlin.test.fail

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
