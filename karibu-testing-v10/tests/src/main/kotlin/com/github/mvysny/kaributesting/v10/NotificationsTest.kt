package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.notification.Notification
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

abstract class AbstractNotificationsTests {
    @Nested inner class Simple : NotificationsTestsImpl(false)

    /**
     * the server-side modality curtain introduced in Vaadin 23 will cause notifications
     * to be nested within the modal dialog itself. We therefore need to test notifications
     * also in the presence of a modal dialog
     */
    @Nested inner class WithModalDialogs : NotificationsTestsImpl(true)
}

abstract class NotificationsTestsImpl(val withModalDialog: Boolean) {
    @BeforeEach fun fakeVaadin() {
        MockVaadin.setup()
        if (withModalDialog) {
            val dlg = Dialog()
            dlg.isModal = true
            dlg.open()
        }
    }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Test fun `initially there are no notifications`() {
        expectNoNotifications()
    }

    @Test fun `expectNotifications() fails if there are expected notifications but no actual notifications`() {
        expectThrows(AssertionError::class, "Notifications: Expected [Error, Warning, baz] but there are no notifications. Dump:") {
            expectNotifications("Error", "Warning", "baz")
        }
    }

    @Test fun `expectNotifications() fails if no notifications are expected but there are some`() {
        Notification.show("Foo")
        expectThrows(AssertionError::class, "Notifications: expected [] but got [Foo]. Dump:") {
            expectNoNotifications()
        }
    }

    @Test fun `expectNotifications() fails if notifications don't match expected value`() {
        Notification.show("Foo")
        expectThrows(AssertionError::class, "Notifications: expected [Bar, Baz] but got [Foo]. Dump:") {
            expectNotifications("Bar", "Baz")
        }
    }

    @Test fun `expectNotifications() fails if notifications don't match expected value 2`() {
        Notification.show("Bar")
        expectThrows(AssertionError::class, "Notifications: expected [Bar, Baz] but got [Bar]. Dump:") {
            expectNotifications("Bar", "Baz")
        }
    }

    @Test fun `assert on shown notifications`() {
        Notification.show("Error")
        expectNotifications("Error")
        // expectNotifications also clears current notifications so that any further notifications won't be mixed with existing ones
        expectNoNotifications()
    }

    @Test fun `clear notifications`() {
        Notification.show("Given user can not be found")
        clearNotifications()
        expectNoNotifications()
    }
}
