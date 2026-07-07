package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.radiobutton.RadioButtonGroup
import com.vaadin.flow.data.renderer.TextRenderer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect

abstract class AbstractRadioButtonTests() {
    @Nested inner class getItemLabels {
        @Test fun empty() {
            expectList() { RadioButtonGroup<String>().getItemLabels() }
        }
        @Test fun `no renderer set uses toString()`() {
            expectList("1", "2", "3") {
                val rb = RadioButtonGroup<Int>()
                rb.setItems2(1, 2, 3)
                rb.getItemLabels()
            }
            expectList("1", "2", "3") {
                val rb = RadioButtonGroup<String>()
                rb.setItems2("1", "2", "3")
                rb.getItemLabels()
            }
        }
        @Test fun `text renderer`() {
            expectList("Item #1", "Item #2", "Item #3") {
                val rb = RadioButtonGroup<Int>()
                rb.setItems2(1, 2, 3)
                rb.setRenderer(TextRenderer { "Item #$it" })
                rb.getItemLabels()
            }
        }
    }
    @Nested inner class selectByLabel {
        @Test fun empty() {
            expectThrows<AssertionError>("RadioButtonGroup[value='null', dataprovider='ListDataProvider<?>(0 items)']: No item found with label 'foo'") {
                RadioButtonGroup<String>().selectByLabel("foo")
            }
        }
        @Test fun `fails on no match`() {
            val c = RadioButtonGroup<String>()
            c.setItems2("1", "2", "3")
            expectThrows<AssertionError>("RadioButtonGroup[value='null', dataprovider='ListDataProvider<String>(3 items)']: No item found with label 'foo'. Available items: ['1'=>1, '2'=>2, '3'=>3]") {
                c.selectByLabel("foo")
            }
        }
        @Test fun `simple with itemLabelGenerator`() {
            val cg = RadioButtonGroup<String>()
            cg.setItems2("1", "2", "3")
            cg.setRenderer(TextRenderer { "Item #$it" })
            cg.selectByLabel("Item #2")
            expect("2") { cg._value }
        }
    }
}

fun <T> RadioButtonGroup<T>.setItems2(vararg items: T) {
    setItems(items.toList())
}
