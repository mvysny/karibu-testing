package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.tabs.TabSheet
import com.vaadin.flow.component.tabs.Tabs
import java.lang.reflect.Field

private val _TabSheet_tabs: Field by lazy {
    val f = TabSheet::class.java.getDeclaredField("tabs")
    f.isAccessible = true
    f
}

/**
 * Retrieves the internal [Tabs] component from the [TabSheet].
 */
public val TabSheet._tabs: Tabs get() = _TabSheet_tabs.get(this) as Tabs
