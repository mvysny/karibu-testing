@file:Suppress("FunctionName")

package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.ClickEvent
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.contextmenu.*
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem
import java.lang.reflect.Method
import kotlin.test.expect
import kotlin.test.fail

/**
 * Tries to find a menu item matching given spec and click it.
 * @throws AssertionError if no such menu item exists, or the menu item is not enabled or visible, or it's nested in
 * a menu item which is invisible or disabled, or it's attached to a component that's invisible.
 */
public fun HasMenuItems._clickItemMatching(searchSpec: SearchSpec<MenuItemBase<*, *, *>>) {
    // fires ContextMenuOpenedListener to simulate menu opening
    (this as Component).element.setProperty("opened", true)

    val parentMap: Map<MenuItemBase<*, *, *>, Component> = (this as Component).getParentMap()
    val predicate = searchSpec.toPredicate()
    val item: MenuItemBase<*, *, *> = parentMap.keys.firstOrNull(predicate)
            ?: fail("No menu item with ${searchSpec.toString().removePrefix("MenuItemBase and ")} in this menu:\n${(this as Component).toPrettyTree()}")
    (item as MenuItem)._click(parentMap)

    // fires ContextMenuOpenedListener to simulate menu closing
    (this as Component).element.setProperty("opened", false)
}

/**
 * Tries to find a menu item with given caption and click it.
 * @throws AssertionError if no such menu item exists, or the menu item is not enabled or visible, or it's nested in
 * a menu item which is invisible or disabled, or it's attached to a component that's invisible.
 */
public fun HasMenuItems._clickItemWithCaption(caption: String) {
    _clickItemMatching(SearchSpec(MenuItemBase::class.java, text = caption))
}

/**
 * Tries to find a menu item with given [id][Component.setId] and click it.
 * @throws AssertionError if no such menu item exists, or the menu item is not enabled or visible, or it's nested in
 * a menu item which is invisible or disabled, or it's attached to a component that's invisible.
 */
public fun HasMenuItems._clickItemWithID(id: String) {
    _clickItemMatching(SearchSpec(MenuItemBase::class.java, id = id))
}

/**
 * Clicks a menu [item]. The item must belong to this menu.
 *
 * Intended to be used with MenuBars. See [Issue 33](https://github.com/mvysny/karibu-testing/issues/33) for more details.
 * @throws AssertionError if no such menu item exists, or the menu item is not enabled or visible, or it's nested in
 * a menu item which is invisible or disabled, or it's attached to a component that's invisible.
 */
public fun HasMenuItems._click(item: MenuItem) {
    val parentMap: Map<MenuItemBase<*, *, *>, Component> = (this as Component).getParentMap()
    if (!parentMap.keys.contains(item)) {
        fail("${item.toPrettyString()} is not contained in this menu:\n${(this as Component).toPrettyTree()}")
    }
    item._click(parentMap)
}

/**
 * @receiver can be of type [HasMenuItems] or [GridContextMenu].
 */
private fun Component.getItems(): List<MenuItemBase<*, *, *>> {
    return when(this) {
        is ContextMenuBase<*, *, *> -> getItems()
        is SubMenuBase<*, *, *> -> getItems()
        else -> {
            // every HasMenuItems implementor has the getItems() method including the MenuBar.
            // can't use the MenuBar type directly though, to keep compatibility with Vaadin 13
            val method: Method = this.javaClass.getMethod("getItems")
            @Suppress("UNCHECKED_CAST")
            method.invoke(this) as List<MenuItemBase<*, *, *>>
        }
    }
}

/**
 * Tries to find a menu item with given caption and click it, passing in given [gridItem].
 * @throws AssertionError if no such menu item exists, or the menu item is not enabled or visible, or it's nested in
 * a menu item which is invisible or disabled, or it's attached to a component that's invisible.
 */
public fun <T> GridContextMenu<T>._clickItemWithCaption(caption: String, gridItem: T?) {
    // fires ContextMenuOpenedListener to simulate menu opening
    _setContextMenuTargetItemKey(gridItem)
    element.setProperty("opened", true)

    // notify the context menu dynamic item generator
    dynamicContentHandler?.also {
        val openMenu: Boolean = it.test(gridItem)
        if (!openMenu) {
            fail("The dynamic content handler returned false signalling the menu should not open:\n${toPrettyTree()}")
        }
    }

    val parentMap: Map<MenuItemBase<*, *, *>, Component> = getParentMap()
    val item: MenuItemBase<*, *, *> = parentMap.keys.firstOrNull { it.getText() == caption }
            ?: fail("No menu item with caption $caption in GridContextMenu:\n${toPrettyTree()}")
    @Suppress("UNCHECKED_CAST")
    (item as GridMenuItem<T>)._click(gridItem)

    // fires ContextMenuOpenedListener to simulate menu closing
    element.setProperty("opened", false)
}

private fun Component.getParentMap(): Map<MenuItemBase<*, *, *>, Component> {
    val result: MutableMap<MenuItemBase<*, *, *>, Component> = mutableMapOf<MenuItemBase<*, *, *>, Component>()

    fun fillInParentFor(item: MenuItemBase<*, *, *>, parent: Component) {
        result[item] = parent
        item.getSubMenu().getItems().forEach { fillInParentFor(it, item) }
    }

    getItems().forEach { fillInParentFor(it, this) }
    return result
}

/**
 * Tries to click given menu item. [MenuItem.isChecked] is toggled if [MenuItem.isCheckable].
 *
 * Fails if no such menu item exists, or the menu item is not enabled or visible, or it's nested in
 * a menu item which is invisible or disabled, or it's attached to a component that's invisible.
 *
 * Doesn't work for MenuItems nested in MenuBar.
 * Use either [HasMenuItems._clickItemWithCaption] or [HasMenuItems._click].
 * See [Issue 33](https://github.com/mvysny/karibu-testing/issues/33) for more details.
 * @throws AssertionError if no such menu item exists, or the menu item is not enabled or visible, or it's nested in
 * a menu item which is invisible or disabled, or it's attached to a component that's invisible.
 */
