package com.github.mvysny.kaributesting.v8

import com.vaadin.ui.Notification
import com.vaadin.ui.UI

/**
 * Returns the list of currently displayed notifications.
 */
fun getNotifications(): List<Notification> = UI.getCurrent().extensions.filterIsInstance<Notification>()

/**
 * Expects that given list of notifications is displayed. Also clears the notifications.
 */
fun expectNotifications(vararg descriptions: Pair<String, String?>) {
    val notifications: List<Notification> = getNotifications()
    expectList(*descriptions) { notifications.map { it.caption to it.description } }
    clearNotifications()
}

/**
 * Clears and removes all notifications from screen.
 */
fun clearNotifications() {
    getNotifications().forEach { it.remove() }
}
