package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasValidation
import kotlin.test.fail

/**
 * Asserts that this component is valid. Fails if the component is invalid.
 */
public fun HasValidation._expectValid() {
    if (isInvalid) {
        fail("${(this as Component).toPrettyString()}: expected to be valid")
    }
}

/**
 * Asserts that all descendant visible components are valid.
 */
public fun Component._expectAllFieldsValid() {
    val hasValidations = _find<Component>().filterIsInstance<HasValidation>()
    hasValidations.onEach { it._expectValid() }
}

/**
 * Asserts that this component is invalid. Optionally also checks the [expectedErrorMessage].
 *
 * Useful when testing one particular field in form for invalid values.
 */
@JvmOverloads
public fun HasValidation._expectInvalid(expectedErrorMessage: String = "") {
    if (!isInvalid) {
        fail("${(this as Component).toPrettyString()}: expected to be invalid")
    }
    if (!(errorMessage ?: "").contains(expectedErrorMessage)) {
        fail("${(this as Component).toPrettyString()}: expected error message '$expectedErrorMessage' got '$errorMessage'")
    }
}
