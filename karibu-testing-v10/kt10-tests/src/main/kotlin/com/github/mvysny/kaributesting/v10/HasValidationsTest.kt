package com.github.mvysny.kaributesting.v10

import com.github.mvysny.karibudsl.v10.comboBox
import com.github.mvysny.karibudsl.v10.textField
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.textfield.TextField
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

abstract class AbstractHasValidationTests() {
    @Nested inner class _expectValid {
        @Test fun `passes on valid`() {
            ComboBox<String>()._expectValid()
            TextField("Foo")._expectValid()
        }
        @Test fun `fails on invalid`() {
            expectThrows<AssertionError>("TextField[label='Foo', value='', INVALID]: expected to be valid") {
                TextField("Foo").apply { isInvalid = true } ._expectValid()
            }
        }
    }
    @Nested inner class _expectAllFieldsValid {
        @Test fun `passes on valid`() {
            MyForm2()._expectAllFieldsValid()
        }
        @Test fun `fails on invalid`() {
            val form = MyForm2()
            form.tf.isInvalid = true
            expectThrows<AssertionError>("TextField[label='Text Field', value='', INVALID]: expected to be valid") {
                form._expectAllFieldsValid()
            }
        }
    }
    @Nested inner class _expectInvalid {
        @Test fun `passes on invalid`() {
            ComboBox<String>().apply { isInvalid = true }._expectInvalid()
            TextField().apply { isInvalid = true }._expectInvalid()
            TextField().apply { isInvalid = true; errorMessage = "FooBar" }._expectInvalid("FooBar")
        }
        @Test fun `fails on valid`() {
            expectThrows<AssertionError>("TextField[label='Foo', value='']: expected to be invalid") {
                TextField("Foo")._expectInvalid()
            }
        }
    }
}

private class MyForm2 : FormLayout() {
    val tf = textField("Text Field")
    val cb = comboBox<String>("Combo Box")
}
