@file:Suppress("FunctionName")

package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributools.IconName
import com.vaadin.flow.component.ClickEvent
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.contextmenu.*
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem
import com.vaadin.flow.dom.DomEvent
import com.vaadin.flow.dom.Element
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.node.ObjectNode
import java.lang.reflect.Method
import kotlin.test.expect
import kotlin.test.fail

/**
 * Tries to find a menu item matching given [searchSpec] and click it.
 * @throws AssertionError if no such menu item exists, or the menu item is not enabled or visible, or it's nested in
 * a menu item which is invisible or disabled, or it's attached to a component that's invisible.
 */
public fun HasMenuItems._clickItemMatching(searchSpec: SearchSpec<MenuItemBase<*, *, *>>) {
    // fires ContextMenuOpenedListener to simulate menu opening
    (this as? ContextMenu)?.setOpened(true)

    _findAndClickItem(searchSpec)

    // fires ContextMenuOpenedListener to simulate menu closing
    (this as? ContextMenu)?.setOpened(false)
}

/**
 * Finds a menu item matching given [searchSpec] in this menu and clicks it. Doesn't open/close
 * the menu - use e.g. [_clickItemMatching] or [Component._openContextMenu] for that.
 */
private fun HasMenuItems._findAndClickItem(searchSpec: SearchSpec<MenuItemBase<*, *, *>>) {
    val parentMap: Map<MenuItemBase<*, *, *>, Component> = (this as Component).getParentMap()
    val predicate = searchSpec.toPredicate()
    val item: MenuItemBase<*, *, *> = parentMap.keys.firstOrNull(predicate)
            ?: fail("No menu item with ${searchSpec.toString().removePrefix("MenuItemBase and ")} in this menu:\n${(this as Component).toPrettyTree()}")
    (item as MenuItem)._click(parentMap)
}

/**
 * Tries to find a menu item with given [caption] and click it.
 * @throws AssertionError if no such menu item exists, or the menu item is not enabled or visible, or it's nested in
 * a menu item which is invisible or disabled, or it's attached to a component that's invisible.
 */
public fun HasMenuItems._clickItemWithCaption(caption: String) {
    _clickItemMatching(SearchSpec(MenuItemBase::class.java, text = caption))
}

/**
 * Tries to find a menu item with given [id] and click it.
 * @throws AssertionError if no such menu item exists, or the menu item is not enabled or visible, or it's nested in
 * a menu item which is invisible or disabled, or it's attached to a component that's invisible.
 */
public fun HasMenuItems._clickItemWithID(id: String) {
    _clickItemMatching(SearchSpec(MenuItemBase::class.java, id = id))
}

/**
 * Tries to find a menu item with given [icon] and click it.
 * @throws AssertionError if no such menu item exists, or the menu item is not enabled or visible, or it's nested in
 * a menu item which is invisible or disabled, or it's attached to a component that's invisible.
 */
