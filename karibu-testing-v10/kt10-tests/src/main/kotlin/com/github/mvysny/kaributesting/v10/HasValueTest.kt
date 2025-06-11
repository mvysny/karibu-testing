package com.github.mvysny.kaributesting.v10

import com.github.mvysny.karibudsl.v10.checkBox
import com.vaadin.flow.component.HasValue
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.combobox.MultiSelectComboBox
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.datetimepicker.DateTimePicker
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.textfield.BigDecimalField
import com.vaadin.flow.component.textfield.EmailField
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.component.textfield.PasswordField
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.component.timepicker.TimePicker
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.test.expect

abstract class AbstractHasValueTests {
    abstract class TestImpl<V>(val fieldSupplier: () -> HasValue<*, V?>, val testValue: V) {
        private val field = fieldSupplier()

        @Test fun `enabled component`() {
            expect(testValue) { fieldSupplier().apply { value = testValue } .value }
            expect(testValue) { fieldSupplier().apply { _value = testValue } .value }
        }

        @Test fun valueChangeListener_setValue_fromClient_true() {
            expect(false) { field.value == testValue }
            var called = false
            field.addValueChangeListener {
                expect(false) { called }
                expect(true) { it.isFromClient }
                called = true
            }
            field._setValue(testValue)
            expect(true) { called }
        }

        @Test fun valueChangeListener_fireValueChange_fromClient_true() {
            var called = false
            field.addValueChangeListener {
                expect(false) { called }
                expect(true) { it.isFromClient }
                called = true
            }
            field._fireValueChange()
            expect(true) { called }
        }
    }

    @Nested inner class TextFieldTests : TestImpl<String>({ TextField() }, "foo")
    @Nested inner class TextAreaTests : TestImpl<String>({ TextArea() }, "foo")
    @Nested inner class PasswordFieldTests : TestImpl<String>({ PasswordField() }, "foo")
    @Nested inner class EmailFieldTests : TestImpl<String>({ EmailField() }, "foo@bar.baz")
    @Nested inner class CheckboxTests : TestImpl<Boolean>({ Checkbox() }, true)
    @Nested inner class IntegerFieldTests : TestImpl<Int?>({ IntegerField() }, 2)
    @Nested inner class BigDecimalFieldTests : TestImpl<BigDecimal>({ BigDecimalField() }, BigDecimal.valueOf(2))
    @Nested inner class DatePickerTests : TestImpl<LocalDate>({ DatePicker() }, LocalDate.now().plusDays(10))
    @Nested inner class DateTimePickerTests : TestImpl<LocalDateTime>({ DateTimePicker() }, LocalDateTime.of(LocalDate.now(), LocalTime.NOON))
    @Nested inner class ComboBoxTests : TestImpl<String>({ ComboBox("label", "foo", "bar") }, "foo") {
        @Test fun `_setValue should fail if ComboBox has no items`() {
            // ComboBox.setValue() fails if ComboBox has no items. _setValue() must emulate that.
            expectThrows<IllegalStateException> {
                ComboBox<String>()._setValue("foo")
            }
        }
        @Test fun `setting _value should fail if ComboBox has no items`() {
            // ComboBox.setValue() fails if ComboBox has no items. _setValue() must emulate that.
            expectThrows<IllegalStateException> {
                ComboBox<String>()._value = "foo"
            }
        }
    }
    @Nested inner class MultiSelectComboBoxTests : TestImpl<Set<String>>({ MultiSelectComboBox("label", "foo", "bar") }, setOf("foo")) {
        @Test fun `_setValue should fail if ComboBox has no items`() {
            // ComboBox.setValue() fails if ComboBox has no items. _setValue() must emulate that.
            expectThrows<IllegalStateException> {
                MultiSelectComboBox<String>()._setValue(setOf("foo"))
            }
        }
        @Test fun `setting _value should fail if ComboBox has no items`() {
            // ComboBox.setValue() fails if ComboBox has no items. _setValue() must emulate that.
            expectThrows<IllegalStateException> {
                MultiSelectComboBox<String>()._value = setOf("foo")
            }
        }
    }
    @Nested inner class SelectTests : TestImpl<String>({ Select() }, "foo")
    @Nested inner class TimePickerTests : TestImpl<LocalTime>({ TimePicker() }, LocalTime.NOON)
    // @todo mavi CustomField, AbstractField, all vaadin-core HasValues
    // @todo mavi emulate maybe how client-side sets value, by calling AbstractField.setModelValue()

    @Nested inner class `HasValue-setValue()` {
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
}
