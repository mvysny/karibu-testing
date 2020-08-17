package com.github.mvysny.kaributesting.v8

import com.vaadin.ui.Notification
import com.vaadin.ui.UI

/**
 * Returns the list of currently displayed notifications.
 */
public fun getNotifications(): List<Notification> = UI.getCurrent().extensions.filterIsInstance<Notification>()

/**
 * Expects that given list of notifications is displayed. Also clears the notifications.
 */
public fun expectNotifications(vararg descriptions: Pair<String, String?>) {
    val notifications: List<Notification> = getNotifications()
    expectList(*descriptions) { notifications.map { it.caption to it.description } }
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
    getNotifications().forEach { it.remove() }
}
