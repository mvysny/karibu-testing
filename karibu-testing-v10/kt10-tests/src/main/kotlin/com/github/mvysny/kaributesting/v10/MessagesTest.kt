package com.github.mvysny.kaributesting.v10

import com.github.mvysny.karibudsl.v10.messageList
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.messages.MessageInput
import com.vaadin.flow.component.messages.MessageListItem
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect

abstract class AbstractMessageTests() {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Nested inner class `message list` {
        @Test fun smoke() {
            UI.getCurrent().messageList {}
        }

        @Test fun smoke2() {
            val ml = UI.getCurrent().messageList {}
            ml.isMarkdown = true
            ml.addItem(MessageListItem("# Hello!"))
        }
    }

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
