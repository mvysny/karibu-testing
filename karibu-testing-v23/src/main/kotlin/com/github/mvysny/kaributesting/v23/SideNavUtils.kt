package com.github.mvysny.kaributesting.v23

import com.github.mvysny.kaributesting.v10._expectEditableByUser
import com.github.mvysny.kaributools.navigateTo
import com.vaadin.flow.component.sidenav.SideNavItem

/**
 * Clicks this item, but only if it's enabled. The effect is that it navigates to the underlying
 * view or [SideNavItem.getPath].
 */
public fun SideNavItem._click() {
    _expectEditableByUser()
    navigateTo(path)
}
