package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.dynatest.expectThrows
import com.vaadin.flow.component.notification.Notification

@DynaTestDsl
internal fun DynaNodeGroup.notificationsTestBattery() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("initially there are no notifications") {
        expectNoNotifications()
    }

    test("expectNotifications() fails if there are expected notifications but no actual notifications") {
        expectThrows(AssertionError::class) {
            expectNotifications("Error", "Warning", "baz")
        }
    }

    test("assert on shown notifications") {
        Notification.show("Error")
        expectNotifications("Error")
        // expectNotifications also clears current notifications so that any further notifications won't be mixed with existing ones
        expectNoNotifications()
    }

    test("clear notifications") {
        Notification.show("Given user can not be found")
        clearNotifications()
        expectNoNotifications()
    }
}
