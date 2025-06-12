package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasEnabled
import com.vaadin.flow.component.HasValue
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.checkbox.CheckboxGroup
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.combobox.MultiSelectComboBox
import com.vaadin.flow.component.customfield.CustomField
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.datetimepicker.DateTimePicker
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.radiobutton.RadioButtonGroup
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

        @Test fun `disabled component`() {
            // Vaadin ignores the enabled flag and updates the value happily.
            expect(testValue) { fieldSupplier().apply { (this as HasEnabled).isEnabled = false; value = testValue } .value }

            // However, calling _value will fail
            val cb = fieldSupplier().apply { (this as HasEnabled).isEnabled = false }
            expectThrows(IllegalStateException::class, " is not enabled") {
                cb._value = testValue
            }
            expect(cb.emptyValue) { cb.value }
        }

        @Test fun `invisible component`() {
            expect(testValue) { fieldSupplier().apply { (this as Component).isVisible = false; value = testValue } .value }
            // However, calling _value will fail
            val cb = fieldSupplier().apply { (this as Component).isVisible = false }
            expectThrows(IllegalStateException::class, " is not effectively visible") {
                cb._value = testValue
            }
            expect(cb.emptyValue) { cb.value }
        }

        @Test fun `check box with parent disabled`() {
            val layout = VerticalLayout().apply { isEnabled = false }
            // Vaadin ignores the enabled flag and updates the value happily.
            var cb = fieldSupplier()
            layout.add(cb as Component)
            cb.value = testValue
            expect(testValue) { cb.value }
            // However, calling _value will fail
            cb = fieldSupplier()
            layout.add(cb as Component)
            expectThrows(IllegalStateException::class, " is nested in a disabled component") {
                cb._value = testValue
            }
            expect(cb.emptyValue) { cb.value }
        }

        @Test fun `read-only check box`() {
            var cb = fieldSupplier().apply { isReadOnly = true }
            // surprisingly this works too
            cb.value = testValue
            expect(testValue) { cb.value }

            cb = fieldSupplier().apply { isReadOnly = true }
            expectThrows(IllegalStateException::class, " is read-only") {
                cb._value = testValue
            }
            expect(cb.emptyValue) { cb.value }
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

        @Test fun valueChangeListener_setValue_fromClient_false() {
            expect(false) { field.value == testValue }
            var called = false
            field.addValueChangeListener {
                expect(false) { called }
                expect(false) { it.isFromClient }
                called = true
            }
            field._setValue(testValue, fromClient = false)
            expect(true) { called }
        }

        @Test fun valueChangeListener_value_fromClient_true() {
            expect(false) { field.value == testValue }
            var called = false
            field.addValueChangeListener {
                expect(false) { called }
                expect(false) { it.isFromClient }
                called = true
            }
            field._value = testValue
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
    @Nested inner class ComboBoxTests : TestImpl<String>({ ComboBox("label", "foo", "bar") }, "foo")
    @Nested inner class MultiSelectComboBoxTests : TestImpl<Set<String>>({ MultiSelectComboBox("label", "foo", "bar") }, setOf("foo"))
    @Nested inner class CheckboxGroupTests : TestImpl<Set<String>>({ CheckboxGroup("label", "foo", "bar") }, setOf("foo"))
    @Nested inner class RadioButtonGroupTests : TestImpl<String>({ RadioButtonGroup("label", "foo", "bar") }, "foo")
    @Nested inner class SelectTests : TestImpl<String>({ Select() }, "foo")
    @Nested inner class TimePickerTests : TestImpl<LocalTime>({ TimePicker() }, LocalTime.NOON)
    @Nested inner class MyCustomFieldTests : TestImpl<String>({ MyCustomField() }, "foo")
    // @todo mavi CustomField, AbstractField, all vaadin-core HasValues
    // @todo mavi emulate maybe how client-side sets value, by calling AbstractField.setModelValue()
}

class MyCustomField : CustomField<String>() {
    private val tf = TextField()
    override fun generateModelValue(): String? = tf.value
    override fun setPresentationValue(newPresentationValue: String?) {
        tf.value = newPresentationValue
    }
}
