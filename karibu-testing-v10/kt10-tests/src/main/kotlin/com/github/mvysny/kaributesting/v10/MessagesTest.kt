package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.vaadin.flow.component.messages.MessageInput
import kotlin.test.expect

@DynaTestDsl
internal fun DynaNodeGroup.messageTests() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    group("MessageInput") {
        test("submit event") {
            lateinit var msg: String
            val messageInput = MessageInput()
            messageInput.addSubmitListener { msg = it.value }
            messageInput._submit("Hello")
            expect("Hello") { msg }
        }
    }
}
