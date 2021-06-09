package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.ContextMenuKt
import com.vaadin.flow.component.contextmenu.HasMenuItems
import com.vaadin.flow.component.contextmenu.MenuItem
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem
import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

/**
 * A set of basic extension methods for {@link GridContextMenu} and {@link com.vaadin.flow.component.contextmenu.ContextMenu}.
 * @author mavi
 */
@CompileStatic
class ContextMenuUtils {
    /**
     * Tries to find a menu item with given caption and click it.
     * @throws AssertionError if no such menu item exists, or the menu item is not enabled or visible, or it's nested in
     * a menu item which is invisible or disabled, or it's attached to a component that's invisible.
     */
    static void _clickItemWithCaption(@NotNull HasMenuItems self, @NotNull String caption) {
        ContextMenuKt._clickItemWithCaption(self, caption)
    }

    /**
     * Tries to find a menu item with given caption and click it, passing in given [gridItem].
     * @throws AssertionError if no such menu item exists, or the menu item is not enabled or visible, or it's nested in
     * a menu item which is invisible or disabled, or it's attached to a component that's invisible.
     */
    static <T> void _clickItemWithCaption(@NotNull GridContextMenu<T> self,
                                     @NotNull String caption,
                                     @Nullable T gridItem) {
        ContextMenuKt._clickItemWithCaption(self, caption, gridItem)
    }

    /**
     * Tries to click given menu item.
     * @throws AssertionError if no such menu item exists, or the menu item is not enabled or visible, or it's nested in
     * a menu item which is invisible or disabled, or it's attached to a component that's invisible.
     */
    static void _click(@NotNull MenuItem self) {
        ContextMenuKt._click(self)
    }

    /**
     * Tries to click given menu item, passing in given [gridItem].
     * @throws AssertionError if no such menu item exists, or the menu item is not enabled or visible, or it's nested in
     * a menu item which is invisible or disabled, or it's attached to a component that's invisible.
     */
    static <T> void _click(@NotNull GridMenuItem<T> self, @Nullable T gridItem) {
        ContextMenuKt._click(self, gridItem)
    }
}
