package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.messages.MessageInput

/**
 * Fires the [MessageInput.SubmitEvent] event.
 */
public fun MessageInput._submit(message: String) {
    checkEditableByUser()
    _fireEvent(MessageInput.SubmitEvent(this, true, message))
}
