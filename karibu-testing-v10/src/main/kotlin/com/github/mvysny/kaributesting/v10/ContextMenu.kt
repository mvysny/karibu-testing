package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.ClickEvent
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
    val menuItem = items.findWithCaption(caption) ?: fail("No menu item with caption $caption in ${toPrettyTree()}")
    menuItem._click()
}

/**
 * Tries to click given menu item.
 * @throws AssertionError if no such menu item exists, or the menu item is not enabled or visible, or it's nested in
 * a menu item which is invisible or disabled, or it's attached to a component that's invisible.
 */
fun MenuItem._click() {
    checkMenuItemVisible(this)
    check(isMenuItemEnabled) { "${toPrettyString()} is not enabled" }
    _fireEvent(ClickEvent<MenuItem>(this, true, 0, 0, 0, 0, 1, 1, false, false, false, false))
}

private fun MenuItem.checkMenuItemVisible(originalItem: MenuItem) {
    expect(true, "${toPrettyString()} is not visible") { isVisible }
    if (!parent.isPresent) return
    val p = parent.get()
    when(p) {
        is MenuItem -> p.checkMenuItemVisible(originalItem)
        is ContextMenu -> expect(true, "Cannot click ${originalItem.toPrettyString()} since it's attached to ${p.target.toPrettyString()} which is not effectively visible") {
            p.target.isEffectivelyVisible()
        }
        else -> fail("Unexpected parent ${p.toPrettyTree()}")
    }
}

private val MenuItem.isMenuItemEnabled: Boolean
    get() = isEnabled // @todo

private fun List<MenuItem>.findWithCaption(caption: String): MenuItem? {
    for (menuItem in this) {
        if (menuItem.text == caption) {
            return menuItem
        }
        val subMenuWithCaption = menuItem.subMenu.items.findWithCaption(caption)
        if (subMenuWithCaption != null) {
            return subMenuWithCaption
        }
    }
    return null
}
