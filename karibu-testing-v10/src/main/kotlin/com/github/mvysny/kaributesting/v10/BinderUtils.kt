package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributools.caption
import com.github.mvysny.kaributools.label
import com.vaadin.flow.component.Component
import com.vaadin.flow.data.binder.*
import kotlin.test.expect

/**
 * Formats the [BinderValidationStatus.getFieldValidationErrors] and [BinderValidationStatus.getBeanValidationErrors]
 * in a nice way: shows the component caption (and ID if it's set), the value and the validation message.
 *
 * Workaround for https://github.com/vaadin/flow/issues/7081
 */
public val ValidationException.verboseMessage: String get() =
    "${message}: ${getVerboseMessage(fieldValidationErrors, beanValidationErrors)}"

private fun getVerboseMessage(
    fieldValidationErrors: List<BindingValidationStatus<*>>,
    beanValidationErrors: List<ValidationResult>
): String {
    val fieldValidationErrorsString: String = fieldValidationErrors.joinToString(", ") {
        val component = it.field as Component
        var componentCaption: String = component.label.ifBlank { component._text } ?: ""
        if (!component.id_.isNullOrBlank()) {
            componentCaption = "$componentCaption(#${component.id_})"
        }
        "$componentCaption: ${it.status} ${it.message.orElse("")}, value='${it.field.value}'"
    }
    val beanValidationErrorsString: String = beanValidationErrors.joinToString(", ") {
        "${it.errorLevel.orElse(null)}: ${it.errorMessage}"
    }
    return "field validation errors: [$fieldValidationErrorsString], bean validation errors: [$beanValidationErrorsString]"
}

/**
 * Formats the [BinderValidationStatus.getFieldValidationErrors] and [BinderValidationStatus.getBeanValidationErrors]
 * in a nice way: shows the component caption (and ID if it's set), the value and the validation message.
 *
 * Workaround for https://github.com/vaadin/flow/issues/7081
 */
public val BinderValidationStatus<*>.verboseMessage: String get() =
    getVerboseMessage(fieldValidationErrors, beanValidationErrors)

/**
 * Fails if this status is not valid ([isOk][BinderValidationStatus.isOk] returns false).
 */
public fun BinderValidationStatus<*>._expectValid() {
    expect(true, verboseMessage) { isOk }
}

/**
 * Fails if this binder is not valid.
 */
public fun Binder<*>._expectValid() {
    validate()._expectValid()
}
