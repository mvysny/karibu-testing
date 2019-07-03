package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.contextmenu.MenuItem
import com.vaadin.flow.component.contextmenu.MenuItemBase
import com.vaadin.flow.component.menubar.MenuBar
import kotlin.test.fail

private fun MenuBar.getParentMap(): Map<MenuItemBase<*, *, *>, Component> {
    val result: MutableMap<MenuItemBase<*, *, *>, Component> = mutableMapOf<MenuItemBase<*, *, *>, Component>()

    fun fillInParentFor(item: MenuItemBase<*, *, *>, parent: Component) {
        result[item] = parent
        item.getSubMenu().getItems().forEach { fillInParentFor(it, item) }
    }

    items.forEach { fillInParentFor(it, this) }
    return result
}

/**
 * Tries to find a menu item with given caption and click it.
 * @throws AssertionError if no such menu item exists, or the menu item is not enabled or visible, or it's nested in
 * a menu item which is invisible or disabled, or it's attached to a component that's invisible.
 */
fun MenuBar._clickItemWithCaption(caption: String) {
    val parentMap: Map<MenuItemBase<*, *, *>, Component> = getParentMap()
    val item: MenuItemBase<*, *, *> = parentMap.keys.firstOrNull { it.getText() == caption } ?: fail("No menu item with caption $caption in ContextMenu:\n${toPrettyTree()}")
    (item as MenuItem)._click(parentMap)
}
