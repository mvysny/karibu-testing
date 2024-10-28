package com.github.mvysny.kaributesting.v10

import com.github.mvysny.karibudsl.v10.checkBox
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect

abstract class AbstractHasValueTests {
    @Nested inner class `HasValue-setValue()` {
        @Test fun `enabled check box`() {
            expect(true) { Checkbox().apply { value = true } .value }
            expect(true) { Checkbox().apply { _value = true } .value }
        }

        @Test fun `disabled check box`() {
            // Vaadin ignores the enabled flag and updates the value happily.
            expect(true) { Checkbox().apply { isEnabled = false; value = true } .value }
            // However, calling _value will fail
            val cb = Checkbox().apply { isEnabled = false }
            expectThrows(IllegalStateException::class, "The Checkbox[DISABLED, value='false'] is not enabled") {
                cb._value = true
            }
            expect(false) { cb.value }
        }

        @Test fun `invisible check box`() {
            expect(true) { Checkbox().apply { isVisible = false; value = true } .value }
            // However, calling _value will fail
            val cb = Checkbox().apply { isVisible = false }
            expectThrows(IllegalStateException::class, "The Checkbox[INVIS, value='false'] is not effectively visible") {
                cb._value = true
            }
            expect(false) { cb.value }
        }

        @Test fun `check box with parent disabled`() {
            val layout = VerticalLayout().apply { isEnabled = false }
            // Vaadin ignores the enabled flag and updates the value happily.
            expect(true) { layout.checkBox { value = true } .value }
            // However, calling _value will fail
            val cb = layout.checkBox()
            expectThrows(IllegalStateException::class, "The Checkbox[DISABLED, value='false'] is nested in a disabled component") {
                cb._value = true
            }
            expect(false) { cb.value }
        }

        @Test fun `read-only check box`() {
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
    @Nested inner class `Fire value change` {
        @Test fun testTextField() {
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
        @Test fun testCheckBox() {
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
        @Nested inner class `from client`() {
            @Test fun testTextField() {
                val tf = TextField()
                var called = false
                tf.addValueChangeListener {
                    expect(false) { called }
                    expect(true) { it.isFromClient }
                    called = true
                }
                tf._setValue("foo")
                expect(true) { called }
            }
            @Test fun testCheckBox() {
                val tf = Checkbox()
                var called = false
                tf.addValueChangeListener {
                    expect(false) { called }
                    expect(true) { it.isFromClient }
                    called = true
                }
                tf._setValue(true)
                expect(true) { called }
            }
        }
    }
}
