package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.listbox.ListBoxBase

/**
 * Fetches renderings of items currently displayed in the list box component.
 */
public fun <T> ListBoxBase<*, T, *>.getRenderedItems(): List<String?> {
    val items: List<T> = dataProvider._findAll()
    return items.map { itemRenderer._getPresentationValue(it) }
}
