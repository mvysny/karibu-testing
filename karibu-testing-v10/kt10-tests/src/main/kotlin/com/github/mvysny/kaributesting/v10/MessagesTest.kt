package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.messages.MessageInput
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect

abstract class AbstractMessageTests() {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Nested inner class MessageInputTests {
        @Test fun `submit event`() {
            lateinit var msg: String
            val messageInput = MessageInput()
            messageInput.addSubmitListener { msg = it.value }
            messageInput._submit("Hello")
            expect("Hello") { msg }
        }
    }
}
