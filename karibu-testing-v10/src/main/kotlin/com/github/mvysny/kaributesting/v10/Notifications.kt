package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributools.getText
import com.github.mvysny.kaributools.removeFromParent
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.notification.Notification
import kotlin.streams.toList

/**
 * Returns the list of currently displayed notifications.
 */
public fun getNotifications(): List<Notification> {
    // notifications are opened lazily. Make sure they are attached to the UI
    testingLifecycleHook.awaitBeforeLookup()
    val notifications = UI.getCurrent().children.toList().filterIsInstance<Notification>()
    testingLifecycleHook.awaitAfterLookup()
    return notifications
}

/**
 * Expects that given list of notifications is displayed. Also clears the notifications.
 */
public fun expectNotifications(vararg texts: String) {
    val notifications: List<Notification> = getNotifications()
    expectList(*texts) { notifications.map { it.getText() } }
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
    getNotifications().forEach { it.removeFromParent() }
}