public fun HasMenuItems._clickItemWithIcon(icon: IconName) {
    _clickItemMatching(SearchSpec(MenuItemBase::class.java, icon = icon))
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
 * Tries to find a menu item with given [id] and click it, passing in given [gridItem].
 * @throws AssertionError if no such menu item exists, or the menu item is not enabled or visible, or it's nested in
 * a menu item which is invisible or disabled, or it's attached to a component that's invisible.
 */
public fun <T> GridContextMenu<T>._clickItemWithID(id: String, gridItem: T?) {
    _clickItemMatching(SearchSpec(MenuItemBase::class.java, id = id), gridItem)
}

/**
 * Tries to find a menu item with given [caption] and click it, passing in given [gridItem].
 * @throws AssertionError if no such menu item exists, or the menu item is not enabled or visible, or it's nested in
 * a menu item which is invisible or disabled, or it's attached to a component that's invisible.
 */
public fun <T> GridContextMenu<T>._clickItemWithCaption(caption: String, gridItem: T?) {
    _clickItemMatching(SearchSpec(MenuItemBase::class.java, text = caption), gridItem)
}

/**
 * Tries to find a menu item with given [icon] and click it, passing in given [gridItem].
 * @throws AssertionError if no such menu item exists, or the menu item is not enabled or visible, or it's nested in
 * a menu item which is invisible or disabled, or it's attached to a component that's invisible.
 */
public fun <T> GridContextMenu<T>._clickItemWithIcon(icon: IconName, gridItem: T?) {
    _clickItemMatching(SearchSpec(MenuItemBase::class.java, icon = icon), gridItem)
}

/**
 * Tries to find a menu item matching given [searchSpec] and click it, passing in given [gridItem].
 * @throws AssertionError if no such menu item exists, or the menu item is not enabled or visible, or it's nested in
 * a menu item which is invisible or disabled, or it's attached to a component that's invisible.
 */
public fun <T> GridContextMenu<T>._clickItemMatching(searchSpec: SearchSpec<MenuItemBase<*, *, *>>, gridItem: T?) {
    // fires ContextMenuOpenedListener to simulate menu opening
    setOpened(true, gridItem)

    val parentMap: Map<MenuItemBase<*, *, *>, Component> = getParentMap()
    val predicate = searchSpec.toPredicate()
    val item: MenuItemBase<*, *, *> = parentMap.keys.firstOrNull(predicate)
            ?: fail("No menu item with ${searchSpec.toString().removePrefix("MenuItemBase and ")} in GridContextMenu:\n${toPrettyTree()}")
    @Suppress("UNCHECKED_CAST")
    (item as GridMenuItem<T>)._click(gridItem)

    // fires ContextMenuOpenedListener to simulate menu closing
    setOpened(false, gridItem)
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
            ?: fail("${originalItem.toPrettyString()} is not part of\n${contextMenu.toPrettyTree()}?!?")
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
            parent.isEnabled
        }
    }
}

private val __SubMenuBase_getMenuManager: Method by lazy {
    val m = SubMenuBase::class.java.getDeclaredMethod("getMenuManager")
    m.isAccessible = true
    m
}

public val SubMenuBase<*, *, *>._menuManager: MenuManager<*, *, *> get() = __SubMenuBase_getMenuManager.invoke(this) as MenuManager<*, *, *>

/**
 * Opens or closes the menu. Fires the [ContextMenuBase.OpenedChangeEvent].
 */
public fun ContextMenu.setOpened(opened: Boolean) {
    element.setProperty("opened", opened)
}

/**
 * Opens or closes the menu. Fires the [ContextMenuBase.OpenedChangeEvent].
 *
 * On open, fires the real `vaadin-context-menu-before-open` DOM event, which runs the dynamic
 * content generator and attaches the menu to the UI (so it's discoverable via [_find] while open).
 * On close, detaches the menu again.
 */
@Suppress("UNCHECKED_CAST")
@JvmOverloads
public fun <T> GridContextMenu<T>.setOpened(opened: Boolean, gridItem: T?, column: Grid.Column<T>? = null) {
    _setContextMenuTargetItemKey(gridItem)
    if (column != null) {
        val id = requireNotNull(column.id_) { "Column $column must have an ID assigned in order to be identifiable in the event object" }
        require(id.isNotBlank()) { "Column $column must have an ID assigned in order to be identifiable in the event object" }
        target.element.setProperty("_contextMenuTargetColumnId", id)
    }
    if (opened) {
        // Fire the real before-open event: it runs the dynamic content generator (onBeforeOpenMenu)
        // and attaches the menu to the UI. If the dynamic content handler vetoes opening, the menu
        // is not attached.
        val grid = target as Grid<T>
        val itemKey = if (gridItem == null) null else grid.dataCommunicator.keyMapper.key(gridItem)
        val detail: ObjectNode = ObjectMapper().createObjectNode()
        detail.put("key", itemKey ?: "")
        detail.put("columnId", column?.id_ ?: "")
        val wasAttached = element.parent != null
        fireContextMenuBeforeOpen(grid, detail)
        if (!wasAttached && element.parent == null) {
            fail("The dynamic content handler returned false signalling the menu should not open:\n${toPrettyTree()}")
        }
    }
    element.setProperty("opened", opened)
    if (!opened) {
        // detach the menu again (it was attached to the UI by the before-open event on open)
        _fireClosed()
    }
}

