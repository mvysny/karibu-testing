package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.checkBox
import com.vaadin.flow.component.AbstractField
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import kotlin.test.expect

internal fun DynaNodeGroup.hasValueTestbatch() {
    group("HasValue.setValue()") {
        test("enabled check box") {
            expect(true) { Checkbox().apply { value = true } .value }
            expect(true) { Checkbox().apply { _value = true } .value }
        }

        test("disabled check box") {
            // Vaadin ignores the enabled flag and updates the value happily.
            expect(true) { Checkbox().apply { isEnabled = false; value = true } .value }
            // However, calling _value will fail
            val cb = Checkbox().apply { isEnabled = false }
            expectThrows(IllegalStateException::class, "The Checkbox[DISABLED, value='false'] is not enabled") {
                cb._value = true
            }
            expect(false) { cb.value }
        }

        test("invisible check box") {
            expect(true) { Checkbox().apply { isVisible = false; value = true } .value }
            // However, calling _value will fail
            val cb = Checkbox().apply { isVisible = false }
            expectThrows(IllegalStateException::class, "The Checkbox[INVIS, value='false'] is not effectively visible") {
                cb._value = true
            }
            expect(false) { cb.value }
        }

        test("check box with parent disabled") {
            val layout = VerticalLayout().apply { isEnabled = false }
            expect(false) { layout.isEffectivelyEnabled() }
            // Vaadin ignores the enabled flag and updates the value happily.
            expect(true) { layout.checkBox { value = true } .value }
            // However, calling _value will fail
            val cb = layout.checkBox()
            expectThrows(IllegalStateException::class, "The Checkbox[DISABLED, value='false'] is nested in a disabled component") {
                cb._value = true
            }
            expect(false) { cb.value }
        }

        test("read-only check box") {
            var cb = Checkbox().apply { isReadOnly = true }
            // surprisingly this works too
            cb.value = true
            expect(true) { cb.value }

            cb = Checkbox().apply { isReadOnly = true }
            expectThrows(IllegalStateException::class, "The Checkbox[RO, value='false'] is read-only") {
                cb._value = true
            }
            expect(false) { cb.value }
        }
    }
    group("Fire value change") {
        test("TextField") {
            val tf = TextField()
            var called = false
            tf.addValueChangeListener {
                expect(false) { called }
                expect(true) { it.isFromClient }
                called = true
            }
            tf._fireValueChange()
            expect(true) { called }
        }
        test("CheckBox") {
            val tf = Checkbox()
            var called = false
            tf.addValueChangeListener {
                expect(false) { called }
                expect(true) { it.isFromClient }
                called = true
            }
            tf._fireValueChange()
            expect(true) { called }
        }
    }
}
