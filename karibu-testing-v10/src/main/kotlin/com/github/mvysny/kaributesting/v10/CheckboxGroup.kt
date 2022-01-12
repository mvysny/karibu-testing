package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.checkbox.CheckboxGroup

/**
 * Fetches renderings of items currently displayed in the checkbox group component.
 */
public fun <T> CheckboxGroup<T>.getItemLabels(): List<String?> {
    val items: List<T> = dataProvider._findAll()
    return items.map { itemLabelGenerator.apply(it) }
}
