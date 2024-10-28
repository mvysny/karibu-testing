package com.github.mvysny.kaributesting.v10

import com.github.mvysny.karibudsl.v10.beanValidationBinder
import com.github.mvysny.karibudsl.v10.bind
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.binder.ValidationException
import jakarta.validation.constraints.NotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.expect

class Person(
        @field:NotNull var name: String? = null
)

abstract class AbstractBinderTests {
    @Test fun `ValidationException-verboseMessage`() {
        val binder: Binder<Person> = beanValidationBinder<Person>()
        TextField("Name:").apply {
            bind(binder).bind(Person::name)
        }
        val e = assertThrows<ValidationException> { binder.writeBean(Person()) }
        expect("Validation has failed for some fields: field validation errors: [Name:: ERROR must not be null, value=''], bean validation errors: []") {
            e.verboseMessage
        }
    }
    @Test fun `BinderValidationStatus-verboseMessage`() {
        val binder: Binder<Person> = beanValidationBinder<Person>()
        TextField("Name").apply {
            bind(binder).bind(Person::name)
        }
        expect("field validation errors: [Name: ERROR must not be null, value=''], bean validation errors: []") {
            binder.validate().verboseMessage
        }
    }
    @Test fun `BinderValidationStatus-expectValid`() {
        val binder: Binder<Person> = beanValidationBinder<Person>()
        TextField("Name").apply {
            bind(binder).bind(Person::name)
        }
        expectThrows<AssertionError>("The binder was expected to be valid but is invalid: field validation errors: [Name: ERROR must not be null, value=''], bean validation errors: []") {
            binder.validate()._expectValid()
        }
    }
    @Test fun `Binder-expectValid`() {
        val binder: Binder<Person> = beanValidationBinder<Person>()
        TextField("Name").apply {
            bind(binder).bind(Person::name)
        }
        expectThrows<AssertionError>("field validation errors: [Name: ERROR must not be null, value=''], bean validation errors: []") {
            binder._expectValid()
        }
    }
    @Test fun `Binder-_expectInvalid()`() {
        val binder: Binder<Person> = beanValidationBinder<Person>()
        val tf = TextField("Name").apply {
            bind(binder).bind(Person::name)
        }
        binder._expectInvalid()
        binder._expectInvalid("Name: ERROR must not be null, value=''")
        expectThrows<AssertionError>("Expected 'foo' but got 'field validation errors: [Name: ERROR must not be null, value=''], bean validation errors: []") {
            binder._expectInvalid("foo")
        }

        // set tf to a valid value and test
        tf._value = "foo"
        expectThrows<AssertionError>("The binder was expected to be invalid but is valid: field validation errors: [], bean validation errors: []. Expected <false>, actual <true>.") {
            binder._expectInvalid()
        }
    }
}
