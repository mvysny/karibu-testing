package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasValue
import com.vaadin.flow.component.ItemLabelGenerator
import com.vaadin.flow.component.checkbox.CheckboxGroup
import com.vaadin.flow.data.provider.DataProvider
import kotlin.test.fail

/**
 * Fetches renderings of items currently displayed in the checkbox group component.
 */
public fun <T> CheckboxGroup<T>.getItemLabels(): List<String?> {
    val items: List<T> = dataProvider._findAll()
    return items.map { itemLabelGenerator.apply(it) }
}

/**
 * Select all items in this [CheckboxGroup] by [labels]. Fails if the item is not found. Fails if this component is not editable.
 */
public fun <T> CheckboxGroup<T>.selectByLabel(vararg labels: String) {
    selectByLabel(labels.toSet(), itemLabelGenerator)
}

internal fun <T> HasValue<*, Set<T>>.selectByLabel(labels: Set<String>, itemLabelGenerator: ItemLabelGenerator<T>) {
    val labelSet: Set<String> = labels.toSet()
    val items: MutableMap<String, List<T>> = ((this as Component).dataProvider as DataProvider<T, *>)
        ._findAll()
        .groupBy { itemLabelGenerator.apply(it) }
        .toMutableMap()
    items.keys.retainAll(labelSet)
    val e = items.entries.firstOrNull { it.value.size > 1 }
    if (e != null) {
        fail("${toPrettyString()}: Multiple items found with label '${e.key}': ${e.value}")
    }
    if (items.keys.size < labelSet.size) {
        fail("${toPrettyString()}: No item found with label(s) '${labelSet.minus(items.keys)}'")
    }

    _value = items.values.flatten().toSet()
}