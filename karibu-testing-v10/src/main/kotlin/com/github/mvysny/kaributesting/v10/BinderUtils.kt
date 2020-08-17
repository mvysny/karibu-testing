package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.Component
import com.vaadin.flow.data.binder.ValidationException

/**
 * Workaround for https://github.com/vaadin/flow/issues/7081
 */
public val ValidationException.verboseMessage: String get() {
    val fieldValidationErrors: String = fieldValidationErrors.joinToString(", ") {
        val componentCaption: String = (it.field as Component).caption
        "$componentCaption: ${it.status} ${it.message.orElse("")}, value='${it.field.getValue()}'"
    }
    val beanValidationErrors: String = beanValidationErrors.joinToString(", ") {
        "${it.errorLevel.orElse(null)}: ${it.errorMessage}"
    }
    return "${message}: field validation errors: [$fieldValidationErrors], bean validation errors: [$beanValidationErrors]"
}
