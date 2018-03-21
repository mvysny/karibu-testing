package com.github.karibu.testing

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.vaadin.ui.Notification

class NotificationsTest : DynaTest({
    beforeEach { MockVaadin.setup() }

    test("initially there are no notifications") {
        expectNotifications()
    }

    test("expectNotifications() fails if there are expected notifications but no actual notifications") {
        expectThrows(AssertionError::class) {
            expectNotifications("foo", "bar", "baz")
        }
    }

    test("assert on shown notifications") {
        Notification.show("Error", "Given user can not be found", Notification.Type.ERROR_MESSAGE)
        expectNotifications("Given user can not be found")
        // it also clears the notifications, so there should be no more notifications
        expectNotifications()
    }

    test("clear notifications") {
        Notification.show("Error", "Given user can not be found", Notification.Type.ERROR_MESSAGE)
        clearNotifications()
        expectNotifications()
    }
})
