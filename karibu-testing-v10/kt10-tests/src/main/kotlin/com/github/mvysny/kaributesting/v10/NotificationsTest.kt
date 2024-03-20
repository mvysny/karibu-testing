package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.dynatest.expectThrows
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.notification.Notification

@DynaTestDsl
internal fun DynaNodeGroup.notificationsTestBattery() {
    group("simple") {
        notificationsTests()
    }

    group("with modal dialogs") {
        // the server-side modality curtain introduced in Vaadin 23 will cause notifications
        // to be nested within the modal dialog itself. We therefore need to test notifications
        // also in the presence of a modal dialog
        notificationsTests()

        // this needs to go after notificationsTests() since notificationsTests()/beforeEach{} will setup MockVaadin for us.
        beforeEach {
            val dlg = Dialog()
            dlg.isModal = true
            dlg.open()
        }
    }
}

@DynaTestDsl
private fun DynaNodeGroup.notificationsTests() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("initially there are no notifications") {
        expectNoNotifications()
    }

    test("expectNotifications() fails if there are expected notifications but no actual notifications") {
        expectThrows(AssertionError::class, "Notifications: Expected [Error, Warning, baz] but there are no notifications. Dump:") {
            expectNotifications("Error", "Warning", "baz")
        }
    }

    test("expectNotifications() fails if no notifications are expected but there are some") {
        Notification.show("Foo")
        expectThrows(AssertionError::class, "Notifications: expected [] but got [Foo]. Dump:") {
            expectNoNotifications()
        }
    }

    test("expectNotifications() fails if notifications don't match expected value") {
        Notification.show("Foo")
        expectThrows(AssertionError::class, "Notifications: expected [Bar, Baz] but got [Foo]. Dump:") {
            expectNotifications("Bar", "Baz")
        }
    }

    test("expectNotifications() fails if notifications don't match expected value 2") {
        Notification.show("Bar")
        expectThrows(AssertionError::class, "Notifications: expected [Bar, Baz] but got [Bar]. Dump:") {
            expectNotifications("Bar", "Baz")
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
