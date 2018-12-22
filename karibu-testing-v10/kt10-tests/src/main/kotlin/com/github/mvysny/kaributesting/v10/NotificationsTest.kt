package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.expectThrows
import com.vaadin.flow.component.notification.Notification

internal fun DynaNodeGroup.notificationsTestBattery() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("initially there are no notifications") {
        expectNotifications()
    }

    test("expectNotifications() fails if there are expected notifications but no actual notifications") {
        expectThrows(AssertionError::class) {
            expectNotifications("Error", "Warning", "baz")
        }
    }

    test("assert on shown notifications") {
        Notification.show("Error")
        expectNotifications("Error")
        // it also clears the notifications, so there should be no more notifications
        expectNotifications()
    }

    test("clear notifications") {
        Notification.show("Given user can not be found")
        clearNotifications()
        expectNotifications()
    }
}
