package com.github.mvysny.kaributesting.v23

import com.github.mvysny.kaributesting.v10._expectEditableByUser
import com.github.mvysny.kaributools.v23.owner
import com.vaadin.flow.component.tabs.Tab

/**
 * Selects this tab, but only if the tab can be selected by the user (it is visible and enabled).
 */
public fun Tab._select() {
    _expectEditableByUser()
    owner.selectedTab = this
}
