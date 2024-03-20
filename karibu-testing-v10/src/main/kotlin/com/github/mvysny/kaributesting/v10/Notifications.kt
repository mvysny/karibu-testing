package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributools.*
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.notification.Notification
import kotlin.test.fail

/**
 * Returns the list of currently displayed notifications.
 */
public fun getNotifications(): List<Notification> {
    // Notifications attach themselves directly amongst the children of the UI. However,
    // notifications are opened lazily; make sure to run the runExecutionsBeforeClientResponse()
    // in order for the Notifications to actually add themselves to the UI.
    testingLifecycleHook.awaitBeforeLookup()

    // Vaadin 23: It's not enough to only consider the children of UI.getCurrent().
    // The server-side modality curtain introduced in Vaadin 23 will cause notifications
    // to be nested within the modal dialog itself.
    val possibleNotificationParents = UI.getCurrent().walk()
        .filter { it is UI || (it is Dialog && it.isModal) }
    val notifications = possibleNotificationParents
        .flatMap { it.children.toList() }
        .filterIsInstance<Notification>()
        .filter { it.isOpened }

    testingLifecycleHook.awaitAfterLookup()
    return notifications
}

/**
 * Expects that given list of notifications is displayed. Also clears the notifications.
 */
public fun expectNotifications(vararg expected: String) {
    val notifications: List<Notification> = getNotifications()
    val actual = notifications.map { it.getText() }
    if (actual.isEmpty()) {
        if (expected.isNotEmpty()) {
            fail("Notifications: Expected ${expected.toList()} but there are no notifications. Dump:\n${UI.getCurrent().toPrettyTree()}")
        }
    } else {
        if (actual != expected.toList()) {
            fail("Notifications: expected ${expected.toList()} but got $actual. Dump:\n${UI.getCurrent().toPrettyTree()}")
        }
    }
    clearNotifications()
}

/**
 * Expects that there are no notifications displayed.
 */
public fun expectNoNotifications() {
    expectNotifications()
}

/**
 * Clears and removes all notifications from screen.
 */
public fun clearNotifications() {
    getNotifications().forEach { it._close() }
}

/**
 * Closes the notification and cleans it up properly.
 */
public fun Notification._close() {
    close()
}