/**
 * The DOM event fired by the target component to make its [ContextMenu] open. Registered on
 * the target's element by [ContextMenuBase.setTarget]; its handler runs the dynamic content
 * generator and attaches the menu to the UI.
 */
private const val CONTEXT_MENU_BEFORE_OPEN_EVENT = "vaadin-context-menu-before-open"

/**
 * Fires the [CONTEXT_MENU_BEFORE_OPEN_EVENT] DOM event on [target]'s element, passing given
 * [detail] as the `event.detail`. This makes Vaadin run the context menu's dynamic content
 * generator and attach the menu to the UI (so it becomes discoverable via [_find] and visible
 * in [toPrettyTree]), exactly as if the user right-clicked the component in the browser.
 *
 * Uses the low-level [Element._fireDomEvent] to bypass the visible+enabled check, since a
 * context menu also opens on disabled components (see [`clicking menu on disabled component succeeds`]).
 */
private fun fireContextMenuBeforeOpen(target: Component, detail: ObjectNode) {
    val eventData: ObjectNode = ObjectMapper().createObjectNode()
    eventData.set("event.detail", detail)
    val element: Element = target.element
    // The before-open listener is registered as ONLY_WHEN_ENABLED, so ElementListenerMap.fireEvent
    // would drop it on a disabled target. But a ContextMenu opens even on a disabled component (parity
    // with the reference-based _clickItemMatching, whose checkMenuItemEnabled skips the target). The
    // enabled gate checks event.source.isEnabled(), so we present an enabled source when the target is
    // disabled - the handler only reads event.detail, never the source.
    val source: Element = if (element.isEnabled) element else currentUI.element
    element._fireDomEvent(DomEvent(source, CONTEXT_MENU_BEFORE_OPEN_EVENT, eventData))
}

/**
 * Opens the [ContextMenu] attached to this component, simulating the user right-clicking (or
 * long-touching) the component in the browser. See [Issue 20](https://github.com/mvysny/karibu-testing/issues/20).
 *
 * The menu is attached to the UI - becoming discoverable via [_find] and visible in [toPrettyTree] -
 * and its dynamic content generator runs, so the menu is fully populated. Remember to [_close]
 * the returned menu afterwards; or use [_clickContextMenuItemWithCaption] and friends which
 * open and close the menu automatically.
 *
 * For [Grid]s use [Grid._openContextMenu] instead, to pass in the item/column being right-clicked.
 * @return the now-open context menu.
 * @throws AssertionError if no context menu is attached to this component (or it refused to open),
 * or if more than one context menu is attached (unsupported).
 */
public fun Component._openContextMenu(): ContextMenu {
    fireContextMenuBeforeOpen(this, ObjectMapper().createObjectNode())
    // The menu attaches to the UI (as a sibling), not under this component, so search from the UI.
    val menus: List<ContextMenu> = currentUI._find<ContextMenu>().filter { it.target === this }
    val menu: ContextMenu = when (menus.size) {
        0 -> fail("No ContextMenu is attached to ${toPrettyString()}, or it refused to open:\n${toPrettyTree()}")
        1 -> menus[0]
        else -> fail("Multiple ContextMenus are attached to ${toPrettyString()}; this is unsupported:\n${toPrettyTree()}")
    }
    // fire the ContextMenuBase.OpenedChangeEvent
    menu.setOpened(true)
    return menu
}

