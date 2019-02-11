package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.ClickEvent
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.contextmenu.ContextMenu
import com.vaadin.flow.component.contextmenu.MenuItem
import kotlin.test.expect
import kotlin.test.fail

/**
 * Tries to find a menu item with given caption and click it.
 * @throws AssertionError if no such menu item exists, or the menu item is not enabled or visible, or it's nested in
 * a menu item which is invisible or disabled, or it's attached to a component that's invisible.
 */
fun ContextMenu._clickItemWithCaption(caption: String) {
    val parentMap = getParentMap()
    val item = parentMap.keys.firstOrNull { it.text == caption } ?: fail("No menu item with caption $caption in ${toPrettyTree()}")
    item._click()
}

private fun ContextMenu.getParentMap(): Map<MenuItem, Component> {
    val result = mutableMapOf<MenuItem, Component>()

    fun fillInParentFor(item: MenuItem, parent: Component) {
        result[item] = parent
        item.subMenu.items.forEach { fillInParentFor(it, item) }
    }

    items.forEach { fillInParentFor(it, this) }
    return result
}

/**
 * Tries to click given menu item.
 * @throws AssertionError if no such menu item exists, or the menu item is not enabled or visible, or it's nested in
 * a menu item which is invisible or disabled, or it's attached to a component that's invisible.
 */
private fun MenuItem._click() {
    val parentMap = contextMenu.getParentMap()
    checkMenuItemVisible(this, parentMap)
    checkMenuItemEnabled(this, parentMap)
    _fireEvent(ClickEvent<MenuItem>(this, true, 0, 0, 0, 0, 1, 1, false, false, false, false))
}

private fun MenuItem.checkMenuItemVisible(originalItem: MenuItem, parentMap: Map<MenuItem, Component>) {
    if (!isVisible) {
        if (originalItem == this) {
            fail("${originalItem.toPrettyString()} is not visible")
        } else {
            fail("${originalItem.toPrettyString()} is not visible because its parent item is not visible:\n${toPrettyTree()}")
        }
    }
    val parent = parentMap[this] ?: fail("${originalItem.toPrettyString()} is not part of\n${contextMenu.toPrettyTree()}?!?")
    when(parent) {
        is MenuItem -> parent.checkMenuItemVisible(originalItem, parentMap)
        is ContextMenu -> expect(true, "Cannot click ${originalItem.toPrettyString()} since it's attached to ${parent.target.toPrettyString()} which is not effectively visible") {
            parent.target.isEffectivelyVisible()
        }
        else -> fail("Unexpected parent ${parent.toPrettyString()}")
    }
}

private fun MenuItem.checkMenuItemEnabled(originalItem: MenuItem, parentMap: Map<MenuItem, Component>) {
    if (!isEnabled) {
        if (originalItem == this) {
            fail("${originalItem.toPrettyString()} is not enabled")
        } else {
            fail("${originalItem.toPrettyString()} is not enabled because its parent item is not enabled:\n${toPrettyTree()}")
        }
    }
    val parent = parentMap[this] ?: fail("${originalItem.toPrettyString()} is not part of\n${contextMenu.toPrettyTree()}?!?")
    when(parent) {
        is MenuItem -> parent.checkMenuItemEnabled(originalItem, parentMap)
        is ContextMenu -> Unit
        else -> fail("Unexpected parent ${parent.toPrettyString()}")
    }
}

