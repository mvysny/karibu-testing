package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributools.caption
import com.vaadin.flow.component.Component
import com.vaadin.flow.data.binder.*
import kotlin.test.expect

/**
 * Workaround for https://github.com/vaadin/flow/issues/7081
 */
public val ValidationException.verboseMessage: String get() =
    "${message}: ${getVerboseMessage(fieldValidationErrors, beanValidationErrors)}"

private fun getVerboseMessage(
    fieldValidationErrors: List<BindingValidationStatus<*>>,
    beanValidationErrors: List<ValidationResult>
): String {
    val fieldValidationErrorsString: String = fieldValidationErrors.joinToString(", ") {
        val componentCaption: String = (it.field as Component).caption
        "$componentCaption: ${it.status} ${it.message.orElse("")}, value='${it.field.getValue()}'"
    }
    val beanValidationErrorsString: String = beanValidationErrors.joinToString(", ") {
        "${it.errorLevel.orElse(null)}: ${it.errorMessage}"
    }
    return "field validation errors: [$fieldValidationErrorsString], bean validation errors: [$beanValidationErrorsString]"
}

public val BinderValidationStatus<*>.verboseMessage: String get() =
    getVerboseMessage(fieldValidationErrors, beanValidationErrors)

public fun BinderValidationStatus<*>.expectValid() {
    expect(true, verboseMessage) { isOk }
}

public fun Binder<*>.expectValid() {
    validate().expectValid()
}
