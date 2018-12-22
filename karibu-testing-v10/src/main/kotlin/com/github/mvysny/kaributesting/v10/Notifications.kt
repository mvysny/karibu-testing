package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.dom.Element
import kotlin.streams.toList

/**
 * Returns the list of currently displayed notifications.
 */
fun getNotifications(): List<Notification> {
    // notifications are opened lazily. Make sure they are attached to the UI
    testingLifecycleHook.awaitBeforeLookup()
    val notifications = UI.getCurrent().children.toList().filterIsInstance<Notification>()
    testingLifecycleHook.awaitAfterLookup()
    return notifications
}

/**
 * Returns the notification text.
 */
fun Notification.getText(): String {
    val f = Notification::class.java.getDeclaredField("templateElement").apply { isAccessible = true }
    val e = f.get(this) as Element
    return e.getProperty("innerHTML") ?: ""
}

/**
 * Expects that given list of notifications is displayed. Also clears the notifications.
 */
fun expectNotifications(vararg texts: String) {
    val notifications: List<Notification> = getNotifications()
    expectList(*texts) { notifications.map { it.getText() } }
    clearNotifications()
}

/**
 * Clears and removes all notifications from screen.
 */
fun clearNotifications() {
    getNotifications().forEach { it.removeFromParent() }
}
