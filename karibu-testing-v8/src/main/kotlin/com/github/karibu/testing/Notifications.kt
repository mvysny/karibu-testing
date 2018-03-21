package com.github.karibu.testing

import com.vaadin.ui.Notification
import com.vaadin.ui.UI

/**
 * Returns the list of currently displayed notifications.
 */
fun getNotifications(): List<Notification> = UI.getCurrent().extensions.filterIsInstance<Notification>()

/**
 * Expects that given list of notifications is displayed. Also clears the notifications.
 */
fun expectNotifications(vararg descriptions: String) {
    val notifications: List<Notification> = getNotifications()
    expectList(*descriptions) { notifications.map { it.description } }
    clearNotifications()
}

/**
 * Clears and removes all notifications from screen.
 */
fun clearNotifications() {
    getNotifications().forEach { it.remove() }
}
