package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.beanValidationBinder
import com.github.mvysny.karibudsl.v10.bind
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.binder.ValidationException
import javax.validation.constraints.NotNull
import kotlin.test.expect

private class Person(
        @field:NotNull var name: String? = null
)

@DynaTestDsl
internal fun DynaNodeGroup.binderTestbatch() {
    test("ValidationException.verboseMessage") {
        val binder: Binder<Person> = beanValidationBinder<Person>()
        TextField("Name:").apply {
            bind(binder).bind(Person::name)
        }
        val e = expectThrows<ValidationException> { binder.writeBean(Person()) }
        expect("Validation has failed for some fields: field validation errors: [Name:: ERROR must not be null, value=''], bean validation errors: []") {
            e.verboseMessage
        }
    }
    test("BinderValidationStatus.verboseMessage") {
        val binder: Binder<Person> = beanValidationBinder<Person>()
        TextField("Name:").apply {
            bind(binder).bind(Person::name)
        }
        expect("field validation errors: [Name:: ERROR must not be null, value=''], bean validation errors: []") {
            binder.validate().verboseMessage
        }
    }
    test("BinderValidationStatus.expectValid()") {
        val binder: Binder<Person> = beanValidationBinder<Person>()
        TextField("Name:").apply {
            bind(binder).bind(Person::name)
        }
        expectThrows<AssertionError>("field validation errors: [Name:: ERROR must not be null, value=''], bean validation errors: []") {
            binder.validate()._expectValid()
        }
    }
    test("Binder.expectValid()") {
        val binder: Binder<Person> = beanValidationBinder<Person>()
        TextField("Name:").apply {
            bind(binder).bind(Person::name)
        }
        expectThrows<AssertionError>("field validation errors: [Name:: ERROR must not be null, value=''], bean validation errors: []") {
            binder._expectValid()
        }
    }
}