/**
 * Opens the [GridContextMenu] attached to this grid, simulating the user right-clicking (or
 * long-touching) the grid in the browser. See [Issue 20](https://github.com/mvysny/karibu-testing/issues/20).
 *
 * The menu is attached to the UI - becoming discoverable via [_find] and visible in [toPrettyTree] -
 * and its dynamic content generator runs, so the menu is fully populated. Remember to [_close]
 * the returned menu afterwards; or use [_clickContextMenuItemWithCaption] and friends which
 * open and close the menu automatically.
 * @param item the item which was right-clicked. `null` when the grid is right-clicked outside
 * of any item (e.g. if there are no items shown in the grid).
 * @param column the column which was right-clicked, or `null` if unknown. Must have an ID
 * assigned via [Grid.Column.setId] in order to be identifiable.
 * @return the now-open context menu.
 * @throws AssertionError if no grid context menu is attached to this grid (or its dynamic content
 * handler refused to open), or if more than one is attached (unsupported).
 */
@JvmOverloads
public fun <T> Grid<T>._openContextMenu(item: T?, column: Grid.Column<T>? = null): GridContextMenu<T> {
    val key: String? = if (item == null) null else dataCommunicator.keyMapper.key(item)
    // set the properties read by the GridContextMenuItemClickEvent
    element.setProperty("_contextMenuTargetItemKey", key ?: "")
    if (column != null) {
        val id: String? = column.id_
        require(!id.isNullOrBlank()) { "Column $column must have an ID assigned in order to be identifiable in the event object" }
        element.setProperty("_contextMenuTargetColumnId", id)
    }
    val detail: ObjectNode = ObjectMapper().createObjectNode()
    detail.put("key", key ?: "")
    detail.put("columnId", column?.id_ ?: "")
    fireContextMenuBeforeOpen(this, detail)
    // The menu attaches to the UI (as a sibling), not under this grid, so search from the UI.
    @Suppress("UNCHECKED_CAST")
    val menus: List<GridContextMenu<T>> =
        currentUI._find<GridContextMenu<*>>().filter { it.target === this } as List<GridContextMenu<T>>
    val menu: GridContextMenu<T> = when (menus.size) {
        0 -> fail("No GridContextMenu is attached to ${toPrettyString()}, or its dynamic content handler refused to open:\n${toPrettyTree()}")
        1 -> menus[0]
        else -> fail("Multiple GridContextMenus are attached to ${toPrettyString()}; this is unsupported:\n${toPrettyTree()}")
    }
    // fire the ContextMenuBase.OpenedChangeEvent
    menu.element.setProperty("opened", true)
    return menu
}

/**
 * Closes this context menu (the opposite of [Component._openContextMenu]): fires the
 * `opened-changed` event with `false` and detaches the menu from the UI.
 */
public fun ContextMenuBase<*, *, *>._close() {
    element.setProperty("opened", false)
    _fireClosed()
}

/**
 * Fires the `closed` DOM event on this menu's element, which detaches it from the UI (if it was
 * auto-added on open). Expects [ContextMenuBase.isOpened] to already be `false`.
 */
private fun ContextMenuBase<*, *, *>._fireClosed() {
    element._fireDomEvent(DomEvent(element, "closed", ObjectMapper().createObjectNode()))
}

/**
 * Opens the [ContextMenu] attached to this component, clicks the menu item with given [caption],
 * then closes the menu again. See [Issue 20](https://github.com/mvysny/karibu-testing/issues/20).
 *
 * Doesn't require a reference to the [ContextMenu] - the menu is located via this target component.
 * For [Grid]s use [Grid._clickContextMenuItemWithCaption] instead.
 * @throws AssertionError if no such menu item exists, or the menu item is not enabled or visible,
 * or it's nested in a menu item which is invisible or disabled, or it's attached to a component
 * that's invisible, or no/multiple context menus are attached to this component.
 */
