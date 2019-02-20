package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.button.Button

/**
 * Clicks the button, but only if it is actually possible to do so by the user. If the button is read-only or disabled, it throws an exception.
 * @throws IllegalArgumentException if the button was not visible, not enabled, read-only. See [checkEditableByUser] for
 * more details.
 */
fun Button._click() {
    checkEditableByUser()
    click()  // this doesn't work on Vaadin 12 but it works properly with Vaadin 13
}
