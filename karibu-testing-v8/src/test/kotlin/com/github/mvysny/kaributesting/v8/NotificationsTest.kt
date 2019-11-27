package com.github.mvysny.kaributesting.v8

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.vaadin.ui.Notification

class NotificationsTest : DynaTest({
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("initially there are no notifications") {
        expectNoNotifications()
    }

    test("expectNotifications() fails if there are expected notifications but no actual notifications") {
        expectThrows(AssertionError::class) {
            expectNotifications("Error" to null, "Warning" to "This won't work!", "baz" to null)
        }
    }

    test("assert on shown notifications") {
        Notification.show("Error", "Given user can not be found", Notification.Type.ERROR_MESSAGE)
        expectNotifications("Error" to "Given user can not be found")
        // it also clears the notifications, so there should be no more notifications
        expectNoNotifications()
    }

    test("get notifications") {
        expectList() { getNotifications() }
        Notification.show("Error", "Given user can not be found", Notification.Type.ERROR_MESSAGE)
        expectList("Given user can not be found") { getNotifications().map { it.description } }
    }

    test("clear notifications") {
        Notification.show("Error", "Given user can not be found", Notification.Type.ERROR_MESSAGE)
        clearNotifications()
        expectNoNotifications()
    }
})