public fun Component._clickContextMenuItemWithCaption(caption: String) {
    _clickContextMenuItemMatching(SearchSpec(MenuItemBase::class.java, text = caption))
}

/**
 * Opens the [ContextMenu] attached to this component, clicks the menu item with given [id],
 * then closes the menu again. See [Component._clickContextMenuItemWithCaption].
 */
public fun Component._clickContextMenuItemWithID(id: String) {
    _clickContextMenuItemMatching(SearchSpec(MenuItemBase::class.java, id = id))
}

/**
 * Opens the [ContextMenu] attached to this component, clicks the menu item with given [icon],
 * then closes the menu again. See [Component._clickContextMenuItemWithCaption].
 */
public fun Component._clickContextMenuItemWithIcon(icon: IconName) {
    _clickContextMenuItemMatching(SearchSpec(MenuItemBase::class.java, icon = icon))
}

private fun Component._clickContextMenuItemMatching(searchSpec: SearchSpec<MenuItemBase<*, *, *>>) {
    val menu: ContextMenu = _openContextMenu()
    try {
        menu._findAndClickItem(searchSpec)
    } finally {
        menu._close()
    }
}

/**
 * Opens the [GridContextMenu] attached to this grid (passing in the right-clicked [item] and
 * [column]), clicks the menu item with given [caption], then closes the menu again.
 * See [Issue 20](https://github.com/mvysny/karibu-testing/issues/20).
 *
 * Doesn't require a reference to the [GridContextMenu] - the menu is located via this grid.
 * @param item the item which was right-clicked. `null` when the grid is right-clicked outside
 * of any item.
 * @param column the column which was right-clicked, or `null` if unknown.
 * @throws AssertionError if no such menu item exists, or it (or a parent) is disabled/invisible,
 * or the grid is invisible, or no/multiple grid context menus are attached to this grid.
 */
@JvmOverloads
public fun <T> Grid<T>._clickContextMenuItemWithCaption(caption: String, item: T?, column: Grid.Column<T>? = null) {
    _clickContextMenuItemMatching(SearchSpec(MenuItemBase::class.java, text = caption), item, column)
}

/**
 * Opens the [GridContextMenu] attached to this grid, clicks the menu item with given [id], then
 * closes the menu again. See [Grid._clickContextMenuItemWithCaption].
 */
@JvmOverloads
public fun <T> Grid<T>._clickContextMenuItemWithID(id: String, item: T?, column: Grid.Column<T>? = null) {
    _clickContextMenuItemMatching(SearchSpec(MenuItemBase::class.java, id = id), item, column)
}

/**
 * Opens the [GridContextMenu] attached to this grid, clicks the menu item with given [icon], then
 * closes the menu again. See [Grid._clickContextMenuItemWithCaption].
 */
@JvmOverloads
public fun <T> Grid<T>._clickContextMenuItemWithIcon(icon: IconName, item: T?, column: Grid.Column<T>? = null) {
    _clickContextMenuItemMatching(SearchSpec(MenuItemBase::class.java, icon = icon), item, column)
}

private fun <T> Grid<T>._clickContextMenuItemMatching(searchSpec: SearchSpec<MenuItemBase<*, *, *>>, item: T?, column: Grid.Column<T>?) {
    val menu: GridContextMenu<T> = _openContextMenu(item, column)
    try {
        val parentMap: Map<MenuItemBase<*, *, *>, Component> = (menu as Component).getParentMap()
        val menuItem: MenuItemBase<*, *, *> = parentMap.keys.firstOrNull(searchSpec.toPredicate())
            ?: fail("No menu item with ${searchSpec.toString().removePrefix("MenuItemBase and ")} in GridContextMenu:\n${menu.toPrettyTree()}")
        @Suppress("UNCHECKED_CAST")
        (menuItem as GridMenuItem<T>)._click(item)
    } finally {
        menu._close()
    }
}