public fun MenuItem._click() {
    val contextMenu: ContextMenu = contextMenu ?: fail("This function doesn't work on menu items attached to MenuBars. Use either menuBar._clickItemWithCaption(\"foo\") or menuBar._click(menuItem). See https://github.com/mvysny/karibu-testing/issues/33 for more details")
    val parentMap: Map<MenuItemBase<*, *, *>, Component> = contextMenu.getParentMap()
    _click(parentMap)
}

/**
 * Tries to click given menu item. [MenuItem.isChecked] is toggled if [MenuItem.isCheckable].
 * @throws AssertionError if no such menu item exists, or the menu item is not enabled or visible, or it's nested in
 * a menu item which is invisible or disabled, or it's attached to a component that's invisible.
 */
private fun MenuItem._click(parentMap: Map<MenuItemBase<*, *, *>, Component>) {
    checkMenuItemVisible(this, parentMap)
    checkMenuItemEnabled(this, parentMap)
    // toggle the isChecked first, so that the click event receives the most current value.
    // https://github.com/mvysny/karibu-testing/issues/126
    if (isCheckable) {
        isChecked = !isChecked
    }
    _fireEvent(ClickEvent<MenuItem>(this, true, 0, 0, 0, 0, 1, 1, false, false, false, false))
}

/**
 * Tries to click given menu item, passing in given [gridItem].
 * @param gridItem the item which was clicked. `null` when the grid is "right-clicked"
 * outside of any item (e.g. if there are no items shown in the grid).
 * @throws AssertionError if no such menu item exists, or the menu item is not enabled or visible, or it's nested in
 * a menu item which is invisible or disabled, or it's attached to a component that's invisible.
 */
public fun <T> GridMenuItem<T>._click(gridItem: T?) {
    val parentMap: Map<MenuItemBase<*, *, *>, Component> = contextMenu.getParentMap()
    checkMenuItemVisible(this, parentMap)
    checkMenuItemEnabled(this, parentMap)

    contextMenu._setContextMenuTargetItemKey(gridItem)
    _fireDomEvent("click")
}

private fun <T> GridContextMenu<T>._setContextMenuTargetItemKey(gridItem: T?) {
    @Suppress("UNCHECKED_CAST")
    val grid: Grid<T> = target as Grid<T>
    val key: String? = grid.dataCommunicator.keyMapper.key(gridItem)
    requireNotNull(key) { "grid ${grid.toPrettyString()} generated null as key for $gridItem" }
    grid.element.setProperty("_contextMenuTargetItemKey", key)
}

private fun MenuItemBase<*, *, *>.checkMenuItemVisible(originalItem: MenuItemBase<*, *, *>, parentMap: Map<MenuItemBase<*, *, *>, Component>) {
    if (!isVisible()) {
        if (originalItem == this) {
            fail("${originalItem.toPrettyString()} is not visible")
        } else {
            fail("${originalItem.toPrettyString()} is not visible because its parent item is not visible:\n${toPrettyTree()}")
        }
    }
    val parent: Component = parentMap[this]
            ?: fail("${originalItem.toPrettyString()} is not part of\n${getContextMenu().toPrettyTree()}?!?")
    when (parent) {
        is MenuItem -> parent.checkMenuItemVisible(originalItem, parentMap)
        is GridMenuItem<*> -> parent.checkMenuItemVisible(originalItem, parentMap)
        is ContextMenu -> {
            checkNotNull(parent.target) { "The context menu ${parent.toPrettyString()} is not attached to any component" }
            expect(true, "Cannot click ${originalItem.toPrettyString()} since it's attached to ${parent.target.toPrettyString()} which is not effectively visible") {
                parent.target.isEffectivelyVisible()
            }
        }
        is GridContextMenu<*> -> {
            checkNotNull(parent.target) { "The context menu ${parent.toPrettyString()} is not attached to any component" }
            expect(true, "Cannot click ${originalItem.toPrettyString()} since it's attached to ${parent.target.toPrettyString()} which is not effectively visible") {
                parent.target.isEffectivelyVisible()
            }
        }
        // e.g. MenuBar
        else -> expect(true, "Cannot click ${originalItem.toPrettyString()} since it's attached to ${parent.toPrettyString()} which is not effectively visible") {
            parent.isEffectivelyVisible()
        }
    }
}

private fun MenuItemBase<*, *, *>.checkMenuItemEnabled(originalItem: MenuItemBase<*, *, *>, parentMap: Map<MenuItemBase<*, *, *>, Component>) {
    if (!isEnabled) {
        if (originalItem == this) {
            fail("${originalItem.toPrettyString()} is not enabled")
        } else {
            fail("${originalItem.toPrettyString()} is not enabled because its parent item is not enabled:\n${toPrettyTree()}")
        }
    }
    val parent: Component = parentMap[this]
            ?: fail("${originalItem.toPrettyString()} is not part of\n${getContextMenu().toPrettyTree()}?!?")
    when (parent) {
        is MenuItem -> parent.checkMenuItemEnabled(originalItem, parentMap)
        is GridMenuItem<*> -> parent.checkMenuItemEnabled(originalItem, parentMap)
        is ContextMenu -> Unit
        is GridContextMenu<*> -> Unit
        // e.g. MenuBar
        else -> expect(true, "Cannot click ${originalItem.toPrettyString()} since it's attached to ${parent.toPrettyString()} which is not effectively visible") {
            parent.isEffectivelyEnabled()
        }
    }
}
