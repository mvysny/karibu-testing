package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeDsl
import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.karibudsl.v10.beanValidationBinder
import com.github.mvysny.karibudsl.v10.bind
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.binder.ValidationException
import javax.validation.constraints.NotNull
import kotlin.test.expect
import kotlin.test.fail

private class Person(
        @field:NotNull var name: String? = null
)

@DynaNodeDsl
internal fun DynaNodeGroup.binderTestbatch() {
    test("verbose message") {
        val binder: Binder<Person> = beanValidationBinder<Person>()
        TextField("blank").apply {
            bind(binder).bind(Person::name)
        }
        try {
            binder.writeBean(Person())
            fail("should have failed")
        } catch (e: ValidationException) {
            expect("Validation has failed for some fields: field validation errors: [blank: ERROR must not be null, value=''], bean validation errors: []") {
                e.verboseMessage
            }
        }
    }
}